package fintech2.easypay.auth.service;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService 테스트")
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // 테스트용 시크릿 키 설정
        ReflectionTestUtils.setField(jwtService, "secretKey", 
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L); // 24시간
    }

    @Test
    @DisplayName("Access Token 생성 테스트")
    void generateAccessTokenTest() {
        // Given
        String phoneNumber = "010-1234-5678";

        // When
        String token = jwtService.generateAccessToken(phoneNumber);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT는 3개의 부분으로 구성

        // 토큰에서 정보 추출 확인
        String extractedPhoneNumber = jwtService.extractPhoneNumber(token);
        assertThat(extractedPhoneNumber).isEqualTo(phoneNumber);
    }

    @Test
    @DisplayName("토큰에서 사용자명(전화번호) 추출 테스트")
    void extractUsernameTest() {
        // Given
        String phoneNumber = "010-9876-5432";
        String token = jwtService.generateAccessToken(phoneNumber);

        // When
        String extractedUsername = jwtService.extractUsername(token);

        // Then
        assertThat(extractedUsername).isEqualTo(phoneNumber);
    }

    @Test
    @DisplayName("토큰에서 전화번호 추출 테스트")
    void extractPhoneNumberTest() {
        // Given
        String phoneNumber = "010-5555-1111";
        String token = jwtService.generateAccessToken(phoneNumber);

        // When
        String extractedPhoneNumber = jwtService.extractPhoneNumber(token);

        // Then
        assertThat(extractedPhoneNumber).isEqualTo(phoneNumber);
    }

    @Test
    @DisplayName("토큰 만료 시간 추출 테스트")
    void extractExpirationTest() {
        // Given
        String phoneNumber = "010-1234-5678";
        String token = jwtService.generateAccessToken(phoneNumber);

        // When
        Date expiration = jwtService.extractExpiration(token);

        // Then
        assertThat(expiration).isNotNull();
        assertThat(expiration).isAfter(new Date()); // 만료 시간이 현재 시간보다 미래
    }

    @Test
    @DisplayName("토큰 만료 여부 확인 테스트")
    void isTokenExpiredTest() {
        // Given
        String phoneNumber = "010-1234-5678";
        String token = jwtService.generateAccessToken(phoneNumber);

        // When
        boolean isExpired = jwtService.isTokenExpired(token);

        // Then
        assertThat(isExpired).isFalse(); // 새로 생성된 토큰은 만료되지 않음
    }

    @Test
    @DisplayName("PIN 세션 토큰 생성 테스트")
    void generatePinSessionTokenTest() {
        // Given
        Long userId = 1L;
        String purpose = "transfer";
        int expirationMinutes = 10;

        // When
        String pinSessionToken = jwtService.generatePinSessionToken(userId, purpose, expirationMinutes);

        // Then
        assertThat(pinSessionToken).isNotNull();
        assertThat(pinSessionToken).isNotEmpty();
        assertThat(pinSessionToken.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("PIN 세션 토큰 검증 테스트")
    void validatePinSessionTokenTest() {
        // Given
        Long userId = 1L;
        String purpose = "payment";
        int expirationMinutes = 5;
        String pinSessionToken = jwtService.generatePinSessionToken(userId, purpose, expirationMinutes);

        // When
        boolean isValid = jwtService.validatePinSessionToken(pinSessionToken, purpose);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("PIN 세션 토큰에서 사용자 ID 추출 테스트")
    void extractUserIdFromPinSessionTest() {
        // Given
        Long userId = 123L;
        String purpose = "common";
        String pinSessionToken = jwtService.generatePinSessionToken(userId, purpose, 10);

        // When
        Long extractedUserId = jwtService.extractUserIdFromPinSession(pinSessionToken);

        // Then
        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    @DisplayName("PIN 세션 토큰에서 목적 추출 테스트")
    void extractPurposeFromPinSessionTest() {
        // Given
        Long userId = 1L;
        String purpose = "transfer";
        String pinSessionToken = jwtService.generatePinSessionToken(userId, purpose, 10);

        // When
        String extractedPurpose = jwtService.extractPurposeFromPinSession(pinSessionToken);

        // Then
        assertThat(extractedPurpose).isEqualTo(purpose);
    }

    @Test
    @DisplayName("잘못된 목적으로 PIN 세션 토큰 검증 시 실패 테스트")
    void validatePinSessionTokenWithWrongPurposeTest() {
        // Given
        Long userId = 1L;
        String originalPurpose = "transfer";
        String wrongPurpose = "payment";
        String pinSessionToken = jwtService.generatePinSessionToken(userId, originalPurpose, 10);

        // When
        boolean isValid = jwtService.validatePinSessionToken(pinSessionToken, wrongPurpose);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("만료된 PIN 세션 토큰 검증 시 실패 테스트")
    void validateExpiredPinSessionTokenTest() {
        // Given: 매우 짧은 만료 시간으로 토큰 생성
        Long userId = 1L;
        String purpose = "transfer";
        String pinSessionToken = jwtService.generatePinSessionToken(userId, purpose, 0); // 즉시 만료

        // 잠시 대기하여 토큰 만료
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        boolean isValid = jwtService.validatePinSessionToken(pinSessionToken, purpose);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("일반 JWT 토큰을 PIN 세션 토큰으로 검증 시 실패 테스트")
    void validateRegularTokenAsPinSessionTest() {
        // Given
        String regularToken = jwtService.generateAccessToken("010-1234-5678");
        String purpose = "transfer";

        // When
        boolean isValid = jwtService.validatePinSessionToken(regularToken, purpose);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("토큰 유효성 검사 테스트")
    void isTokenValidTest() {
        // Given
        String phoneNumber = "010-1234-5678";
        String token = jwtService.generateAccessToken(phoneNumber);

        // Mock UserDetails 생성
        org.springframework.security.core.userdetails.UserDetails userDetails = 
            org.springframework.security.core.userdetails.User.builder()
                .username(phoneNumber)
                .password("password")
                .authorities("USER")
                .build();

        // When
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("잘못된 사용자명으로 토큰 검증 시 실패 테스트")
    void isTokenValidWithWrongUsernameTest() {
        // Given
        String phoneNumber = "010-1234-5678";
        String token = jwtService.generateAccessToken(phoneNumber);

        // Mock UserDetails with wrong username
        org.springframework.security.core.userdetails.UserDetails userDetails = 
            org.springframework.security.core.userdetails.User.builder()
                .username("010-9999-9999") // 잘못된 사용자명
                .password("password")
                .authorities("USER")
                .build();

        // When
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("클레임 추출 테스트")
    void extractClaimTest() {
        // Given
        String phoneNumber = "010-1234-5678";
        String token = jwtService.generateAccessToken(phoneNumber);

        // When
        String subject = jwtService.extractClaim(token, Claims::getSubject);

        // Then
        assertThat(subject).isEqualTo(phoneNumber);
    }

    @Test
    @DisplayName("잘못된 토큰으로 정보 추출 시 예외 발생 테스트")
    void extractInfoFromInvalidTokenTest() {
        // Given
        String invalidToken = "invalid.token.here";

        // When & Then
        assertThatThrownBy(() -> jwtService.extractPhoneNumber(invalidToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("다양한 전화번호 형식으로 토큰 생성 테스트")
    void generateTokenWithDifferentPhoneNumbersTest() {
        // Given
        String[] phoneNumbers = {
            "010-1234-5678",
            "010-9876-5432",
            "010-5555-1111",
            "010-1111-2222"
        };

        // When & Then
        for (String phoneNumber : phoneNumbers) {
            String token = jwtService.generateAccessToken(phoneNumber);
            String extractedPhoneNumber = jwtService.extractPhoneNumber(token);
            
            assertThat(extractedPhoneNumber).isEqualTo(phoneNumber);
            assertThat(jwtService.isTokenExpired(token)).isFalse();
        }
    }

    @Test
    @DisplayName("토큰 생성 시 추가 클레임 포함 테스트")
    void generateTokenWithExtraClaimsTest() {
        // Given
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "USER");
        extraClaims.put("permissions", "READ,WRITE");
        String phoneNumber = "010-1234-5678";
        long expiration = 3600000L; // 1시간

        // When
        String token = jwtService.generateToken(extraClaims, phoneNumber, expiration);

        // Then
        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3);
        
        // 토큰에서 기본 정보 추출 확인
        String extractedPhoneNumber = jwtService.extractPhoneNumber(token);
        assertThat(extractedPhoneNumber).isEqualTo(phoneNumber);
    }
} 