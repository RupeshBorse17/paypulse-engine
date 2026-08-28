package com.bank.settlementengine.service;

import com.bank.settlementengine.dto.SettlementRequestDto;
import com.bank.settlementengine.dto.SettlementResponseDto;
import com.bank.settlementengine.entity.Account;
import com.bank.settlementengine.entity.AuditLog;
import com.bank.settlementengine.repository.AccountRepository;
import com.bank.settlementengine.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final AccountRepository accountRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional
    public SettlementResponseDto processSettlement(SettlementRequestDto request) {

        String fromAccountId = request.getSenderAccountId();
        String toAccountId = request.getReceiverAccountId();
        var amount = request.getAmount();

        // १. Validation: स्वतःच्याच खात्यात ट्रान्सफर रोखणे
        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("Sender and Receiver Account cannot be the same!");
        }

        // २. Idempotency Check: डुप्लिकेट रिक्वेस्ट टाळण्यासाठी
        String idempotencyKey = request.getIdempotencyKey() != null ?
                request.getIdempotencyKey() : UUID.randomUUID().toString();

        if (auditLogRepository.existsByIdempotencyKey(idempotencyKey)) {
            AuditLog existingLog = auditLogRepository.findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(() -> new IllegalStateException("Audit log not found for existing key"));

            return SettlementResponseDto.builder()
                    .transactionId(existingLog.getTransactionId())
                    .status(existingLog.getStatus())
                    .message("Duplicate Request (Returned from Audit Log)")
                    .remainingBalance(null)
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        // ३. Accounts शोधणे
        Account fromAccount = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Sender account not found: " + fromAccountId));

        Account toAccount = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Receiver account not found: " + toAccountId));

        // ४. Balance Check: पाठवणाऱ्याकडे पुरेसे पैसे आहेत का?
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            AuditLog failedLog = AuditLog.builder()
                    .transactionId(UUID.randomUUID().toString())
                    .idempotencyKey(idempotencyKey)
                    .fromAccount(fromAccountId)
                    .toAccount(toAccountId)
                    .amount(amount)
                    .status("FAILED")
                    .failureReason("INSUFFICIENT_FUNDS")
                    .build();

            auditLogRepository.save(failedLog);

            return SettlementResponseDto.builder()
                    .transactionId(failedLog.getTransactionId())
                    .status("FAILED")
                    .message("Insufficient balance in sender account!")
                    .remainingBalance(fromAccount.getBalance())
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        // ५. पैसे कट व जमा करणे
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        try {
            // ६. डेटाबेसमध्ये सेव्ह करणे
            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);

            String txnId = UUID.randomUUID().toString();

            // ७. सक्सेस ऑडिट लॉग सेव्ह करणे
            AuditLog successLog = AuditLog.builder()
                    .transactionId(txnId)
                    .idempotencyKey(idempotencyKey)
                    .fromAccount(fromAccountId)
                    .toAccount(toAccountId)
                    .amount(amount)
                    .status("SUCCESS")
                    .failureReason(null)
                    .build();

            auditLogRepository.save(successLog);

            // ८. फायनल सक्सेस रिस्पॉन्स परत पाठवणे
            return SettlementResponseDto.builder()
                    .transactionId(txnId)
                    .status("SUCCESS")
                    .message("Fund transferred successfully!")
                    .remainingBalance(fromAccount.getBalance())
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (ObjectOptimisticLockingFailureException e) {
            // ९. कन्करन्सी (Optimistic Locking) हँडल करणे
            AuditLog concurrencyFailedLog = AuditLog.builder()
                    .transactionId(UUID.randomUUID().toString())
                    .idempotencyKey(idempotencyKey)
                    .fromAccount(fromAccountId)
                    .toAccount(toAccountId)
                    .amount(amount)
                    .status("FAILED")
                    .failureReason("CONCURRENCY_CONFLICT_TRY_AGAIN")
                    .build();

            auditLogRepository.save(concurrencyFailedLog);

            return SettlementResponseDto.builder()
                    .transactionId(concurrencyFailedLog.getTransactionId())
                    .status("FAILED")
                    .message("Concurrency conflict! Please try again.")
                    .remainingBalance(null)
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }
}