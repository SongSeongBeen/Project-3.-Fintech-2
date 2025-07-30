package fintech2.easypay.payment.service;

import fintech2.easypay.account.entity.Account;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 결제 모듈에서 계좌 관련 기능을 위한 추상화 인터페이스
 * 실제 AccountRepository 의존성을 제거하고 테스트 가능하도록 분리
 */
public interface PaymentAccountService { // AccountService -> PaymentAccountService
    
    /**
     * 회원 ID로 계좌 조회
     */
    Optional<Account> findByUserId(Long userId); // findByMemberId -> findByUserId, VirtualAccount -> Account
    
    /**
     * 계좌번호로 계좌 조회
     */
    Optional<Account> findByAccountNumber(String accountNumber); // VirtualAccount -> Account
    
    /**
     * 잔액 출금 (결제)
     */
    void withdraw(Account account, BigDecimal amount); // VirtualAccount -> Account
    
    /**
     * 잔액 입금 (환불)
     */
    void deposit(Account account, BigDecimal amount); // VirtualAccount -> Account
    
    /**
     * 계좌 저장
     */
    Account save(Account account); // VirtualAccount -> Account
}