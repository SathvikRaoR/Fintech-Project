package com.fintech.backend.controller;

import com.fintech.backend.dto.AccountDto;
import com.fintech.backend.dto.CreateAccountRequest;
import com.fintech.backend.service.AccountService;
import com.fintech.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class AccountController {

    private final AccountService accountService;
    private final AuthService authService;

    public AccountController(AccountService accountService, AuthService authService) {
        this.accountService = accountService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<List<AccountDto>> getAccounts() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Get accounts for user: {}", email);

        Long userId = authService.getUserByEmail(email).getId();
        List<AccountDto> accounts = accountService.getAccountsByUserId(userId);

        return ResponseEntity.ok(accounts);
    }

    @PostMapping
    public ResponseEntity<AccountDto> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Create account for user: {}", email);

        Long userId = authService.getUserByEmail(email).getId();
        AccountDto account = accountService.createAccount(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDto> getAccount(@PathVariable Long accountId) {
        log.info("Get account: {}", accountId);
        AccountDto account = accountService.getAccountById(accountId);
        return ResponseEntity.ok(account);
    }
}
