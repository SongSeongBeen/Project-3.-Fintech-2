package fintech2.easypay.payment.service.impl;

import fintech2.easypay.account.entity.Account;
import fintech2.easypay.account.repository.AccountRepository;
import fintech2.easypay.payment.service.PaymentAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 결제 모듈용 계좌 서비스 구현체
 * 실제 AccountRepository를 사용하여 계좌 관련 기능 제공
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements PaymentAccountService {
    
    private final AccountRepository accountRepository;
    
    @Override
    public Optional<Account> findByUserId(Long userId) {
        return accountRepository.findByUserId(userId);
    }
    
    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }
    
    @Override
    @Transactional
    public void withdraw(Account account, BigDecimal amount) {
        account.withdraw(amount); // Account 엔티티의 withdraw 메소드 사용
        accountRepository.save(account);
    }
    
    @Override
    @Transactional
    public void deposit(Account account, BigDecimal amount) {
        account.deposit(amount); // Account 엔티티의 deposit 메소드 사용
        accountRepository.save(account);
    }
    
    @Override
    @Transactional
    public Account save(Account account) {
        return accountRepository.save(account);
    }
}