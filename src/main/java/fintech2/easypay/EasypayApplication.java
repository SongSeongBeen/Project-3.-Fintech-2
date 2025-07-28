package fintech2.easypay;

import fintech2.easypay.account.entity.AccountBalance;
import fintech2.easypay.account.repository.AccountBalanceRepository;
import fintech2.easypay.auth.entity.User;
import fintech2.easypay.auth.repository.UserRepository;
import fintech2.easypay.audit.service.AlarmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

/**
 * 간편 송금 시스템 메인 애플리케이션 클래스
 * Spring Boot 애플리케이션의 진입점 역할
 * JPA Auditing 기능을 활성화하여 엔티티의 생성/수정 시간을 자동으로 관리
 */
@SpringBootApplication
@EnableJpaAuditing
@RequiredArgsConstructor
@Slf4j
public class EasypayApplication {
    
    private final UserRepository userRepository;
    private final AccountBalanceRepository accountBalanceRepository;
    private final PasswordEncoder passwordEncoder;
    private final AlarmService alarmService;
    
    /**
     * 애플리케이션 시작점
     * @param args 커맨드라인 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(EasypayApplication.class, args);
    }
    
    /**
     * 테스트 데이터 생성
     */
    @Bean
    public CommandLineRunner initData() {
        return args -> {
            log.info("Initializing test data...");
            
            // 테스트 사용자 생성
            if (userRepository.findByPhoneNumber("010-1234-5678").isEmpty()) {
                User testUser = new User();
                testUser.setPhoneNumber("010-1234-5678");
                testUser.setPassword(passwordEncoder.encode("123456"));
                testUser.setName("테스트 사용자");
                testUser.setAccountNumber("VA1234567890");
                userRepository.save(testUser);
                
                // 계좌 잔액 생성
                AccountBalance balance = new AccountBalance();
                balance.setAccountNumber("VA1234567890");
                balance.setBalance(new BigDecimal("1000000")); // 100만원
                accountBalanceRepository.save(balance);
                
                // 테스트 알림 생성
                String userId = testUser.getId().toString();
                alarmService.sendUserNotification(userId, "LOGIN_SUCCESS", "로그인 성공 - 2025년 07월 28일 21시 11분에 로그인되었습니다");
                alarmService.sendUserNotification(userId, "LOGIN_SUCCESS", "로그인 성공 - 2025년 07월 28일 21시 09분에 로그인되었습니다");
                alarmService.sendUserNotification(userId, "LOGIN_FAILURE", "휴대폰: 010-1234-1234, 사유: 잘못된 비밀번호");
                alarmService.sendUserNotification(userId, "LOGIN_FAILURE", "휴대폰: 010-1234-1234, 사유: 잘못된 비밀번호");
                alarmService.sendUserNotification(userId, "BALANCE_CHANGE", "잔액 변동: +50,000원 (잔액: 1,050,000원)");
                alarmService.sendUserNotification(userId, "INSUFFICIENT_BALANCE", "잔액 부족: 출금 시도 2,000,000원 (현재 잔액: 1,050,000원)");
                alarmService.sendUserNotification(userId, "SYSTEM_ERROR", "시스템 점검 완료");
                
                log.info("Test user created: 010-1234-5678 / 123456");
                log.info("Test account: VA1234567890 with 1,000,000원 balance");
                log.info("Test alarms created for user: {}", userId);
            }
        };
    }
}
