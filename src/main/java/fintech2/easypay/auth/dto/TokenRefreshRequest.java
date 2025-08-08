package fintech2.easypay.auth.dto;

import lombok.Data;

@Data
public class TokenRefreshRequest {
    private String refreshToken;
} 