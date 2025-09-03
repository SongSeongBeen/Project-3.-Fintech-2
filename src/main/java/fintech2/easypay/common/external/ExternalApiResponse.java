package fintech2.easypay.common.external;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 외부 API 응답 래퍼 클래스
 * 성공/실패 상태, 응답 데이터, 메타데이터를 포함
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ExternalApiResponse<T> {
    
    private final boolean success;
    private final T responseData;
    private final String errorCode;
    private final String errorMessage;
    private final LocalDateTime responseTime;
    private final long executionTimeMs;
    private final int retryCount;
    private final Map<String, Object> metadata;
    
    /**
     * 성공 응답 생성
     */
    public static <T> ExternalApiResponse<T> success(T responseData, long executionTimeMs, int retryCount) {
        return new ExternalApiResponse<>(
            true, responseData, null, null, 
            LocalDateTime.now(), executionTimeMs, retryCount, Map.of()
        );
    }
    
    /**
     * 성공 응답 생성 (메타데이터 포함)
     */
    public static <T> ExternalApiResponse<T> success(T responseData, long executionTimeMs, int retryCount, 
                                                   Map<String, Object> metadata) {
        return new ExternalApiResponse<>(
            true, responseData, null, null, 
            LocalDateTime.now(), executionTimeMs, retryCount, metadata
        );
    }
    
    /**
     * 실패 응답 생성
     */
    public static <T> ExternalApiResponse<T> failure(String errorCode, String errorMessage, 
                                                   long executionTimeMs, int retryCount) {
        return new ExternalApiResponse<>(
            false, null, errorCode, errorMessage, 
            LocalDateTime.now(), executionTimeMs, retryCount, Map.of()
        );
    }
    
    /**
     * 실패 응답 생성 (메타데이터 포함)
     */
    public static <T> ExternalApiResponse<T> failure(String errorCode, String errorMessage, 
                                                   long executionTimeMs, int retryCount, 
                                                   Map<String, Object> metadata) {
        return new ExternalApiResponse<>(
            false, null, errorCode, errorMessage, 
            LocalDateTime.now(), executionTimeMs, retryCount, metadata
        );
    }
    
    /**
     * 응답이 성공인지 확인
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * 응답이 실패인지 확인
     */
    public boolean isFailure() {
        return !success;
    }
}