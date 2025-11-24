package com.fintech.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditScoreResponse {
    private Integer score;
    private Double probDefault;
    private String riskLevel;
}
