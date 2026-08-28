package com.bank.settlementengine.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnTransformer;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "transaction_id", nullable = false, unique = true, length = 100)
    private String transactionId;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    @Column(name = "from_account", nullable = false, length = 50)
    private String fromAccount;

    @Column(name = "to_account", nullable = false, length = 50)
    private String toAccount;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    // PostgreSQL Enum Casting साठी @ColumnTransformer जोडले आहे
    @Column(name = "status", nullable = false, length = 30)
    @ColumnTransformer(write = "?::transaction_status")
    private String status;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}