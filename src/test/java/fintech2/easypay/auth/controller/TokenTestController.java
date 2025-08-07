package fintech2.easypay.auth.controller;

import fintech2.easypay.auth.service.JwtService;
import fintech2.easypay.auth.service.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;
import fintech2.easypay.auth.entity.RefreshToken;
import fintech2.easypay.auth.repository.RefreshTokenRepository;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;

/**
 * 테스트용 토큰 컨트롤러
 * 실제 프로덕션에서는 제거되어야 함
 */
@RestController
@RequestMapping("/test/token")
public class TokenTestController {

    private final JwtService jwtService;
    private final TokenService tokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenTestController(JwtService jwtService, TokenService tokenService, RefreshTokenRepository refreshTokenRepository) {
        this.jwtService = jwtService;
        this.tokenService = tokenService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * JWT 토큰 디코딩 및 정보 확인
     */
    @GetMapping("/decode")
    public ResponseEntity<?> decodeToken(@RequestParam String token) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 토큰 유효성 검사
            boolean isValid = !jwtService.isTokenExpired(token);
            result.put("isValid", isValid);
            
            // 토큰에서 정보 추출
            String phoneNumber = jwtService.extractPhoneNumber(token);
            result.put("phoneNumber", phoneNumber);
            
            // 만료 시간 확인
            java.util.Date expiration = jwtService.extractExpiration(token);
            result.put("expiration", expiration);
            result.put("expirationTime", expiration.getTime());
            
            // 현재 시간과 비교
            long currentTime = System.currentTimeMillis();
            long timeUntilExpiration = expiration.getTime() - currentTime;
            result.put("currentTime", currentTime);
            result.put("timeUntilExpiration", timeUntilExpiration);
            result.put("timeUntilExpirationMinutes", timeUntilExpiration / (1000 * 60));
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "INVALID_TOKEN",
                "message", "토큰 디코딩 실패: " + e.getMessage()
            ));
        }
    }

    /**
     * Refresh Token 정보 확인
     */
    @GetMapping("/refresh-info")
    public ResponseEntity<?> getRefreshTokenInfo(@RequestParam String refreshToken) {
        try {
            // TokenService에서 Refresh Token 정보 조회
            // 실제 구현에서는 RefreshTokenRepository를 통해 조회
            Map<String, Object> result = new HashMap<>();
            result.put("refreshToken", refreshToken);
            result.put("message", "Refresh Token 정보 조회 기능은 TokenService에서 구현 필요");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "INVALID_REFRESH_TOKEN",
                "message", "Refresh Token 조회 실패: " + e.getMessage()
            ));
        }
    }

    /**
     * 테스트용 Access Token 생성
     */
    @PostMapping("/generate-test")
    public ResponseEntity<?> generateTestToken(@RequestParam String phoneNumber) {
        try {
            String accessToken = jwtService.generateAccessToken(phoneNumber);
            
            Map<String, Object> result = new HashMap<>();
            result.put("accessToken", accessToken);
            result.put("phoneNumber", phoneNumber);
            result.put("message", "테스트용 Access Token이 생성되었습니다");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "TOKEN_GENERATION_FAILED",
                "message", "토큰 생성 실패: " + e.getMessage()
            ));
        }
    }

    /**
     * 토큰 유효성 검사
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 토큰 만료 여부 확인
            boolean isExpired = jwtService.isTokenExpired(token);
            result.put("isExpired", isExpired);
            
            // 토큰 유효성
            boolean isValid = !isExpired;
            result.put("isValid", isValid);
            
            if (isValid) {
                String phoneNumber = jwtService.extractPhoneNumber(token);
                result.put("phoneNumber", phoneNumber);
                result.put("message", "토큰이 유효합니다");
            } else {
                result.put("message", "토큰이 만료되었습니다");
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "TOKEN_VALIDATION_FAILED",
                "message", "토큰 검증 실패: " + e.getMessage()
            ));
        }
    }

    /**
     * 특정 사용자의 Refresh Token 정보 조회
     */
    @GetMapping("/refresh-token/user/{userId}")
    public ResponseEntity<?> getUserRefreshToken(@PathVariable Long userId) {
        try {
            Optional<RefreshToken> refreshToken = refreshTokenRepository.findByUserIdAndIsRevokedFalse(userId);
            
            Map<String, Object> response = new HashMap<>();
            if (refreshToken.isPresent()) {
                RefreshToken token = refreshToken.get();
                response.put("userId", token.getUserId());
                response.put("phoneNumber", token.getPhoneNumber());
                response.put("token", token.getToken());
                response.put("expiresAt", token.getExpiresAt());
                response.put("isRevoked", token.getIsRevoked());
                response.put("createdAt", token.getCreatedAt());
                response.put("message", "사용자별 활성 Refresh Token이 있습니다");
            } else {
                response.put("message", "해당 사용자의 활성 Refresh Token이 없습니다");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "사용자 Refresh Token 조회 실패");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 모든 사용자의 Refresh Token 개수 확인
     */
    @GetMapping("/refresh-token/count")
    public ResponseEntity<?> getRefreshTokenCount() {
        try {
            List<RefreshToken> allTokens = refreshTokenRepository.findAll();
            List<RefreshToken> activeTokens = allTokens.stream()
                    .filter(token -> !token.getIsRevoked())
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalTokens", allTokens.size());
            response.put("activeTokens", activeTokens.size());
            response.put("revokedTokens", allTokens.size() - activeTokens.size());
            
            // 사용자별 토큰 개수
            Map<Long, Long> userTokenCount = activeTokens.stream()
                    .collect(Collectors.groupingBy(RefreshToken::getUserId, Collectors.counting()));
            
            response.put("userTokenCount", userTokenCount);
            response.put("message", "Refresh Token 통계");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Refresh Token 개수 조회 실패");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 만료된 Refresh Token 수동 정리
     */
    @PostMapping("/cleanup-expired")
    public ResponseEntity<?> cleanupExpiredTokens() {
        try {
            System.out.println("🧹 [테스트] 만료된 Refresh Token 수동 정리 시작");
            int deletedCount = refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
            
            Map<String, Object> response = new HashMap<>();
            response.put("deletedCount", deletedCount);
            response.put("message", "만료된 토큰 정리 완료");
            
            System.out.println("🧹 [테스트] 정리된 토큰 수: " + deletedCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "토큰 정리 실패");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 특정 사용자의 토큰 상태 확인
     */
    @GetMapping("/user/{userId}/token-status")
    public ResponseEntity<?> getUserTokenStatus(@PathVariable Long userId) {
        try {
            Optional<RefreshToken> refreshToken = refreshTokenRepository.findByUserIdAndIsRevokedFalse(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            
            if (refreshToken.isPresent()) {
                RefreshToken token = refreshToken.get();
                response.put("hasActiveToken", true);
                response.put("isExpired", token.isExpired());
                response.put("isValid", token.isValid());
                response.put("expiresAt", token.getExpiresAt());
                response.put("timeUntilExpiration", java.time.Duration.between(LocalDateTime.now(), token.getExpiresAt()).toMinutes());
                response.put("message", "활성 토큰이 있습니다");
            } else {
                response.put("hasActiveToken", false);
                response.put("message", "활성 토큰이 없습니다");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "토큰 상태 조회 실패");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
} 