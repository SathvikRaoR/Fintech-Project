package com.fintech.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudCheckResponse {
    private Double score;
    private String decision; // NORMAL or SUSPICIOUS
    private String explanation;
}
