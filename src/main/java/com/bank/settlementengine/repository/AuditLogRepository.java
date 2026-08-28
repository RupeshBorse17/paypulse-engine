package com.bank.settlementengine.repository;

import com.bank.settlementengine.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Idempotency Key आधीच डेटाबेसमध्ये वापरली आहे का ते तपासण्यासाठी
    boolean existsByIdempotencyKey(String idempotencyKey);

    Optional<AuditLog> findByIdempotencyKey(String idempotencyKey);
}