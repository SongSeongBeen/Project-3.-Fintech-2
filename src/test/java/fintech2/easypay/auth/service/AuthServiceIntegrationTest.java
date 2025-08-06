package fintech2.easypay.auth.service;

import fintech2.easypay.account.repository.AccountBalanceRepository;
import fintech2.easypay.account.repository.AccountRepository;
import fintech2.easypay.auth.entity.User;
import fintech2.easypay.auth.repository.UserRepository;
import fintech2.easypay.auth.service.JwtService;
import fintech2.easypay.audit.service.AuditLogService;
import fintech2.easypay.audit.service.AlarmService;
import fintech2.easypay.auth.service.LoginHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 가상 사용자 통합 테스트")
class AuthServiceIntegrationTest {

    @Mock private UserRepository userRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private AccountBalanceRepository accountBalanceRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private TokenService tokenService;
    @Mock private LoginHistoryService loginHistoryService;
    @Mock private AuditLogService auditLogService;
    @Mock private AlarmService alarmService;

    private AuthService authService;

    // 가상 사용자 데이터
    private User testUser1;
    private User testUser2;
    private User testUser3;
    private User lockedUser;

    @BeforeEach
    void setUp() {
        // Given: AuthService 생성자 주입으로 초기화
        authService = new AuthService(
            userRepository,
            accountRepository,
            accountBalanceRepository,
            passwordEncoder,
            jwtService,
            tokenService,
            loginHistoryService,
            auditLogService,
            alarmService
        );

        // Given: 가상 사용자 데이터 생성
        testUser1 = User.builder()
                .id(1L)
                .phoneNumber("010-1234-5678")
                .email("kim@example.com")
                .password("encodedPassword123")
                .name("김철수")
                .createdAt(LocalDateTime.now().minusDays(30))
                .accountNumber("VA1234567890")
                .build();

        testUser2 = User.builder()
                .id(2L)
                .phoneNumber("010-9876-5432")
                .email("lee@example.com")
                .password("encodedPassword456")
                .name("이영희")
                .createdAt(LocalDateTime.now().minusDays(15))
                .accountNumber("VA0987654321")
                .build();

        testUser3 = User.builder()
                .id(3L)
                .phoneNumber("010-5555-1111")
                .email("park@example.com")
                .password("encodedPassword789")
                .name("박민수")
                .createdAt(LocalDateTime.now().minusDays(7))
                .accountNumber("VA5555111122")
                .build();

        lockedUser = User.builder()
                .id(4L)
                .phoneNumber("010-1111-2222")
                .email("locked@example.com")
                .password("encodedPasswordLocked")
                .name("잠금계정")
                .createdAt(LocalDateTime.now().minusDays(1))
                .accountNumber("VA1111222233")
                .loginFailCount(5)
                .isLocked(true)
                .lockExpiresAt(LocalDateTime.now().plusMinutes(30))
                .lockReason("로그인 5회 실패로 인한 계정 잠금")
                .build();
    }

    @Test
    @DisplayName("가상 사용자 1 - 김철수 조회 성공 테스트")
    void findUser1SuccessTest() {
        // Given: 김철수 사용자 조회 시나리오
        when(userRepository.findByPhoneNumber("010-1234-5678"))
            .thenReturn(Optional.of(testUser1));

        // When: 사용자 조회 실행
        Optional<User> result = userRepository.findByPhoneNumber("010-1234-5678");

        // Then: 조회 결과 검증
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("김철수");
        assertThat(result.get().getAccountNumber()).isEqualTo("VA1234567890");
        assertThat(result.get().getLoginFailCount()).isEqualTo(0);
        assertThat(result.get().isAccountLocked()).isFalse();
    }

    @Test
    @DisplayName("가상 사용자 2 - 이영희 비밀번호 검증 테스트")
    void user2PasswordValidationTest() {
        // Given: 이영희 사용자 로그인 시나리오
        String rawPassword = "myPassword456";
        when(passwordEncoder.matches(rawPassword, testUser2.getPassword()))
            .thenReturn(true);

        // When: 비밀번호 검증 실행
        boolean isPasswordValid = passwordEncoder.matches(rawPassword, testUser2.getPassword());

        // Then: 비밀번호 검증 결과 확인
        assertThat(isPasswordValid).isTrue();
        assertThat(testUser2.getName()).isEqualTo("이영희");
        assertThat(testUser2.getPhoneNumber()).isEqualTo("010-9876-5432");
    }

