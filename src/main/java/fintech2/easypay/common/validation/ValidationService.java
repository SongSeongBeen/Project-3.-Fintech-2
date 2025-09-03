package fintech2.easypay.common.validation;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * 공통 검증 서비스
 * 애플리케이션 전반에서 사용되는 검증 로직을 통합 관리
 */
@Service
public class ValidationService {
    
    // 정규식 패턴들
    private static final Pattern PHONE_PATTERN = Pattern.compile("^010-?\\d{4}-?\\d{4}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("^[A-Z]{2}\\d{10}$"); // VA1234567890 형식
    private static final Pattern NAME_PATTERN = Pattern.compile("^[가-힣a-zA-Z\\s]{2,20}$");
    private static final Pattern PIN_PATTERN = Pattern.compile("^\\d{6}$");
    
    // 상수들
    private static final BigDecimal MIN_TRANSFER_AMOUNT = new BigDecimal("1000");
    private static final BigDecimal MAX_TRANSFER_AMOUNT = new BigDecimal("10000000");
    private static final BigDecimal MIN_PAYMENT_AMOUNT = new BigDecimal("100");
    private static final BigDecimal MAX_PAYMENT_AMOUNT = new BigDecimal("5000000");
    
    /**
     * 휴대폰 번호 검증
     */
    public ValidationResult validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return ValidationResult.failure("INVALID_PHONE", "휴대폰 번호를 입력해주세요");
        }
        
        String normalizedPhone = phoneNumber.replaceAll("-", "");
        if (!PHONE_PATTERN.matcher(normalizedPhone).matches()) {
            return ValidationResult.failure("INVALID_PHONE", "올바른 휴대폰 번호 형식이 아닙니다 (010-1234-5678)");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * 이메일 검증
     */
    public ValidationResult validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return ValidationResult.failure("INVALID_EMAIL", "이메일을 입력해주세요");
        }
        
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            return ValidationResult.failure("INVALID_EMAIL", "올바른 이메일 형식이 아닙니다");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * 계좌번호 검증
     */
    public ValidationResult validateAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return ValidationResult.failure("INVALID_ACCOUNT", "계좌번호를 입력해주세요");
        }
        
        if (!ACCOUNT_NUMBER_PATTERN.matcher(accountNumber.trim()).matches()) {
            return ValidationResult.failure("INVALID_ACCOUNT", "올바른 계좌번호 형식이 아닙니다 (VA1234567890)");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * 이름 검증
     */
    public ValidationResult validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return ValidationResult.failure("INVALID_NAME", "이름을 입력해주세요");
        }
        
        if (!NAME_PATTERN.matcher(name.trim()).matches()) {
            return ValidationResult.failure("INVALID_NAME", "이름은 2-20자의 한글, 영문만 입력 가능합니다");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * PIN 번호 검증
     */
    public ValidationResult validatePin(String pin) {
        if (pin == null || pin.trim().isEmpty()) {
            return ValidationResult.failure("INVALID_PIN", "PIN 번호를 입력해주세요");
        }
        
        if (!PIN_PATTERN.matcher(pin.trim()).matches()) {
            return ValidationResult.failure("INVALID_PIN", "PIN 번호는 6자리 숫자여야 합니다");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * 송금 금액 검증
     */
    public ValidationResult validateTransferAmount(BigDecimal amount) {
        if (amount == null) {
            return ValidationResult.failure("INVALID_AMOUNT", "송금 금액을 입력해주세요");
        }
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ValidationResult.failure("INVALID_AMOUNT", "송금 금액은 0원보다 커야 합니다");
        }
        
        if (amount.compareTo(MIN_TRANSFER_AMOUNT) < 0) {
            return ValidationResult.failure("INVALID_AMOUNT", 
                String.format("송금 최소 금액은 %,d원입니다", MIN_TRANSFER_AMOUNT.intValue()));
        }
        
        if (amount.compareTo(MAX_TRANSFER_AMOUNT) > 0) {
            return ValidationResult.failure("INVALID_AMOUNT", 
                String.format("송금 최대 금액은 %,d원입니다", MAX_TRANSFER_AMOUNT.intValue()));
        }
        
        return ValidationResult.success();
    }
    
    /**
     * 결제 금액 검증
     */
    public ValidationResult validatePaymentAmount(BigDecimal amount) {
        if (amount == null) {
            return ValidationResult.failure("INVALID_AMOUNT", "결제 금액을 입력해주세요");
        }
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ValidationResult.failure("INVALID_AMOUNT", "결제 금액은 0원보다 커야 합니다");
        }
        
        if (amount.compareTo(MIN_PAYMENT_AMOUNT) < 0) {
            return ValidationResult.failure("INVALID_AMOUNT", 
                String.format("결제 최소 금액은 %,d원입니다", MIN_PAYMENT_AMOUNT.intValue()));
        }
        
        if (amount.compareTo(MAX_PAYMENT_AMOUNT) > 0) {
            return ValidationResult.failure("INVALID_AMOUNT", 
                String.format("결제 최대 금액은 %,d원입니다", MAX_PAYMENT_AMOUNT.intValue()));
        }
        
        return ValidationResult.success();
    }
    
    /**
     * 메모 검증
     */
    public ValidationResult validateMemo(String memo) {
        if (memo != null && memo.length() > 100) {
            return ValidationResult.failure("INVALID_MEMO", "메모는 100자 이내로 입력해주세요");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * 비밀번호 강도 검증
     */
    public ValidationResult validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return ValidationResult.failure("INVALID_PASSWORD", "비밀번호를 입력해주세요");
        }
        
        if (password.length() < 8) {
            return ValidationResult.failure("INVALID_PASSWORD", "비밀번호는 8자리 이상이어야 합니다");
        }
        
        if (password.length() > 20) {
            return ValidationResult.failure("INVALID_PASSWORD", "비밀번호는 20자리 이하여야 합니다");
        }
        
        // 영문, 숫자, 특수문자 중 2가지 이상 포함 검사
        int typeCount = 0;
        if (password.matches(".*[a-zA-Z].*")) typeCount++;
        if (password.matches(".*\\d.*")) typeCount++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\?].*")) typeCount++;
        
        if (typeCount < 2) {
            return ValidationResult.failure("INVALID_PASSWORD", 
                "비밀번호는 영문, 숫자, 특수문자 중 2가지 이상을 포함해야 합니다");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * 여러 검증 결과를 하나로 결합
     */
    public ValidationResult combineResults(ValidationResult... results) {
        ValidationResult combined = ValidationResult.success();
        for (ValidationResult result : results) {
            combined = combined.combine(result);
        }
        return combined;
    }
}