package com.fintech.backend.controller;

import com.fintech.backend.dto.*;
import com.fintech.backend.service.InsightsService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class InsightsController {

    private final InsightsService insightsService;

    public InsightsController(InsightsService insightsService) {
        this.insightsService = insightsService;
    }

    @PostMapping("/fraud-check")
    public ResponseEntity<FraudCheckResponse> fraudCheck(@Valid @RequestBody FraudCheckRequest request) {
        log.info("Fraud check for account: {} amount: {}", request.getAccountId(), request.getAmount());
        FraudCheckResponse response = insightsService.checkFraud(request.getAccountId(), request.getAmount());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/credit-score")
    public ResponseEntity<CreditScoreResponse> creditScore(@Valid @RequestBody CreditScoreRequest request) {
        log.info("Credit score calculation for user: {}", request.getUserId());
        CreditScoreResponse response = insightsService.calculateCreditScore(request.getUserId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        log.info("Chat request from user: {}", request.getUserId());
        ChatResponse response = insightsService.chat(request.getUserId(), request.getMessage());
        return ResponseEntity.ok(response);
    }
}
