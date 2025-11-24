package com.fintech.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class ComplianceService {

    @Value("${compliance.aml-threshold:50000.0}")
    private BigDecimal amlThreshold;

    public boolean checkAmlRules(Long accountId, BigDecimal amount) {
        // Rule 1: Flag if transfer amount > threshold
        if (amount.compareTo(amlThreshold) > 0) {
            log.warn("AML flagged: Transaction amount {} exceeds threshold {}", amount, amlThreshold);
            return true;
        }

        // Rule 2: Additional checks can be added here (velocity checks, etc.)

        return false;
    }

    public boolean verifyKyc(Long userId) {
        // KYC verification logic (placeholder)
        // In real scenario, this would check user's KYC status
        return true;
    }
}
