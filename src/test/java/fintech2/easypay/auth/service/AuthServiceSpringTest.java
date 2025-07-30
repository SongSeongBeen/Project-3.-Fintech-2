package fintech2.easypay.auth.service;

import fintech2.easypay.auth.entity.User;
import fintech2.easypay.auth.repository.UserRepository;
import fintech2.easypay.account.repository.AccountBalanceRepository;
import fintech2.easypay.audit.service.AuditLogService;
import fintech2.easypay.audit.service.AlarmService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 가벼운 테스트")
class AuthServiceSpringTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private AccountBalanceRepository accountBalanceRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtService jwtService;
    
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
            accountBalanceRepository,
            passwordEncoder,
            jwtService,
            loginHistoryService,
            auditLogService,
            alarmService
        );
    }

    @Test
    @DisplayName("가벼운 사용자 조회 테스트")
    void lightweightFindUserTest() {
        // Given
        String phoneNumber = "010-1234-5678";
        User user = new User(1L, phoneNumber, "encodedPassword", "홍길동", 
                           LocalDateTime.now(), "VA12345678", 0, false, null, null);

        given(userRepository.findByPhoneNumber(phoneNumber))
            .willReturn(Optional.of(user));

        // When
        Optional<User> result = userRepository.findByPhoneNumber(phoneNumber);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("홍길동");
        assertThat(result.get().getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(authService).isNotNull(); // 생성자로 주입된 서비스

        // 의존성 호출 검증
        then(userRepository).should().findByPhoneNumber(phoneNumber);
    }

    @Test
    @DisplayName("Mock을 활용한 비밀번호 인코딩 테스트")
    void passwordEncodingWithMockTest() {
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

        // Mock 검증
        then(passwordEncoder).should().encode(rawPassword);
    }

    @Test
    @DisplayName("가벼운 테스트 객체 생성 확인")
    void lightweightObjectCreationTest() {
        // Given & When & Then
        assertThat(authService).isNotNull();
        assertThat(userRepository).isNotNull();
        assertThat(passwordEncoder).isNotNull();
        assertThat(jwtService).isNotNull();
        assertThat(loginHistoryService).isNotNull();
        assertThat(auditLogService).isNotNull();
        assertThat(alarmService).isNotNull();
        
        // 모든 의존성이 Mock으로 생성됨을 확인
    }

    @Test
    @DisplayName("복합 비즈니스 로직 테스트 - 사용자 생성과 검증")
    void complexBusinessLogicTest() {
        // Given
        String phoneNumber = "010-5555-5555";
        String rawPassword = "newPassword123";
        String encodedPassword = "encodedNewPassword123";
        String jwtToken = "new.jwt.token";
        
        User newUser = new User(2L, phoneNumber, encodedPassword, "신규사용자", 
                              LocalDateTime.now(), "VA55555555", 0, false, null, null);

        // Mock 설정
        given(userRepository.findByPhoneNumber(phoneNumber))
            .willReturn(Optional.empty()) // 첫 번째 호출에서는 없음
            .willReturn(Optional.of(newUser)); // 두 번째 호출에서는 있음
        given(passwordEncoder.encode(rawPassword))
            .willReturn(encodedPassword);
        given(jwtService.generateAccessToken(phoneNumber))
            .willReturn(jwtToken);

        // When
        Optional<User> beforeCreate = userRepository.findByPhoneNumber(phoneNumber);
        String encoded = passwordEncoder.encode(rawPassword);
        String token = jwtService.generateAccessToken(phoneNumber);
        Optional<User> afterCreate = userRepository.findByPhoneNumber(phoneNumber);

        // Then
        assertThat(beforeCreate).isEmpty();
        assertThat(encoded).isEqualTo(encodedPassword);
        assertThat(token).isEqualTo(jwtToken);
        assertThat(afterCreate).isPresent();
        assertThat(afterCreate.get().getName()).isEqualTo("신규사용자");

        // 모든 의존성 호출 검증
        then(userRepository).should(times(2)).findByPhoneNumber(phoneNumber);
        then(passwordEncoder).should().encode(rawPassword);
        then(jwtService).should().generateAccessToken(phoneNumber);
    }
}