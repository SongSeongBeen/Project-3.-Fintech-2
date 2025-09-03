package fintech2.easypay.common.user;

import fintech2.easypay.account.entity.Account;
import fintech2.easypay.account.repository.AccountRepository;
import fintech2.easypay.auth.entity.User;
import fintech2.easypay.auth.repository.UserRepository;
import fintech2.easypay.common.enums.AccountStatus;
import fintech2.easypay.common.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 사용자 및 계좌 검증 서비스
 * 사용자와 계좌의 존재 여부, 상태, 소유권 등을 통합 검증
 * 캐싱을 통한 성능 최적화
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserAccountValidationService {
    
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    
    /**
     * 휴대폰 번호로 사용자 검증 (캐시 적용)
     */
    @Cacheable(value = "user-validation", key = "#phoneNumber")
    public UserValidationResult validateUser(String phoneNumber) {
        log.debug("사용자 검증 시작: {}", phoneNumber);
        
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return UserValidationResult.failure("INVALID_PHONE", "휴대폰 번호를 입력해주세요");
        }
        
        Optional<User> userOpt = userRepository.findByPhoneNumber(phoneNumber);
        if (userOpt.isEmpty()) {
            return UserValidationResult.failure("USER_NOT_FOUND", "등록되지 않은 사용자입니다");
        }
        
        User user = userOpt.get();
        
        // 계정 잠금 상태 확인
        if (user.isLocked()) {
            return UserValidationResult.failure("USER_LOCKED", "잠금된 계정입니다");
        }
        
        log.debug("사용자 검증 성공: {} (ID: {})", phoneNumber, user.getId());
        return UserValidationResult.success(user);
    }
    
    /**
     * 사용자 ID로 사용자 검증 (캐시 적용)
     */
    @Cacheable(value = "user-validation", key = "'id:' + #userId")
    public UserValidationResult validateUser(Long userId) {
        log.debug("사용자 ID 검증 시작: {}", userId);
        
        if (userId == null) {
            return UserValidationResult.failure("INVALID_USER_ID", "사용자 ID를 입력해주세요");
        }
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return UserValidationResult.failure("USER_NOT_FOUND", "등록되지 않은 사용자입니다");
        }
        
        User user = userOpt.get();
        
        // 계정 잠금 상태 확인 (동일한 로직)
        if (user.isLocked()) {
            return UserValidationResult.failure("USER_LOCKED", "잠금된 계정입니다");
        }
        
        log.debug("사용자 ID 검증 성공: {}", userId);
        return UserValidationResult.success(user);
    }
    
    /**
     * 계좌 번호로 계좌 검증 (캐시 적용)
     */
    @Cacheable(value = "account-validation", key = "#accountNumber")
    public AccountValidationResult validateAccount(String accountNumber) {
        log.debug("계좌 검증 시작: {}", accountNumber);
        
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return AccountValidationResult.failure("INVALID_ACCOUNT", "계좌번호를 입력해주세요");
        }
        
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty()) {
            return AccountValidationResult.failure("ACCOUNT_NOT_FOUND", "존재하지 않는 계좌입니다");
        }
        
        Account account = accountOpt.get();
        
        // 계좌 상태 검증
        if (account.getStatus() == AccountStatus.INACTIVE) {
            return AccountValidationResult.failure("ACCOUNT_INACTIVE", "비활성화된 계좌입니다");
        }
        
        log.debug("계좌 검증 성공: {} (ID: {})", accountNumber, account.getId());
        return AccountValidationResult.success(account);
    }
    
    /**
     * 계좌 소유권 검증 (사용자가 해당 계좌를 소유하고 있는지)
     */
    @Cacheable(value = "account-ownership", key = "#accountNumber + ':' + #userId")
    public AccountValidationResult validateAccountOwnership(String accountNumber, Long userId) {
        log.debug("계좌 소유권 검증 시작: {} (사용자: {})", accountNumber, userId);
        
        // 먼저 계좌 자체 검증
        AccountValidationResult accountResult = validateAccount(accountNumber);
        if (!accountResult.isValid()) {
            return accountResult;
        }
        
        Account account = accountResult.getAccount();
        
        // 소유권 검증
        if (!account.getUserId().equals(userId)) {
            return AccountValidationResult.failure("ACCOUNT_NOT_OWNED", "본인의 계좌가 아닙니다");
        }
        
        log.debug("계좌 소유권 검증 성공: {} (사용자: {})", accountNumber, userId);
        return AccountValidationResult.success(account);
    }
    
    /**
     * 사용자와 계좌를 함께 검증
     */
    public CombinedValidationResult validateUserAndAccount(String phoneNumber, String accountNumber) {
        UserValidationResult userResult = validateUser(phoneNumber);
        if (!userResult.isValid()) {
            return CombinedValidationResult.failure(userResult.getErrorCode(), userResult.getErrorMessage());
        }
        
        AccountValidationResult accountResult = validateAccountOwnership(accountNumber, userResult.getUserId());
        if (!accountResult.isValid()) {
            return CombinedValidationResult.failure(accountResult.getErrorCode(), accountResult.getErrorMessage());
        }
        
        return CombinedValidationResult.success(userResult.getUser(), accountResult.getAccount());
    }
    
    /**
     * 송금자와 수신자 계좌 검증
     */
    public TransferValidationResult validateTransferAccounts(String senderPhone, String senderAccountNumber, String receiverAccountNumber) {
        // 송금자 검증
        UserValidationResult senderResult = validateUser(senderPhone);
        if (!senderResult.isValid()) {
            return TransferValidationResult.failure(senderResult.getErrorCode(), senderResult.getErrorMessage());
        }
        
        // 송금자 계좌 소유권 검증 (계좌번호가 제공된 경우)
        AccountValidationResult senderAccountResult = null;
        if (senderAccountNumber != null && !senderAccountNumber.trim().isEmpty()) {
            senderAccountResult = validateAccountOwnership(senderAccountNumber, senderResult.getUserId());
            if (!senderAccountResult.isValid()) {
                return TransferValidationResult.failure(senderAccountResult.getErrorCode(), senderAccountResult.getErrorMessage());
            }
        }
        
        // 수신자 계좌 검증
        AccountValidationResult receiverAccountResult = validateAccount(receiverAccountNumber);
        if (!receiverAccountResult.isValid()) {
            return TransferValidationResult.failure(receiverAccountResult.getErrorCode(), receiverAccountResult.getErrorMessage());
        }
        
        // 자기 자신에게 송금 방지
        if (senderAccountResult != null && 
            senderAccountResult.getAccount().getId().equals(receiverAccountResult.getAccount().getId())) {
            return TransferValidationResult.failure("SAME_ACCOUNT_TRANSFER", "본인 계좌로는 송금할 수 없습니다");
        }
        
        return TransferValidationResult.success(
            senderResult.getUser(), 
            senderAccountResult != null ? senderAccountResult.getAccount() : null,
            receiverAccountResult.getAccount()
        );
    }
}