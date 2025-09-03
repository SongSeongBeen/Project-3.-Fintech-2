package fintech2.easypay.common.validation;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 검증 결과를 담는 클래스
 * 검증 성공/실패 여부와 오류 메시지들을 포함
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationResult {
    
    private final boolean valid;
    private final List<String> errorMessages;
    private final String errorCode;
    
    /**
     * 성공적인 검증 결과 생성
     */
    public static ValidationResult success() {
        return new ValidationResult(true, new ArrayList<>(), null);
    }
    
    /**
     * 실패한 검증 결과 생성 (단일 오류)
     */
    public static ValidationResult failure(String errorCode, String errorMessage) {
        List<String> messages = new ArrayList<>();
        messages.add(errorMessage);
        return new ValidationResult(false, messages, errorCode);
    }
    
    /**
     * 실패한 검증 결과 생성 (여러 오류)
     */
    public static ValidationResult failure(String errorCode, List<String> errorMessages) {
        return new ValidationResult(false, new ArrayList<>(errorMessages), errorCode);
    }
    
    /**
     * 첫 번째 오류 메시지 반환
     */
    public String getFirstErrorMessage() {
        return errorMessages.isEmpty() ? null : errorMessages.get(0);
    }
    
    /**
     * 모든 오류 메시지를 하나의 문자열로 결합
     */
    public String getAllErrorMessages() {
        return String.join(", ", errorMessages);
    }
    
    /**
     * 다른 검증 결과와 합치기
     * 둘 중 하나라도 실패하면 실패 결과 반환
     */
    public ValidationResult combine(ValidationResult other) {
        if (this.valid && other.valid) {
            return success();
        }
        
        List<String> combinedMessages = new ArrayList<>(this.errorMessages);
        combinedMessages.addAll(other.errorMessages);
        
        String combinedErrorCode = this.errorCode != null ? this.errorCode : other.errorCode;
        return failure(combinedErrorCode, combinedMessages);
    }
}