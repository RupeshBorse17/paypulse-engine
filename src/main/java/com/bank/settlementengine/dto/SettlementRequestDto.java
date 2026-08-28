package com.bank.settlementengine.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettlementRequestDto {

    private String senderAccountId;
    private String receiverAccountId;
    private BigDecimal amount;
    private String idempotencyKey; // <-- हे फिल्ड जोडले आहे का तपासून घ्या
}