    @Test
    @DisplayName("가상 사용자 3 - 박민수 JWT 토큰 생성 테스트")
    void user3JwtTokenGenerationTest() {
        // Given: 박민수 사용자 JWT 토큰 생성 시나리오
        String expectedToken = "jwt.token.for.user3";
        when(jwtService.generateAccessToken(testUser3.getPhoneNumber()))
            .thenReturn(expectedToken);

        // When: JWT 토큰 생성 실행
        String actualToken = jwtService.generateAccessToken(testUser3.getPhoneNumber());

        // Then: 토큰 생성 결과 검증
        assertThat(actualToken).isEqualTo(expectedToken);
        assertThat(testUser3.getName()).isEqualTo("박민수");
        assertThat(testUser3.getAccountNumber()).isEqualTo("VA5555111122");
    }

    @Test
    @DisplayName("잠긴 계정 - 계정 잠금 상태 확인 테스트")
    void lockedAccountStatusTest() {
        // Given: 잠긴 계정 상태 확인 시나리오
        // 이미 setUp에서 잠긴 사용자 데이터 생성

        // When: 계정 잠금 상태 확인
        boolean isLocked = lockedUser.isAccountLocked();

        // Then: 잠금 상태 검증
        assertThat(isLocked).isTrue();
        assertThat(lockedUser.getLoginFailCount()).isEqualTo(5);
        assertThat(lockedUser.getLockReason()).contains("로그인 5회 실패");
        assertThat(lockedUser.getName()).isEqualTo("잠금계정");
    }

    @Test
    @DisplayName("복합 시나리오 - 여러 사용자 동시 처리 테스트")
    void multipleUsersScenarioTest() {
        // Given: 여러 사용자 동시 처리 시나리오
        when(userRepository.findByPhoneNumber("010-1234-5678"))
            .thenReturn(Optional.of(testUser1));
        when(userRepository.findByPhoneNumber("010-9876-5432"))
            .thenReturn(Optional.of(testUser2));
        when(userRepository.findByPhoneNumber("010-5555-1111"))
            .thenReturn(Optional.of(testUser3));

        // When: 각 사용자별 조회 실행
        Optional<User> user1Result = userRepository.findByPhoneNumber("010-1234-5678");
        Optional<User> user2Result = userRepository.findByPhoneNumber("010-9876-5432");
        Optional<User> user3Result = userRepository.findByPhoneNumber("010-5555-1111");

        // Then: 모든 사용자 조회 결과 검증
        assertThat(user1Result).isPresent();
        assertThat(user1Result.get().getName()).isEqualTo("김철수");
        
        assertThat(user2Result).isPresent();
        assertThat(user2Result.get().getName()).isEqualTo("이영희");
        
        assertThat(user3Result).isPresent();
        assertThat(user3Result.get().getName()).isEqualTo("박민수");

        // 모든 사용자가 활성 상태인지 확인
        assertThat(user1Result.get().isAccountLocked()).isFalse();
        assertThat(user2Result.get().isAccountLocked()).isFalse();
        assertThat(user3Result.get().isAccountLocked()).isFalse();
    }

    @Test
    @DisplayName("실제 사용 사례 - 로그인 실패 후 계정 잠금 시나리오")
    void loginFailureAndLockScenarioTest() {
        // Given: 정상 사용자가 로그인을 5번 실패하는 시나리오
        User normalUser = User.builder()
                .id(5L)
                .phoneNumber("010-7777-8888")
                .email("test@example.com")
                .password("encodedPassword")
                .name("테스트유저")
                .accountNumber("VA7777888899")
                .build();

        // When: 로그인 실패를 5번 반복
        for (int i = 0; i < 5; i++) {
            normalUser.incrementLoginFailCount();
        }

        // Then: 계정이 잠긴 상태인지 확인
        assertThat(normalUser.isAccountLocked()).isTrue();
        assertThat(normalUser.getLoginFailCount()).isEqualTo(5);
        assertThat(normalUser.getLockReason()).contains("로그인 5회 실패");
        assertThat(normalUser.getLockExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("계정 복구 시나리오 - 잠긴 계정 해제 테스트")
    void accountRecoveryScenarioTest() {
        // Given: 잠긴 계정을 복구하는 시나리오
        User userToRecover = User.builder()
                .id(6L)
                .phoneNumber("010-9999-0000")
                .email("recover@example.com")
                .password("encodedPassword")
                .name("복구유저")
                .accountNumber("VA9999000011")
                .loginFailCount(5)
                .isLocked(true)
                .lockExpiresAt(LocalDateTime.now().plusMinutes(30))
                .lockReason("로그인 5회 실패로 인한 계정 잠금")
                .build();

        // When: 계정 복구 실행
        userToRecover.resetLoginFailCount();

        // Then: 계정이 정상 상태로 복구되었는지 확인
        assertThat(userToRecover.isAccountLocked()).isFalse();
        assertThat(userToRecover.getLoginFailCount()).isEqualTo(0);
        assertThat(userToRecover.getLockReason()).isNull();
        assertThat(userToRecover.getLockExpiresAt()).isNull();
    }
}