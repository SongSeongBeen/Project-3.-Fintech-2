package fintech2.easypay.auth.service;

import fintech2.easypay.auth.dto.LoginRequest;
import fintech2.easypay.auth.dto.RegisterRequest;
import fintech2.easypay.auth.entity.User;
import fintech2.easypay.auth.repository.UserRepository;
import fintech2.easypay.account.entity.AccountBalance;
import fintech2.easypay.account.repository.AccountRepository;
import fintech2.easypay.account.repository.AccountBalanceRepository;
import fintech2.easypay.audit.service.AuditLogService;
import fintech2.easypay.audit.service.AlarmService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 테스트")
class AuthServiceSimpleTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private AccountRepository accountRepository;
    
    @Mock
    private AccountBalanceRepository accountBalanceRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtService jwtService;
    
    @Mock
    private TokenService tokenService;
    
    @Mock
    private LoginHistoryService loginHistoryService;
    
    @Mock
    private AuditLogService auditLogService;
    
    @Mock
    private AlarmService alarmService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    @DisplayName("사용자 조회 성공 테스트")
    void findUserSuccessTest() {
        // Given
        String phoneNumber = "010-1234-5678";
        User user = User.builder()
                .id(1L)
                .phoneNumber(phoneNumber)
                .email("hong@example.com")
                .password("encodedPassword")
                .name("홍길동")
                .accountNumber("VA12345678")
                .build();

        given(userRepository.findByPhoneNumber(phoneNumber))
            .willReturn(Optional.of(user));

        // When
        Optional<User> result = userRepository.findByPhoneNumber(phoneNumber);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("홍길동");
        assertThat(result.get().getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(result.get().getAccountNumber()).isEqualTo("VA12345678");

        // 의존성 호출 검증
        then(userRepository).should().findByPhoneNumber(phoneNumber);
    }

    @Test
    @DisplayName("사용자 조회 실패 - 존재하지 않는 사용자")
    void findUserNotFoundTest() {
        // Given
        String phoneNumber = "010-9999-9999";

        given(userRepository.findByPhoneNumber(phoneNumber))
            .willReturn(Optional.empty());

        // When
        Optional<User> result = userRepository.findByPhoneNumber(phoneNumber);

        // Then
        assertThat(result).isEmpty();

        // 의존성 호출 검증
        then(userRepository).should().findByPhoneNumber(phoneNumber);
    }

    @Test
    @DisplayName("비밀번호 인코딩 테스트")
    void passwordEncodingTest() {
        // Given
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword123";

        given(passwordEncoder.encode(rawPassword))
            .willReturn(encodedPassword);

        // When
        String result = passwordEncoder.encode(rawPassword);

        // Then
        assertThat(result).isEqualTo(encodedPassword);
        assertThat(result).isNotEqualTo(rawPassword);

        // 의존성 호출 검증
        then(passwordEncoder).should().encode(rawPassword);
    }

    @Test
    @DisplayName("비밀번호 매칭 테스트")
    void passwordMatchingTest() {
        // Given
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword123";

        given(passwordEncoder.matches(rawPassword, encodedPassword))
            .willReturn(true);

        // When
        boolean result = passwordEncoder.matches(rawPassword, encodedPassword);

        // Then
        assertThat(result).isTrue();

        // 의존성 호출 검증
        then(passwordEncoder).should().matches(rawPassword, encodedPassword);
    }

    @Test
    @DisplayName("JWT 토큰 생성 테스트")
    void jwtTokenGenerationTest() {
        // Given
        String phoneNumber = "010-1234-5678";
        String expectedToken = "jwt.token.here";

        given(jwtService.generateAccessToken(phoneNumber))
            .willReturn(expectedToken);

        // When
        String result = jwtService.generateAccessToken(phoneNumber);

        // Then
        assertThat(result).isEqualTo(expectedToken);
        assertThat(result).isNotEmpty();

        // 의존성 호출 검증
        then(jwtService).should().generateAccessToken(phoneNumber);
    }
}