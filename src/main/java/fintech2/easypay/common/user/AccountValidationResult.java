package fintech2.easypay.common.user;

import fintech2.easypay.account.entity.Account;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 계좌 검증 결과
 * 계좌 존재 여부, 소유권, 계좌 상태, 계좌 정보를 포함
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountValidationResult {
    
    private final boolean valid;
    private final Account account;
    private final String errorCode;
    private final String errorMessage;
    
    /**
     * 성공적인 계좌 검증 결과
     */
    public static AccountValidationResult success(Account account) {
        return new AccountValidationResult(true, account, null, null);
    }
    
    /**
     * 실패한 계좌 검증 결과
     */
    public static AccountValidationResult failure(String errorCode, String errorMessage) {
        return new AccountValidationResult(false, null, errorCode, errorMessage);
    }
    
    /**
     * 계좌 ID 반환 (검증 성공 시)
     */
    public Long getAccountId() {
        return account != null ? account.getId() : null;
    }
    
    /**
     * 계좌 번호 반환 (검증 성공 시)
     */
    public String getAccountNumber() {
        return account != null ? account.getAccountNumber() : null;
    }
    
    /**
     * 사용자 ID 반환 (검증 성공 시)
     */
    public Long getUserId() {
        return account != null ? account.getUserId() : null;
    }
}