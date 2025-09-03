package fintech2.easypay.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 표준화된 API 응답 클래스
 * 모든 REST API 응답에서 일관된 형식을 제공
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private final boolean success;
    private final T data;
    private final String message;
    private final String errorCode;
    private final String errorMessage;
    private final LocalDateTime timestamp;
    private final Map<String, Object> metadata;
    
    /**
     * 성공 응답 생성 (데이터 포함)
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, null, null, LocalDateTime.now(), null);
    }
    
    /**
     * 성공 응답 생성 (데이터 + 메시지)
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, null, null, LocalDateTime.now(), null);
    }
    
    /**
     * 성공 응답 생성 (데이터 + 메시지 + 메타데이터)
     */
    public static <T> ApiResponse<T> success(T data, String message, Map<String, Object> metadata) {
        return new ApiResponse<>(true, data, message, null, null, LocalDateTime.now(), metadata);
    }
    
    /**
     * 성공 응답 생성 (메시지만)
     */
    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, null, message, null, null, LocalDateTime.now(), null);
    }
    
    /**
     * 실패 응답 생성
     */
    public static <T> ApiResponse<T> error(String errorCode, String errorMessage) {
        return new ApiResponse<>(false, null, null, errorCode, errorMessage, LocalDateTime.now(), null);
    }
    
    /**
     * 실패 응답 생성 (메타데이터 포함)
     */
    public static <T> ApiResponse<T> error(String errorCode, String errorMessage, Map<String, Object> metadata) {
        return new ApiResponse<>(false, null, null, errorCode, errorMessage, LocalDateTime.now(), metadata);
    }
    
    /**
     * 검증 실패 응답 생성
     */
    public static <T> ApiResponse<T> validationError(String message) {
        return new ApiResponse<>(false, null, null, "VALIDATION_FAILED", message, LocalDateTime.now(), null);
    }
    
    /**
     * 권한 없음 응답 생성
     */
    public static <T> ApiResponse<T> unauthorized(String message) {
        return new ApiResponse<>(false, null, null, "UNAUTHORIZED", message, LocalDateTime.now(), null);
    }
    
    /**
     * 리소스 없음 응답 생성
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(false, null, null, "NOT_FOUND", message, LocalDateTime.now(), null);
    }
    
    /**
     * 내부 서버 오류 응답 생성
     */
    public static <T> ApiResponse<T> internalError(String message) {
        return new ApiResponse<>(false, null, null, "INTERNAL_SERVER_ERROR", message, LocalDateTime.now(), null);
    }
}