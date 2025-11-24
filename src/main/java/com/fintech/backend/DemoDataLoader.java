package com.fintech.backend;

import com.fintech.backend.model.Account;
import com.fintech.backend.model.Transaction;
import com.fintech.backend.model.User;
import com.fintech.backend.repository.AccountRepository;
import com.fintech.backend.repository.TransactionRepository;
import com.fintech.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Configuration
@Slf4j
public class DemoDataLoader {

    @Bean
    public CommandLineRunner loadDemoData(UserRepository userRepository,
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if data already exists
            if (userRepository.count() > 0) {
                log.info("Demo data already loaded");
                return;
            }

            log.info("Loading demo data...");

            // Create 3 users
            User user1 = User.builder()
                    .email("john@example.com")
                    .password(passwordEncoder.encode("Password123"))
                    .name("John Doe")
                    .kycStatus("VERIFIED")
                    .build();

            User user2 = User.builder()
                    .email("jane@example.com")
                    .password(passwordEncoder.encode("SecurePass456"))
                    .name("Jane Smith")
                    .kycStatus("VERIFIED")
                    .build();

            User user3 = User.builder()
                    .email("bob@example.com")
                    .password(passwordEncoder.encode("MyPassword789"))
                    .name("Bob Johnson")
                    .kycStatus("PENDING")
                    .build();

            user1 = userRepository.save(user1);
            user2 = userRepository.save(user2);
            user3 = userRepository.save(user3);

            // Create accounts for each user
            Account acc1_1 = Account.builder()
                    .user(user1)
                    .accountType("SAVINGS")
                    .balance(new BigDecimal("10000.00"))
                    .status("ACTIVE")
                    .build();

            Account acc1_2 = Account.builder()
                    .user(user1)
                    .accountType("FD")
                    .balance(new BigDecimal("50000.00"))
                    .status("ACTIVE")
                    .build();

            Account acc2_1 = Account.builder()
                    .user(user2)
                    .accountType("INVESTMENT")
                    .balance(new BigDecimal("25000.00"))
                    .status("ACTIVE")
                    .build();

            Account acc3_1 = Account.builder()
                    .user(user3)
                    .accountType("SAVINGS")
                    .balance(new BigDecimal("5000.00"))
                    .status("ACTIVE")
                    .build();

            acc1_1 = accountRepository.save(acc1_1);
            acc1_2 = accountRepository.save(acc1_2);
            acc2_1 = accountRepository.save(acc2_1);
            acc3_1 = accountRepository.save(acc3_1);

            // Create 50 transactions
            Random rand = new Random();
            LocalDateTime baseTime = LocalDateTime.now().minus(30, ChronoUnit.DAYS);

            String[] types = { "DEPOSIT", "WITHDRAWAL", "TRANSFER" };
            BigDecimal[] amounts = {
                    BigDecimal.valueOf(100), BigDecimal.valueOf(500), BigDecimal.valueOf(1000),
                    BigDecimal.valueOf(5000), BigDecimal.valueOf(10000), BigDecimal.valueOf(25000),
                    BigDecimal.valueOf(50000), BigDecimal.valueOf(75000)
            };

            for (int i = 0; i < 50; i++) {
                Account account = switch (i % 4) {
                    case 0 -> acc1_1;
                    case 1 -> acc1_2;
                    case 2 -> acc2_1;
                    default -> acc3_1;
                };

                String type = types[rand.nextInt(types.length)];
                BigDecimal amount = amounts[rand.nextInt(amounts.length)];

                // Mark some large transactions as suspicious for AML testing
                String riskFlag = "NORMAL";
                if (amount.compareTo(BigDecimal.valueOf(50000)) >= 0) {
                    riskFlag = "SUSPICIOUS";
                }
                // Randomly flag some transactions (5% chance)
                if (rand.nextDouble() < 0.05) {
                    riskFlag = "SUSPICIOUS";
                }

                Transaction transaction = Transaction.builder()
                        .account(account)
                        .type(type)
                        .amount(amount)
                        .timestamp(baseTime.plus(i, ChronoUnit.HOURS))
                        .riskFlag(riskFlag)
                        .build();

                transactionRepository.save(transaction);
            }

            log.info("Demo data loaded successfully: 3 users, 4 accounts, 50 transactions");
        };
    }
}
