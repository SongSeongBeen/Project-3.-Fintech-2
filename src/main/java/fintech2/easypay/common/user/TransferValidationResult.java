package fintech2.easypay.common.user;

import fintech2.easypay.account.entity.Account;
import fintech2.easypay.auth.entity.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 송금 관련 검증 결과
 * 송금자, 송금자 계좌, 수신자 계좌 정보를 포함
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TransferValidationResult {
    
    private final boolean valid;
    private final User sender;
    private final Account senderAccount;  // nullable (기본계좌 사용시)
    private final Account receiverAccount;
    private final String errorCode;
    private final String errorMessage;
    
    /**
     * 성공적인 검증 결과
     */
    public static TransferValidationResult success(User sender, Account senderAccount, Account receiverAccount) {
        return new TransferValidationResult(true, sender, senderAccount, receiverAccount, null, null);
    }
    
    /**
     * 실패한 검증 결과
     */
    public static TransferValidationResult failure(String errorCode, String errorMessage) {
        return new TransferValidationResult(false, null, null, null, errorCode, errorMessage);
    }
    
    /**
     * 수신자 사용자 ID 반환 (receiverAccount에서 추출)
     */
    public Long getReceiverId() {
        return receiverAccount != null ? receiverAccount.getUserId() : null;
    }
}