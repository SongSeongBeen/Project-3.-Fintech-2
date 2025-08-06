package fintech2.easypay.auth.controller;

import fintech2.easypay.auth.dto.TokenRefreshRequest;
import fintech2.easypay.auth.service.AuthService;
import fintech2.easypay.auth.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController 테스트")
class AuthControllerTest {

    @Mock private AuthService authService;
    @Mock private TokenService tokenService;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(authService, tokenService);
    }

    @Test
    @DisplayName("토큰 갱신 성공 테스트")
    void refreshTokenSuccessTest() {
        // Given
        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken("valid-refresh-token");
        
        TokenService.TokenPair expectedTokenPair = TokenService.TokenPair.builder()
                .accessToken("new-access-token-123")
                .refreshToken("new-refresh-token-456")
                .build();
        when(tokenService.refreshAccessToken("valid-refresh-token"))
                .thenReturn(expectedTokenPair);

        // When
        ResponseEntity<?> response = authController.refreshToken(request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(Map.class);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertThat(responseBody.get("accessToken")).isEqualTo(expectedTokenPair.getAccessToken());
        assertThat(responseBody.get("refreshToken")).isEqualTo(expectedTokenPair.getRefreshToken());
        assertThat(responseBody.get("message")).isEqualTo("토큰이 성공적으로 갱신되었습니다");

        // Verify
        verify(tokenService).refreshAccessToken("valid-refresh-token");
    }

    @Test
    @DisplayName("토큰 갱신 실패 테스트")
    void refreshTokenFailureTest() {
        // Given
        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken("invalid-refresh-token");
        
        when(tokenService.refreshAccessToken("invalid-refresh-token"))
                .thenThrow(new RuntimeException("Invalid token"));

        // When
        ResponseEntity<?> response = authController.refreshToken(request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(Map.class);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertThat(responseBody.get("error")).isEqualTo("TOKEN_REFRESH_FAILED");

        // Verify
        verify(tokenService).refreshAccessToken("invalid-refresh-token");
    }

    @Test
    @DisplayName("로그아웃 성공 테스트")
    void logoutSuccessTest() {
        // Given
        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken("token-to-revoke");
        
        doNothing().when(tokenService).revokeRefreshToken("token-to-revoke");

        // When
        ResponseEntity<?> response = authController.logout((Authentication) request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(Map.class);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertThat(responseBody.get("message")).isEqualTo("로그아웃이 완료되었습니다");

        // Verify
        verify(tokenService).revokeRefreshToken("token-to-revoke");
    }

    @Test
    @DisplayName("로그아웃 실패 테스트")
    void logoutFailureTest() {
        // Given
        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken("non-existent-token");
        
        doThrow(new RuntimeException("Token not found"))
                .when(tokenService).revokeRefreshToken("non-existent-token");

        // When
        ResponseEntity<?> response = authController.logout((Authentication) request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(Map.class);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertThat(responseBody.get("error")).isEqualTo("LOGOUT_FAILED");

        // Verify
        verify(tokenService).revokeRefreshToken("non-existent-token");
    }

    @Test
    @DisplayName("회원가입 요청 위임 테스트")
    void registerDelegationTest() {
        // Given
        doReturn(ResponseEntity.ok().build()).when(authService).register(any());

        // When
        ResponseEntity<?> response = authController.register(null);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(authService).register(any());
    }

    @Test
    @DisplayName("로그인 요청 위임 테스트")
    void loginDelegationTest() {
        // Given
        doReturn(ResponseEntity.ok().build()).when(authService).login(any());

        // When
        ResponseEntity<?> response = authController.login(null);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(authService).login(any());
    }

    @Test
    @DisplayName("이메일 중복 확인 요청 위임 테스트")
    void checkEmailDuplicateDelegationTest() {
        // Given
        doReturn(ResponseEntity.ok().build()).when(authService).checkEmailDuplicate(anyString());

        // When
        ResponseEntity<?> response = authController.checkEmailDuplicate("test@example.com");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(authService).checkEmailDuplicate("test@example.com");
    }

    @Test
    @DisplayName("휴대폰 번호 중복 확인 요청 위임 테스트")
    void checkPhoneDuplicateDelegationTest() {
        // Given
        doReturn(ResponseEntity.ok().build()).when(authService).checkPhoneDuplicate(anyString());

        // When
        ResponseEntity<?> response = authController.checkPhoneDuplicate("010-1234-5678");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(authService).checkPhoneDuplicate("010-1234-5678");
    }

    @Test
    @DisplayName("프로필 조회 요청 위임 테스트")
    void getProfileDelegationTest() {
        // Given
        doReturn(ResponseEntity.ok().build()).when(authService).getProfile();

        // When
        ResponseEntity<?> response = authController.getProfile();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(authService).getProfile();
    }

    @Test
    @DisplayName("프로필 수정 요청 위임 테스트")
    void updateProfileDelegationTest() {
        // Given
        doReturn(ResponseEntity.ok().build()).when(authService).updateProfile(any());

        // When
        ResponseEntity<?> response = authController.updateProfile(null);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(authService).updateProfile(any());
    }

    @Test
    @DisplayName("비밀번호 확인 요청 위임 테스트")
    void verifyPasswordDelegationTest() {
        // Given
        doReturn(ResponseEntity.ok().build()).when(authService).verifyPassword(any());

        // When
        ResponseEntity<?> response = authController.verifyPassword(null);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(authService).verifyPassword(any());
    }

    @Test
    @DisplayName("PIN 등록 필요 여부 확인 요청 위임 테스트")
    void checkPinRequiredDelegationTest() {
        // Given
        doReturn(ResponseEntity.ok().build()).when(authService).checkPinRequired();

        // When
        ResponseEntity<?> response = authController.checkPinRequired();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(authService).checkPinRequired();
    }
} 