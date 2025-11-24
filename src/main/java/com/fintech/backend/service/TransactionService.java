package com.fintech.backend.service;

import com.fintech.backend.audit.Auditable;
import com.fintech.backend.dto.TransactionDto;
import com.fintech.backend.model.Account;
import com.fintech.backend.model.Transaction;
import com.fintech.backend.repository.TransactionRepository;
import com.fintech.backend.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final ComplianceService complianceService;

    public TransactionService(TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            ComplianceService complianceService) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.complianceService = complianceService;
    }

    @Transactional
    @Auditable(action = "DEPOSIT", entityType = "Transaction")
    public TransactionDto deposit(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));

        // Check AML rules
        String riskFlag = complianceService.checkAmlRules(accountId, amount) ? "SUSPICIOUS" : "NORMAL";

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .account(account)
                .type("DEPOSIT")
                .amount(amount)
                .timestamp(LocalDateTime.now())
                .riskFlag(riskFlag)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        return mapToDto(savedTransaction);
    }

    @Transactional
    @Auditable(action = "WITHDRAW", entityType = "Transaction")
    public TransactionDto withdraw(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));

        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException(
                    "Insufficient balance. Current: " + account.getBalance() + ", Required: " + amount);
        }

        // Check AML rules
        String riskFlag = complianceService.checkAmlRules(accountId, amount) ? "SUSPICIOUS" : "NORMAL";

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .account(account)
                .type("WITHDRAWAL")
                .amount(amount)
                .timestamp(LocalDateTime.now())
                .riskFlag(riskFlag)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        return mapToDto(savedTransaction);
    }

    @Transactional
    @Auditable(action = "TRANSFER", entityType = "Transaction")
    public TransactionDto transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        Account fromAccount = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new RuntimeException("From account not found"));

        Account toAccount = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new RuntimeException("To account not found"));

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance for transfer");
        }

        // Check AML rules for large transfers
        String riskFlag = complianceService.checkAmlRules(fromAccountId, amount) ? "SUSPICIOUS" : "NORMAL";

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        Transaction transaction = Transaction.builder()
                .account(fromAccount)
                .type("TRANSFER")
                .amount(amount)
                .timestamp(LocalDateTime.now())
                .riskFlag(riskFlag)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        return mapToDto(savedTransaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionDto> getTransactionHistory(Long accountId) {
        return transactionRepository.findByAccountId(accountId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private TransactionDto mapToDto(Transaction transaction) {
        return TransactionDto.builder()
                .id(transaction.getId())
                .accountId(transaction.getAccount().getId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .timestamp(transaction.getTimestamp())
                .riskFlag(transaction.getRiskFlag())
                .build();
    }
}
