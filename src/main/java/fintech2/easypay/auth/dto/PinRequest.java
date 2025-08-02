package fintech2.easypay.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PinRequest {
    private String pin;           // 새로운 PIN (6자리)
    private String currentPassword; // 현재 비밀번호 (PIN 설정 시 보안 확인용)
    
    // PIN 유효성 검사
    public boolean isValidPin() {
        return pin != null && pin.matches("^\\d{6}$");
    }
}