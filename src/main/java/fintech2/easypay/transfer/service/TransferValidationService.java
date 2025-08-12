package fintech2.easypay.transfer.service;

import fintech2.easypay.account.entity.Account;
import fintech2.easypay.account.entity.UserAccount;
import fintech2.easypay.account.repository.AccountRepository;
import fintech2.easypay.account.service.UserAccountService;
import fintech2.easypay.auth.entity.User;
import fintech2.easypay.auth.repository.UserRepository;
import fintech2.easypay.common.BusinessException;
import fintech2.easypay.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 송금 검증 서비스
 * 송금 관련 유효성 검증 및 계좌 조회 로직을 담당
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TransferValidationService {
    
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final UserAccountService userAccountService;
    
    /**
     * 송금 참여자 정보를 조회하고 검증
     */
    public TransferParticipants validateAndGetParticipants(String senderPhoneNumber, String receiverAccountNumber) {
        // 송금자 조회
        User sender = findUserByPhoneNumber(senderPhoneNumber);
        
        // 수신자 계좌 및 사용자 조회
        Account receiverAccount = findAccountByNumber(receiverAccountNumber);
        User receiver = findUserById(receiverAccount.getUserId());
        
        // 자기 자신에게 송금 방지
        validateNotSelfTransfer(sender, receiver);
        
        return new TransferParticipants(sender, receiver, receiverAccount);
    }
    
    /**
     * 송금자 계좌 조회 및 검증
     */
    public Account getSenderAccount(User sender, String requestedAccountNumber) {
        if (hasRequestedAccountNumber(requestedAccountNumber)) {
            return getSpecificAccount(sender, requestedAccountNumber);
        } else {
            return getPrimaryAccount(sender);
        }
    }
    
    /**
     * 데드락 방지를 위한 순서화된 계좌 락 획득
     */
    public LockedAccounts acquireAccountLocks(Account senderAccount, Account receiverAccount) {
        // ID 순서로 락 획득하여 데드락 방지
        if (shouldLockSenderFirst(senderAccount, receiverAccount)) {
            Account senderLocked = lockAccount(senderAccount.getId(), ErrorCode.ACCOUNT_NOT_FOUND);
            Account receiverLocked = lockAccount(receiverAccount.getId(), ErrorCode.INVALID_ACCOUNT_NUMBER);
            return new LockedAccounts(senderLocked, receiverLocked);
        } else {
            Account receiverLocked = lockAccount(receiverAccount.getId(), ErrorCode.INVALID_ACCOUNT_NUMBER);
            Account senderLocked = lockAccount(senderAccount.getId(), ErrorCode.ACCOUNT_NOT_FOUND);
            return new LockedAccounts(senderLocked, receiverLocked);
        }
    }
    
    // Private helper methods
    
    private User findUserByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }
    
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }
    
    private Account findAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_ACCOUNT_NUMBER));
    }
    
    private void validateNotSelfTransfer(User sender, User receiver) {
        if (sender.getId().equals(receiver.getId())) {
            throw new BusinessException(ErrorCode.SAME_ACCOUNT_TRANSFER);
        }
    }
    
    private boolean hasRequestedAccountNumber(String accountNumber) {
        return accountNumber != null && !accountNumber.trim().isEmpty();
    }
    
    private Account getSpecificAccount(User sender, String accountNumber) {
        Account account = findAccountByNumber(accountNumber);
        validateAccountOwnership(account, sender);
        return account;
    }
    
    private void validateAccountOwnership(Account account, User owner) {
        if (!account.getUserId().equals(owner.getId())) {
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND, "본인 계좌가 아닙니다.");
        }
    }
    
    private Account getPrimaryAccount(User sender) {
        UserAccount primaryUserAccount = userAccountService.getPrimaryAccount(sender.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND, "기본 계좌를 찾을 수 없습니다."));
        
        return findAccountByNumber(primaryUserAccount.getAccountNumber());
    }
    
    private boolean shouldLockSenderFirst(Account senderAccount, Account receiverAccount) {
        return senderAccount.getId().compareTo(receiverAccount.getId()) < 0;
    }
    
    private Account lockAccount(Long accountId, ErrorCode errorCode) {
        return accountRepository.findByIdWithLock(accountId)
                .orElseThrow(() -> new BusinessException(errorCode));
    }
    
    /**
     * 송금 참여자 정보 DTO
     */
    public record TransferParticipants(
            User sender,
            User receiver,
            Account receiverAccount
    ) {}
    
    /**
     * 락이 적용된 계좌 정보 DTO
     */
    public record LockedAccounts(
            Account senderAccount,
            Account receiverAccount
    ) {}
}