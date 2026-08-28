package com.bank.settlementengine.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementResponseDto {

    private String transactionId;
    private String status;
    private String message;
    private BigDecimal remainingBalance;
    private LocalDateTime timestamp;
}