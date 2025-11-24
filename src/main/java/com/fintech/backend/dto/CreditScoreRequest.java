package com.fintech.backend.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditScoreRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    private Map<String, Object> features;
}
