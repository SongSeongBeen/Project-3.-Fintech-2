package fintech2.easypay.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePinRequest {
    private String currentPin;   // 현재 PIN
    private String newPin;       // 새로운 PIN (6자리)
    
    // PIN 유효성 검사
    public boolean isValidCurrentPin() {
        return currentPin != null && currentPin.matches("^\\d{6}$");
    }
    
    public boolean isValidNewPin() {
        return newPin != null && newPin.matches("^\\d{6}$");
    }
    
    public boolean arePinsDifferent() {
        return !currentPin.equals(newPin);
    }
}