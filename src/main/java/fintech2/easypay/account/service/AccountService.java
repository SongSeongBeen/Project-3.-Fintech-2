package fintech2.easypay.account.service;

import fintech2.easypay.account.dto.AccountResponse;
import fintech2.easypay.account.dto.DepositRequest;
import fintech2.easypay.account.dto.WithdrawRequest;
import fintech2.easypay.account.entity.Account;
import fintech2.easypay.account.repository.AccountRepository;
import fintech2.easypay.common.BusinessException;
import fintech2.easypay.common.ErrorCode;
import fintech2.easypay.member.entity.Member;
import fintech2.easypay.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 계좌 관리 서비스
 * 가상계좌 생성, 조회, 입출금 처리 기능을 제공
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AccountService {
    
    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;
    
    /**
     * 새로운 계좌 생성
     * 회원당 하나의 계좌만 생성 가능
     * @param phoneNumber 회원 휴대폰 번호
     * @return 생성된 계좌 정보
     * @throws BusinessException 회원을 찾을 수 없거나 이미 계좌가 존재하는 경우
     */
    @Transactional
    public AccountResponse createAccount(String phoneNumber) {
        Member member = memberRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        
        // 이미 계좌가 있는지 확인
        if (accountRepository.findByMember(member).isPresent()) {
            throw new BusinessException(ErrorCode.ACCOUNT_CREATION_FAILED, "이미 계좌가 존재합니다.");
        }
        
        // 계좌번호 생성
        String accountNumber = generateAccountNumber();
        
        // 계좌 생성 (초기 테스트 잔액 100만원)
        Account account = Account.builder()
                .accountNumber(accountNumber)
                .member(member)
                .balance(BigDecimal.valueOf(1000000))
                .build();
        
        Account savedAccount = accountRepository.save(account);
        
        log.info("계좌 생성 완료: {} -> {}", phoneNumber, accountNumber);
        
        return AccountResponse.from(savedAccount);
    }
    
    /**
     * 회원의 계좌 정보 조회
     * @param phoneNumber 회원 휴대폰 번호
     * @return 계좌 정보
     * @throws BusinessException 회원이나 계좌을 찾을 수 없는 경우
     */
    public AccountResponse getAccount(String phoneNumber) {
        Member member = memberRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        
        Account account = accountRepository.findByMember(member)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));
        
        return AccountResponse.from(account);
    }
    
    public AccountResponse getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));
        
        return AccountResponse.from(account);
    }
    
    /**
     * 계좌 입금 처리
     * 낙관적 락을 사용하여 동시성 제어
     * @param phoneNumber 회원 휴대폰 번호
     * @param request 입금 요청 정보
     * @return 업데이트된 계좌 정보
     * @throws BusinessException 회원이나 계좌을 찾을 수 없는 경우
     */
    @Transactional
    public AccountResponse deposit(String phoneNumber, DepositRequest request) {
        Member member = memberRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        
        Account account = accountRepository.findByMemberIdWithLock(member.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));
        
        account.deposit(request.getAmount());
        
        log.info("입금 완료: {} -> {} (잔액: {})", phoneNumber, request.getAmount(), account.getBalance());
        
        return AccountResponse.from(account);
    }
    
    @Transactional
    public AccountResponse withdraw(String phoneNumber, WithdrawRequest request) {
        Member member = memberRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        
        Account account = accountRepository.findByMemberIdWithLock(member.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));
        
        if (!account.hasEnoughBalance(request.getAmount())) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE);
        }
        
        account.withdraw(request.getAmount());
        
        log.info("출금 완료: {} -> {} (잔액: {})", phoneNumber, request.getAmount(), account.getBalance());
        
        return AccountResponse.from(account);
    }
    
    /**
     * 계좌 잔액 초기화 (테스트용)
     * 잔액을 100만원으로 재설정
     * @param phoneNumber 회원 휴대폰 번호
     * @throws BusinessException 회원이나 계좌을 찾을 수 없는 경우
     */
    @Transactional
    public void resetBalance(String phoneNumber) {
        Member member = memberRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        
        Account account = accountRepository.findByMemberIdWithLock(member.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));
        
        // 현재 잔액을 고려하여 100만원으로 조정
        BigDecimal targetBalance = BigDecimal.valueOf(1000000);
        BigDecimal currentBalance = account.getBalance();
        BigDecimal difference = targetBalance.subtract(currentBalance);
        
        if (difference.compareTo(BigDecimal.ZERO) != 0) {
            if (difference.compareTo(BigDecimal.ZERO) > 0) {
                account.deposit(difference);
            } else {
                account.withdraw(difference.negate());
            }
        }
        
        log.info("잔액 초기화 완료: {} -> 1,000,000원", phoneNumber);
    }
    
    /**
     * 고유한 계좌번호 생성
     * EP 접두어 + 10자리 랜덤 문자열 형식
     * @return 생성된 계좌번호
     */
    private String generateAccountNumber() {
        String accountNumber;
        do {
            accountNumber = "EP" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        } while (accountRepository.existsByAccountNumber(accountNumber));
        
        return accountNumber;
    }
}
