package fintech2.easypay.config;

import fintech2.easypay.account.entity.Account;
import fintech2.easypay.account.entity.AccountBalance;
import fintech2.easypay.account.repository.AccountRepository;
import fintech2.easypay.account.repository.AccountBalanceRepository;
import fintech2.easypay.auth.entity.User;
import fintech2.easypay.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final AccountBalanceRepository accountBalanceRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("애플리케이션 초기 데이터 생성 시작...");
        
        // 테스트 사용자1이 이미 존재하는지 확인
        if (userRepository.findByPhoneNumber("01012345678").isEmpty()) {
            createTestUser1();
        } else {
            log.info("테스트 사용자1이 이미 존재합니다.");
        }
        
        // 테스트 사용자2가 이미 존재하는지 확인
        if (userRepository.findByPhoneNumber("01098765432").isEmpty()) {
            createTestUser2();
        } else {
            log.info("테스트 사용자2가 이미 존재합니다.");
        }
        
        // 테스트 사용자3 (PIN 없음) - 신규 가입 테스트용
        if (userRepository.findByPhoneNumber("01012341234").isEmpty()) {
            createTestUser3();
        } else {
            log.info("테스트 사용자3이 이미 존재합니다.");
        }
        
        log.info("애플리케이션 초기 데이터 생성 완료!");
    }

    private void createTestUser1() {
        try {
            log.info("테스트 사용자1 생성 중...");
            
            // 사용자 생성
            User user1 = User.builder()
                    .phoneNumber("01012345678")
                    .password(passwordEncoder.encode("123456"))
                    .name("테스트사용자1")
                    .email("test1@example.com")
                    .createdAt(LocalDateTime.now())
                    .transferPin(passwordEncoder.encode("123456")) // 테스트용 기본 PIN
                    .pinCreatedAt(LocalDateTime.now())
                    .build();
            user1 = userRepository.save(user1);
            
            // 계좌번호 생성 (EasyPay 가상계좌)
            String accountNumber1 = "EP" + String.format("%010d", user1.getId());
            
            // Account 엔티티 생성
            Account account1 = Account.builder()
                    .accountNumber(accountNumber1)
                    .userId(user1.getId())
                    .balance(new BigDecimal("1000000")) // 초기 잔액 100만원
                    .createdAt(LocalDateTime.now())
                    .build();
            accountRepository.save(account1);
            
            // AccountBalance 엔티티 생성 (BalanceService에서 사용)
            AccountBalance accountBalance1 = AccountBalance.builder()
                    .accountNumber(accountNumber1)
                    .balance(new BigDecimal("1000000"))
                    .build();
            accountBalanceRepository.save(accountBalance1);
            
            // User 엔티티에 계좌번호 설정
            user1.setAccountNumber(accountNumber1);
            userRepository.save(user1);
            
            log.info("테스트 사용자1 생성 완료: 전화번호={}, 계좌번호={}, 초기잔액=1,000,000원", 
                    user1.getPhoneNumber(), accountNumber1);
                    
        } catch (Exception e) {
            log.error("테스트 사용자1 생성 실패: {}", e.getMessage(), e);
        }
    }

    private void createTestUser2() {
        try {
            log.info("테스트 사용자2 생성 중...");
            
            // 사용자 생성
            User user2 = User.builder()
                    .phoneNumber("01098765432")
                    .password(passwordEncoder.encode("123456"))
                    .name("테스트사용자2")
                    .email("test2@example.com")
                    .createdAt(LocalDateTime.now())
                    .transferPin(passwordEncoder.encode("123456")) // 테스트용 기본 PIN
                    .pinCreatedAt(LocalDateTime.now())
                    .build();
            user2 = userRepository.save(user2);
            
            // 계좌번호 생성 (EasyPay 가상계좌)
            String accountNumber2 = "EP" + String.format("%010d", user2.getId());
            
            // Account 엔티티 생성
            Account account2 = Account.builder()
                    .accountNumber(accountNumber2)
                    .userId(user2.getId())
                    .balance(new BigDecimal("500000")) // 초기 잔액 50만원
                    .createdAt(LocalDateTime.now())
                    .build();
            accountRepository.save(account2);
            
            // AccountBalance 엔티티 생성 (BalanceService에서 사용)
            AccountBalance accountBalance2 = AccountBalance.builder()
                    .accountNumber(accountNumber2)
                    .balance(new BigDecimal("500000"))
                    .build();
            accountBalanceRepository.save(accountBalance2);
            
            // User 엔티티에 계좌번호 설정
            user2.setAccountNumber(accountNumber2);
            userRepository.save(user2);
            
            log.info("테스트 사용자2 생성 완료: 전화번호={}, 계좌번호={}, 초기잔액=500,000원", 
                    user2.getPhoneNumber(), accountNumber2);
                    
        } catch (Exception e) {
            log.error("테스트 사용자2 생성 실패: {}", e.getMessage(), e);
        }
    }

    private void createTestUser3() {
        try {
            log.info("테스트 사용자3 생성 중... (PIN 없음)");
            
            // 사용자 생성 (PIN 없음)
            User user3 = User.builder()
                    .phoneNumber("01012341234")
                    .password(passwordEncoder.encode("test1234"))
                    .name("테스트사용자3")
                    .email("test3@example.com")
                    .createdAt(LocalDateTime.now())
                    .transferPin(null) // PIN 없음
                    .pinCreatedAt(null) // PIN 생성일 없음
                    .build();
            user3 = userRepository.save(user3);
            
            // 계좌번호 생성 (EasyPay 가상계좌)
            String accountNumber3 = "EP" + String.format("%010d", user3.getId());
            
            // Account 엔티티 생성
            Account account3 = Account.builder()
                    .accountNumber(accountNumber3)
                    .userId(user3.getId())
                    .balance(new BigDecimal("200000")) // 초기 잔액 20만원
                    .createdAt(LocalDateTime.now())
                    .build();
            accountRepository.save(account3);
            
            // AccountBalance 엔티티 생성 (BalanceService에서 사용)
            AccountBalance accountBalance3 = AccountBalance.builder()
                    .accountNumber(accountNumber3)
                    .balance(new BigDecimal("200000"))
                    .build();
            accountBalanceRepository.save(accountBalance3);
            
            // User 엔티티에 계좌번호 설정
            user3.setAccountNumber(accountNumber3);
            userRepository.save(user3);
            
            log.info("테스트 사용자3 생성 완료: 전화번호={}, 계좌번호={}, 초기잔액=200,000원 (PIN 없음)", 
                    user3.getPhoneNumber(), accountNumber3);
                    
        } catch (Exception e) {
            log.error("테스트 사용자3 생성 실패: {}", e.getMessage(), e);
        }
    }
}