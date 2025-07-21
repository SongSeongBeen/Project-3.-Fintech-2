package fintech2.easypay.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignInRequest {
    
    @NotBlank(message = "휴대폰 번호는 필수입니다.")
    private String phoneNumber;
    
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}
