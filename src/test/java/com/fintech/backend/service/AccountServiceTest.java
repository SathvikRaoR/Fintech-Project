package com.fintech.backend.service;

import com.fintech.backend.dto.AccountDto;
import com.fintech.backend.dto.CreateAccountRequest;
import com.fintech.backend.model.Account;
import com.fintech.backend.model.User;
import com.fintech.backend.repository.AccountRepository;
import com.fintech.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountService accountService;

    private User testUser;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
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
    }

    @Test
    void testCreateAccount() {
        CreateAccountRequest request = CreateAccountRequest.builder()
                .accountType("SAVINGS")
                .initialDeposit(new BigDecimal("5000.00"))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        AccountDto result = accountService.createAccount(1L, request);

        assertNotNull(result);
        assertEquals("SAVINGS", result.getAccountType());
        assertEquals(new BigDecimal("10000.00"), result.getBalance());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void testGetAccountsByUserId() {
        List<Account> accounts = new ArrayList<>();
        accounts.add(testAccount);

        when(accountRepository.findByUserId(1L)).thenReturn(accounts);

        List<AccountDto> result = accountService.getAccountsByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("SAVINGS", result.get(0).getAccountType());
    }

    @Test
    void testGetAccountById() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        AccountDto result = accountService.getAccountById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("SAVINGS", result.getAccountType());
    }

    @Test
    void testGetAccountByIdNotFound() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> accountService.getAccountById(999L));
    }

    @Test
    void testUpdateBalance() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        Account result = accountService.updateBalance(1L, new BigDecimal("1000.00"));

        assertNotNull(result);
        assertEquals(new BigDecimal("10000.00"), result.getBalance());
        verify(accountRepository, times(1)).save(any(Account.class));
    }
}
