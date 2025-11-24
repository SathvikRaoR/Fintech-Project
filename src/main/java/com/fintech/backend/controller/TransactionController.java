package com.fintech.backend.controller;

import com.fintech.backend.dto.TransactionDto;
import com.fintech.backend.dto.TransactionRequest;
import com.fintech.backend.dto.TransactionResponse;
import com.fintech.backend.service.TransactionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(@Valid @RequestBody TransactionRequest request) {
        log.info("Deposit request for account: {} amount: {}", request.getAccountId(), request.getAmount());

        TransactionDto transaction = transactionService.deposit(request.getAccountId(), request.getAmount());

        TransactionResponse response = TransactionResponse.builder()
                .transactionId(transaction.getId())
                .newBalance(null) // Would need to fetch account balance
                .riskFlag(transaction.getRiskFlag())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(@Valid @RequestBody TransactionRequest request) {
        log.info("Withdraw request for account: {} amount: {}", request.getAccountId(), request.getAmount());

        TransactionDto transaction = transactionService.withdraw(request.getAccountId(), request.getAmount());

        TransactionResponse response = TransactionResponse.builder()
                .transactionId(transaction.getId())
                .newBalance(null)
                .riskFlag(transaction.getRiskFlag())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransactionRequest request) {
        log.info("Transfer request from: {} to: {} amount: {}",
                request.getAccountId(), request.getToAccountId(), request.getAmount());

        TransactionDto transaction = transactionService.transfer(request.getAccountId(), request.getToAccountId(),
                request.getAmount());

        TransactionResponse response = TransactionResponse.builder()
                .transactionId(transaction.getId())
                .fromBalance(null)
                .toBalance(null)
                .riskFlag(transaction.getRiskFlag())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{accountId}")
    public ResponseEntity<List<TransactionDto>> getTransactionHistory(@PathVariable Long accountId) {
        log.info("Get transaction history for account: {}", accountId);
        List<TransactionDto> transactions = transactionService.getTransactionHistory(accountId);
        return ResponseEntity.ok(transactions);
    }
}
