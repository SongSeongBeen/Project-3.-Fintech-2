package fintech2.easypay.common.dto;

import java.time.LocalDateTime;

/**
 * Java 21: Record for standardized API responses
 * 
 * @param success 성공 여부
 * @param message 응답 메시지
 * @param data 응답 데이터
 * @param timestamp 응답 시간
 * @param errorCode 에러 코드 (실패 시)
 */
public record ApiResponse<T>(
    boolean success,
    String message,
    T data,
    LocalDateTime timestamp,
    String errorCode
) {
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now(), null);
    }
    
    public static <T> ApiResponse<T> success(T data) {
        return success(data, "요청이 성공적으로 처리되었습니다");
    }
    
    public static <T> ApiResponse<T> failure(String message, String errorCode) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now(), errorCode);
    }
    
    public static <T> ApiResponse<T> failure(String message) {
        return failure(message, "UNKNOWN_ERROR");
    }
    
    // Java 21: Pattern matching for response type
    public boolean isSuccess() {
        return success;
    }
    
    public boolean hasData() {
        return data != null;
    }
    
    public boolean hasError() {
        return !success && errorCode != null;
    }
} 