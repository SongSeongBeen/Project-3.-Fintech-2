package fintech2.easypay.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String secretKey;

    @Value("${jwt.expiration.refresh:60000}") // 24시간, 테스트 5분
    private long jwtExpiration;

    public String generateAccessToken(String phoneNumber) {
        return generateToken(new HashMap<>(), phoneNumber, jwtExpiration);
    }

    public String generateToken(Map<String, Object> extraClaims, String subject, long expiration) {
        // 외부 변경 가능 객체 문제 해결을 위해 Map을 복사
        Map<String, Object> immutableClaims = new HashMap<>(extraClaims);
        
        return Jwts.builder()
                .setClaims(immutableClaims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractPhoneNumber(String token) {
        return extractUsername(token);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * PIN 인증 후 발급되는 임시 세션 토큰 생성
     * @param userId 사용자 ID
     * @param purpose PIN 사용 목적 (transfer, payment, common)
     * @param expirationMinutes 만료 시간 (분)
     * @return PIN 세션 토큰
     */
    public String generatePinSessionToken(Long userId, String purpose, int expirationMinutes) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("purpose", purpose);
        claims.put("type", "PIN_SESSION");
        
        long expiration = expirationMinutes * 60 * 1000L; // 분을 밀리초로 변환
        
        return generateToken(claims, "PIN_SESSION_" + userId, expiration);
    }

    /**
     * PIN 세션 토큰 검증
     * @param token PIN 세션 토큰
     * @param expectedPurpose 예상되는 목적
     * @return 검증 결과
     */
    public boolean validatePinSessionToken(String token, String expectedPurpose) {
        try {
            Claims claims = extractAllClaims(token);
            
            // 토큰 타입 확인
            String tokenType = claims.get("type", String.class);
            if (!"PIN_SESSION".equals(tokenType)) {
                log.warn("잘못된 토큰 타입: {}", tokenType);
                return false;
            }
            
            // 목적 확인
            String tokenPurpose = claims.get("purpose", String.class);
            if (!expectedPurpose.equals(tokenPurpose)) {
                log.warn("토큰 목적 불일치: expected={}, actual={}", expectedPurpose, tokenPurpose);
                return false;
            }
            
            // 만료 시간 확인
            if (isTokenExpired(token)) {
                log.warn("PIN 세션 토큰 만료");
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("PIN 세션 토큰 검증 실패", e);
            return false;
        }
    }

    /**
     * PIN 세션 토큰에서 사용자 ID 추출
     * @param token PIN 세션 토큰
     * @return 사용자 ID
     */
    public Long extractUserIdFromPinSession(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", Long.class);
    }

    /**
     * PIN 세션 토큰에서 목적 추출
     * @param token PIN 세션 토큰
     * @return 목적
     */
    public String extractPurposeFromPinSession(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("purpose", String.class);
    }
} 