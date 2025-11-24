package com.fintech.backend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDto {
    private Long id;
    private Long accountId;
    private String type;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private String riskFlag;
}
