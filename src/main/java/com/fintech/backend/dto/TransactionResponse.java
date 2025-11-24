package com.fintech.backend.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {
    private Long transactionId;
    private BigDecimal newBalance;
    private BigDecimal fromBalance;
    private BigDecimal toBalance;
    private String riskFlag;
}
