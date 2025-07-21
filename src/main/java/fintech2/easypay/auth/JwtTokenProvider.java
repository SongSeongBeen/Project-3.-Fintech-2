package fintech2.easypay.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증을 위한 유틸리티 클래스
 * HS256 알고리즘을 사용하여 토큰을 서명하고 검증
 * 인증된 사용자의 세션 관리를 위한 상태비저장 인증 시스템
 */
@Component
@Slf4j
public class JwtTokenProvider {
    
    private final SecretKey key;
    private final long tokenValidityInMilliseconds;
    private final UserDetailsService userDetailsService;
    
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
                           @Value("${jwt.expiration}") long tokenValidityInMilliseconds,
                           UserDetailsService userDetailsService) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.tokenValidityInMilliseconds = tokenValidityInMilliseconds;
        this.userDetailsService = userDetailsService;
    }
    
    /**
     * JWT 토큰 생성
     * 휴대폰 번호를 주체(subject)로 하여 토큰을 생성
     * @param phoneNumber 인증된 사용자의 휴대폰 번호
     * @return 생성된 JWT 토큰
     */
    public String createToken(String phoneNumber) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + tokenValidityInMilliseconds);
        
        return Jwts.builder()
                .setSubject(phoneNumber)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(getPhoneNumber(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }
    
    /**
     * JWT 토큰에서 휴대폰 번호 추출
     * @param token 분석할 JWT 토큰
     * @return 토큰에 포함된 휴대폰 번호
     */
    public String getPhoneNumber(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    
    /**
     * JWT 토큰 유효성 검증
     * 서명 검증 및 만료 시간 확인
     * @param token 검증할 JWT 토큰
     * @return 토큰 유효성 여부
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 토큰 유효 시간 조회
     * @return 토큰 유효 시간 (밀리초)
     */
    public long getTokenValidityInMilliseconds() {
        return tokenValidityInMilliseconds;
    }
}
