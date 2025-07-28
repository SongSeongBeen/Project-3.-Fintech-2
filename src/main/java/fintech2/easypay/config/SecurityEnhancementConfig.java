package fintech2.easypay.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * 보안 강화 설정
 * 금융 서비스 수준의 보안 요구사항 적용
 */
@Configuration
@Slf4j
public class SecurityEnhancementConfig {
    
    /**
     * 강화된 비밀번호 인코더
     * BCrypt의 강도를 12로 설정 (기본값 10보다 강화)
     * SecurityConfig의 기본 PasswordEncoder 대신 사용
     */
    // @Bean - SecurityConfig의 PasswordEncoder와 중복 방지를 위해 주석 처리
    public PasswordEncoder strongPasswordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    
    /**
     * CORS 설정 강화
     * 프로덕션 환경에서는 특정 도메인만 허용
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 개발 환경에서는 로컬호스트 허용, 프로덕션에서는 실제 도메인 설정
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",
            "https://localhost:*",
            "https://*.easypay.com", // 실제 도메인으로 변경
            "https://*.easypay.co.kr" // 실제 도메인으로 변경
        ));
        
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "X-Requested-With",
            "X-Request-ID",
            "X-User-Agent"
        ));
        
        configuration.setExposedHeaders(Arrays.asList(
            "X-Request-ID",
            "X-Rate-Limit-Remaining",
            "X-Rate-Limit-Reset"
        ));
        
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // 1시간
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        log.info("CORS 설정이 적용되었습니다.");
        return source;
    }
    
    /**
     * 보안 헤더 설정
     */
    public static class SecurityHeaders {
        public static final String X_FRAME_OPTIONS = "DENY";
        public static final String X_CONTENT_TYPE_OPTIONS = "nosniff";
        public static final String X_XSS_PROTECTION = "1; mode=block";
        public static final String STRICT_TRANSPORT_SECURITY = "max-age=31536000; includeSubDomains";
        public static final String CONTENT_SECURITY_POLICY = 
            "default-src 'self'; " +
            "script-src 'self' 'unsafe-inline'; " +
            "style-src 'self' 'unsafe-inline'; " +
            "img-src 'self' data: https:; " +
            "font-src 'self'; " +
            "connect-src 'self'; " +
            "frame-ancestors 'none';";
    }
    
    /**
     * 허용되는 사용자 에이전트 패턴
     * 정상적인 모바일 앱과 웹 브라우저만 허용
     */
    public static class AllowedUserAgents {
        public static final List<String> PATTERNS = Arrays.asList(
            "Mozilla/.*", // 웹 브라우저
            "EasyPay-Android/.*", // 안드로이드 앱
            "EasyPay-iOS/.*", // iOS 앱
            "okhttp/.*" // 테스트용 (프로덕션에서는 제거)
        );
    }
    
    /**
     * API 요청 제한 설정
     */
    public static class RateLimitConfig {
        public static final int LOGIN_ATTEMPTS_PER_MINUTE = 5; // 1분당 로그인 시도 5회
        public static final int TRANSFER_REQUESTS_PER_MINUTE = 10; // 1분당 송금 요청 10회
        public static final int GENERAL_REQUESTS_PER_MINUTE = 100; // 1분당 일반 요청 100회
        
        public static final int DAILY_TRANSFER_LIMIT = 50; // 하루 송금 횟수 제한
        public static final long DAILY_AMOUNT_LIMIT = 10_000_000L; // 하루 송금 금액 제한 (1000만원)
    }
    
    /**
     * 세션 보안 설정
     */
    public static class SessionSecurity {
        public static final int JWT_EXPIRATION_MINUTES = 30; // JWT 만료시간 30분
        public static final int REFRESH_TOKEN_EXPIRATION_DAYS = 7; // 리프레시 토큰 7일
        public static final int MAX_SESSIONS_PER_USER = 3; // 사용자당 최대 세션 수
    }
    
    /**
     * 암호화 설정
     */
    public static class EncryptionConfig {
        public static final String AES_ALGORITHM = "AES/GCM/NoPadding";
        public static final int AES_KEY_LENGTH = 256;
        public static final int GCM_IV_LENGTH = 12;
        public static final int GCM_TAG_LENGTH = 16;
    }
    
    /**
     * 감사 로그 보안 설정
     */
    public static class AuditSecurity {
        public static final List<String> SENSITIVE_FIELDS = Arrays.asList(
            "password", "pin", "ssn", "cardNumber", "cvv", "accountNumber"
        );
        
        public static final int LOG_RETENTION_DAYS = 2555; // 7년 보관 (금융감독원 기준)
    }
}