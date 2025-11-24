package com.fintech.backend.service;

import com.fintech.backend.dto.TransactionDto;
import com.fintech.backend.model.Account;
import com.fintech.backend.model.Transaction;
import com.fintech.backend.model.User;
import com.fintech.backend.repository.AccountRepository;
import com.fintech.backend.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ComplianceService complianceService;

    @InjectMocks
    private TransactionService transactionService;

    private Account testAccount;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        User testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .password("encrypted")
                .kycStatus("VERIFIED")
                .build();

        testAccount = Account.builder()
                .id(1L)
                .user(testUser)
                .accountType("SAVINGS")
                .balance(new BigDecimal("10000.00"))
                .status("ACTIVE")
                .build();

        testTransaction = Transaction.builder()
                .id(1L)
                .account(testAccount)
                .type("DEPOSIT")
                .amount(new BigDecimal("1000.00"))
                .timestamp(LocalDateTime.now())
                .riskFlag("NORMAL")
                .build();
    }

    @Test
    void testDeposit() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(complianceService.checkAmlRules(1L, new BigDecimal("1000.00"))).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        TransactionDto result = transactionService.deposit(1L, new BigDecimal("1000.00"));

        assertNotNull(result);
        assertEquals("DEPOSIT", result.getType());
        assertEquals("NORMAL", result.getRiskFlag());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testWithdraw() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(complianceService.checkAmlRules(1L, new BigDecimal("500.00"))).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        TransactionDto result = transactionService.withdraw(1L, new BigDecimal("500.00"));

        assertNotNull(result);
        assertEquals("WITHDRAWAL", result.getType());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testWithdrawInsufficientBalance() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        assertThrows(RuntimeException.class, () -> transactionService.withdraw(1L, new BigDecimal("15000.00")));
    }

    @Test
    void testTransfer() {
        Account toAccount = Account.builder()
                .id(2L)
                .accountType("SAVINGS")
                .balance(new BigDecimal("5000.00"))
                .status("ACTIVE")
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(toAccount));
        when(complianceService.checkAmlRules(1L, new BigDecimal("1000.00"))).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        TransactionDto result = transactionService.transfer(1L, 2L, new BigDecimal("1000.00"));

        assertNotNull(result);
        assertEquals("TRANSFER", result.getType());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testTransferInsufficientBalance() {
        Account toAccount = Account.builder()
                .id(2L)
                .accountType("SAVINGS")
                .balance(new BigDecimal("5000.00"))
                .status("ACTIVE")
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(toAccount));

        assertThrows(RuntimeException.class, () -> transactionService.transfer(1L, 2L, new BigDecimal("15000.00")));
    }
}
