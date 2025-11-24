package com.fintech.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private Long id;
    private String email;
    private String name;
    private String kycStatus;
    private String token; // JWT token (for login response)
    private long expiresIn; // Token expiration in seconds
}
