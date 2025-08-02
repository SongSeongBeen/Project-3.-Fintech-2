package fintech2.easypay.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PinResponse {
    private boolean success;      // 성공 여부
    private String message;       // 응답 메시지
    private boolean hasPinSet;    // PIN 설정 여부
    private boolean isPinLocked;  // PIN 잠금 여부
    private Integer remainingAttempts; // 남은 시도 횟수
    private String lockReason;    // 잠금 사유
    private String sessionToken; // PIN 인증 후 발급되는 임시 세션 토큰 (선택적)
    
    // 성공 응답 생성
    public static PinResponse success(String message) {
        return PinResponse.builder()
                .success(true)
                .message(message)
                .build();
    }
    
    public static PinResponse success(String message, String sessionToken) {
        return PinResponse.builder()
                .success(true)
                .message(message)
                .sessionToken(sessionToken)
                .build();
    }
    
    // 실패 응답 생성
    public static PinResponse failure(String message) {
        return PinResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
    
    public static PinResponse failure(String message, Integer remainingAttempts) {
        return PinResponse.builder()
                .success(false)
                .message(message)
                .remainingAttempts(remainingAttempts)
                .build();
    }
    
    // PIN 잠금 응답 생성
    public static PinResponse locked(String lockReason) {
        return PinResponse.builder()
                .success(false)
                .message("PIN이 잠금되었습니다.")
                .isPinLocked(true)
                .lockReason(lockReason)
                .build();
    }
}