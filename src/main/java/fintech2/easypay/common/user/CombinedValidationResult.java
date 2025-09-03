package fintech2.easypay.common.user;

import fintech2.easypay.account.entity.Account;
import fintech2.easypay.auth.entity.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사용자와 계좌를 함께 검증한 결과
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CombinedValidationResult {
    
    private final boolean valid;
    private final User user;
    private final Account account;
    private final String errorCode;
    private final String errorMessage;
    
    /**
     * 성공적인 검증 결과
     */
    public static CombinedValidationResult success(User user, Account account) {
        return new CombinedValidationResult(true, user, account, null, null);
    }
    
    /**
     * 실패한 검증 결과
     */
    public static CombinedValidationResult failure(String errorCode, String errorMessage) {
        return new CombinedValidationResult(false, null, null, errorCode, errorMessage);
    }
}