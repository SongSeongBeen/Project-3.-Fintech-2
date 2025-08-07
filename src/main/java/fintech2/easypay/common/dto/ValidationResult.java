package fintech2.easypay.common.dto;

import java.util.List;
import java.util.Map;

/**
 * Java 21: Record for validation results
 * 
 * @param isValid 검증 성공 여부
 * @param errors 검증 실패 시 에러 목록
 * @param warnings 경고 메시지 목록
 */
public record ValidationResult(
    boolean isValid,
    List<ValidationError> errors,
    List<String> warnings
) {
    public static ValidationResult success() {
        return new ValidationResult(true, List.of(), List.of());
    }
    
    public static ValidationResult success(List<String> warnings) {
        return new ValidationResult(true, List.of(), warnings);
    }
    
    public static ValidationResult failure(List<ValidationError> errors) {
        return new ValidationResult(false, errors, List.of());
    }
    
    public static ValidationResult failure(String field, String message) {
        return failure(List.of(new ValidationError(field, message)));
    }
    
    public static ValidationResult failure(String field, String message, String code) {
        return failure(List.of(new ValidationError(field, message, code)));
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
    
    public int getErrorCount() {
        return errors.size();
    }
    
    public int getWarningCount() {
        return warnings.size();
    }
    
    /**
     * Java 21: Record for validation errors
     * 
     * @param field 검증 실패한 필드명
     * @param message 에러 메시지
     * @param code 에러 코드
     */
    public record ValidationError(
        String field,
        String message,
        String code
    ) {
        public ValidationError(String field, String message) {
            this(field, message, "VALIDATION_ERROR");
        }
        
        public Map<String, String> toMap() {
            return Map.of(
                "field", field,
                "message", message,
                "code", code
            );
        }
    }
} 