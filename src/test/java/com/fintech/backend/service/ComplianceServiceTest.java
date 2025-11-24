package com.fintech.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ComplianceServiceTest {

    @InjectMocks
    private ComplianceService complianceService;

    @Test
    void testCheckAmlRulesNormalAmount() {
        // Set AML threshold via reflection
        ReflectionTestUtils.setField(complianceService, "amlThreshold", BigDecimal.valueOf(50000.0));

        boolean result = complianceService.checkAmlRules(1L, BigDecimal.valueOf(10000.0));

        assertFalse(result, "Amount under threshold should not be flagged");
    }

    @Test
    void testCheckAmlRulesSuspiciousAmount() {
        ReflectionTestUtils.setField(complianceService, "amlThreshold", BigDecimal.valueOf(50000.0));

        boolean result = complianceService.checkAmlRules(1L, BigDecimal.valueOf(75000.0));

        assertTrue(result, "Amount exceeding threshold should be flagged");
    }

    @Test
    void testCheckAmlRulesThresholdBoundary() {
        ReflectionTestUtils.setField(complianceService, "amlThreshold", BigDecimal.valueOf(50000.0));

        boolean result = complianceService.checkAmlRules(1L, BigDecimal.valueOf(50000.0));

        assertTrue(result, "Amount equal to threshold should be flagged");
    }
}
