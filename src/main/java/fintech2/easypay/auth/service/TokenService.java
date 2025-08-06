package fintech2.easypay.auth.service;

import fintech2.easypay.auth.entity.RefreshToken;
import fintech2.easypay.auth.entity.User;
import fintech2.easypay.auth.repository.RefreshTokenRepository;
import fintech2.easypay.common.exception.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TokenService {

    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    
    @Value("${jwt.expiration.refresh:60000}") // 기본값 60초
    private long refreshTokenExpirationMs;

    /**
     * Access Token과 Refresh Token 생성
     */
    public TokenPair generateTokenPair(User user) {
        String accessToken = jwtService.generateAccessToken(user.getPhoneNumber());
        String refreshToken = generateRefreshToken(user);
        
        return TokenPair.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * Refresh Token 생성 및 저장 (user_id 기준 1개만 유지)
     */
    private String generateRefreshToken(User user) {
        String tokenValue = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000);
        
        // 기존 Refresh Token 확인 (user_id 기준 1개만)
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserIdAndIsRevokedFalse(user.getId());
        
        if (existingToken.isPresent()) {
            // 기존 토큰이 있으면 UPDATE (갱신)
            RefreshToken token = existingToken.get();
            token.setToken(tokenValue);
            token.setExpiresAt(expiresAt);
            token.setIsRevoked(false);
            token.setRevokedAt(null);
            refreshTokenRepository.save(token);
        } else {
            // 기존 토큰이 없으면 새로 생성
            RefreshToken refreshToken = RefreshToken.builder()
                    .token(tokenValue)
                    .userId(user.getId())
                    .phoneNumber(user.getPhoneNumber())
                    .expiresAt(expiresAt)
                    .isRevoked(false)
                    .build();
            
            refreshTokenRepository.save(refreshToken);
        }
        
        return tokenValue;
    }

    /**
     * Refresh Token으로 새로운 Access Token 발급 (Rolling Refresh 비활성화)
     */
    public TokenPair refreshAccessToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new AuthException("INVALID_REFRESH_TOKEN", "유효하지 않은 Refresh Token입니다"));
        
        if (!refreshToken.isValid()) {
            // 만료된 토큰을 자동으로 정리
            if (refreshToken.isExpired()) {
                refreshTokenRepository.delete(refreshToken);
            }
            
            throw new AuthException("EXPIRED_REFRESH_TOKEN", "만료된 Refresh Token입니다. 재로그인이 필요합니다.");
        }
        
        // 새로운 Access Token만 생성 (Refresh Token은 갱신하지 않음)
        String newAccessToken = jwtService.generateAccessToken(refreshToken.getPhoneNumber());
        
        return TokenPair.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshTokenValue) // 기존 Refresh Token 그대로 반환
                .build();
    }
    
    /**
     * 사용자 ID와 전화번호로 새로운 Refresh Token 생성 (만료 시간 갱신)
     */
    private String generateRefreshTokenForUser(Long userId, String phoneNumber) {
        String tokenValue = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000);
        
        // 기존 활성 Refresh Token 확인
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserIdAndIsRevokedFalse(userId);
        
        if (existingToken.isPresent()) {
            // 기존 토큰이 있으면 UPDATE (만료 시간 갱신)
            RefreshToken token = existingToken.get();
            token.setToken(tokenValue);
            token.setExpiresAt(expiresAt); // 만료 시간 갱신
            token.setIsRevoked(false);
            token.setRevokedAt(null);
            refreshTokenRepository.save(token);
        } else {
            // 기존 토큰이 없으면 새로 생성
            RefreshToken refreshToken = RefreshToken.builder()
                    .token(tokenValue)
                    .userId(userId)
                    .phoneNumber(phoneNumber)
                    .expiresAt(expiresAt)
                    .isRevoked(false)
                    .build();
            
            refreshTokenRepository.save(refreshToken);
        }
        return tokenValue;
    }

    /**
     * Refresh Token 폐기
     */
    public void revokeRefreshToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new AuthException("INVALID_REFRESH_TOKEN", "유효하지 않은 Refresh Token입니다"));
        
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);
    }

    /**
     * 사용자의 모든 Refresh Token 삭제 (로그아웃)
     */
    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId, LocalDateTime.now());
    }
    
    /**
     * 강제 만료 - 특정 사용자의 모든 Refresh Token 즉시 삭제
     */
    public void forceExpireUserTokens(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId, LocalDateTime.now());
    }

    /**
     * 만료된 Refresh Token 정리 (스케줄링)
     */
    @Scheduled(cron = "0 */5 * * * ?") // 5분마다 실행
    @Transactional
    public void cleanupExpiredTokens() {
        int deletedCount = refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
    
    /**
     * 수동으로 만료된 토큰 정리 (테스트용)
     */
    @Transactional
    public void manualCleanupExpiredTokens() {
        int deletedCount = refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }

    /**
     * 토큰 쌍을 위한 내부 클래스
     */
    @lombok.Builder
    @lombok.Data
    public static class TokenPair {
        private String accessToken;
        private String refreshToken;
    }
} 