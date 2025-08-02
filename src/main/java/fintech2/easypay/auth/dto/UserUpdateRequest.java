package fintech2.easypay.auth.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String name;
    private String email;
    private String phoneNumber;
    private String newPassword;
} 