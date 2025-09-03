package fintech2.easypay.common.user;

import fintech2.easypay.auth.entity.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 검증 결과
 * 사용자 존재 여부, 계정 상태, 사용자 정보를 포함
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UserValidationResult {
    
    private final boolean valid;
    private final User user;
    private final String errorCode;
    private final String errorMessage;
    
    /**
     * 성공적인 사용자 검증 결과
     */
    public static UserValidationResult success(User user) {
        return new UserValidationResult(true, user, null, null);
    }
    
    /**
     * 실패한 사용자 검증 결과
     */
    public static UserValidationResult failure(String errorCode, String errorMessage) {
        return new UserValidationResult(false, null, errorCode, errorMessage);
    }
    
    /**
     * 사용자 ID 반환 (검증 성공 시)
     */
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }
    
    /**
     * 휴대폰 번호 반환 (검증 성공 시)
     */
    public String getPhoneNumber() {
        return user != null ? user.getPhoneNumber() : null;
    }
    
    /**
     * 사용자 이름 반환 (검증 성공 시)
     */
    public String getName() {
        return user != null ? user.getName() : null;
    }
}