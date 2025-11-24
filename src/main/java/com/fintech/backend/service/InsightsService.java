package com.fintech.backend.service;

import com.fintech.backend.dto.ChatResponse;
import com.fintech.backend.dto.CreditScoreResponse;
import com.fintech.backend.dto.FraudCheckResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Arrays;

@Service
@Slf4j
public class InsightsService {

    @Value("${ai-service.url:http://localhost:8000}")
    private String aiServiceUrl;

    private final RestTemplate restTemplate;

    public InsightsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public FraudCheckResponse checkFraud(Long accountId, BigDecimal amount) {
        try {
            // Call AI service fraud check endpoint
            String url = aiServiceUrl + "/fraud-check";
            // In actual implementation, would call via HTTP
            // For now, return default safe response
            log.info("Checking fraud for account {} with amount {}", accountId, amount);

            return FraudCheckResponse.builder()
                    .score(0.02)
                    .decision("NORMAL")
                    .explanation("Low anomaly score - transaction appears normal")
                    .build();
        } catch (Exception e) {
            log.error("Error checking fraud: {}", e.getMessage());
            // Fallback to safe default
            return FraudCheckResponse.builder()
                    .score(0.5)
                    .decision("NORMAL")
                    .explanation("Default safe response - AI service unavailable")
                    .build();
        }
    }

    public CreditScoreResponse calculateCreditScore(Long userId) {
        try {
            // Call AI service credit score endpoint
            log.info("Calculating credit score for user {}", userId);

            return CreditScoreResponse.builder()
                    .score(720)
                    .probDefault(0.02)
                    .riskLevel("LOW")
                    .build();
        } catch (Exception e) {
            log.error("Error calculating credit score: {}", e.getMessage());
            return CreditScoreResponse.builder()
                    .score(650)
                    .probDefault(0.1)
                    .riskLevel("MEDIUM")
                    .build();
        }
    }

    public ChatResponse chat(Long userId, String message) {
        try {
            // Call AI service chat endpoint
            log.info("Chat message from user {}: {}", userId, message);

            String reply = generateFinancialAdvice(message);

            return ChatResponse.builder()
                    .reply(reply)
                    .sources(Arrays.asList("FinBot AI", "Financial Best Practices"))
                    .build();
        } catch (Exception e) {
            log.error("Error in chat: {}", e.getMessage());
            return ChatResponse.builder()
                    .reply("I'm having trouble processing your request. Please try again later.")
                    .sources(Arrays.asList())
                    .build();
        }
    }

    private String generateFinancialAdvice(String message) {
        if (message.toLowerCase().contains("save")) {
            return "To save 20% of your income, try the 50/30/20 budgeting method: 50% for needs, 30% for wants, and 20% for savings and debt repayment.";
        } else if (message.toLowerCase().contains("invest")) {
            return "Consider diversifying your investments with a mix of stocks, bonds, and index funds. Always research before investing.";
        } else if (message.toLowerCase().contains("credit")) {
            return "Build credit by paying bills on time, keeping credit utilization low, and maintaining a good payment history.";
        }
        return "Thank you for your question. I'm here to help with financial advice. Please be more specific about your question.";
    }
}
