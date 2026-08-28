package com.bank.settlementengine.controller;

import com.bank.settlementengine.dto.SettlementRequestDto;
import com.bank.settlementengine.dto.SettlementResponseDto;
import com.bank.settlementengine.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settlement")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @PostMapping("/process")
    public ResponseEntity<SettlementResponseDto> processSettlement(@RequestBody SettlementRequestDto request) {
        SettlementResponseDto response = settlementService.processSettlement(request);
        return ResponseEntity.ok(response);
    }
}