package fintech2.easypay.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PinVerifyRequest {
    private String pin;          // 입력된 PIN (6자리)
    private String purpose;      // PIN 사용 목적 ("transfer", "payment", "common")
    
    // PIN 유효성 검사
    public boolean isValidPin() {
        return pin != null && pin.matches("^\\d{6}$");
    }
    
    // 목적 유효성 검사
    public boolean isValidPurpose() {
        return purpose != null && 
               (purpose.equals("transfer") || purpose.equals("payment") || purpose.equals("common"));
    }
}