package fintech2.easypay.common.response;

import fintech2.easypay.common.BusinessException;
import fintech2.easypay.common.ErrorCode;
import fintech2.easypay.common.validation.ValidationResult;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 응답 빌더 서비스
 * 일관된 API 응답을 생성하기 위한 유틸리티 클래스
 */
@Component
@Slf4j
public class ResponseBuilderService {
    
    /**
     * 성공 응답 생성
     */
    public <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }
    
    /**
     * 성공 응답 생성 (메시지 포함)
     */
    public <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }
    
    /**
     * 페이지 응답 생성 (페이지네이션 메타데이터 포함)
     */
    public <T> ResponseEntity<ApiResponse<List<T>>> successPage(Page<T> page) {
        Map<String, Object> metadata = createPageMetadata(page);
        return ResponseEntity.ok(ApiResponse.success(page.getContent(), "조회 성공", metadata));
    }
    
    /**
     * 페이지 응답 생성 (커스텀 메시지)
     */
    public <T> ResponseEntity<ApiResponse<List<T>>> successPage(Page<T> page, String message) {
        Map<String, Object> metadata = createPageMetadata(page);
        return ResponseEntity.ok(ApiResponse.success(page.getContent(), message, metadata));
    }
    
    /**
     * 생성 성공 응답 (201 Created)
     */
    public <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(data, message));
    }
    
    /**
     * 수정 성공 응답 (200 OK)
     */
    public <T> ResponseEntity<ApiResponse<T>> updated(T data, String message) {
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }
    
    /**
     * 삭제 성공 응답 (204 No Content)
     */
    public ResponseEntity<ApiResponse<Void>> deleted(String message) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(message));
    }
    
    /**
     * 검증 실패 응답
     */
    public <T> ResponseEntity<ApiResponse<T>> validationError(ValidationResult validationResult) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.validationError(validationResult.getAllErrorMessages()));
    }
    
    /**
     * 검증 실패 응답 (단일 메시지)
     */
    public <T> ResponseEntity<ApiResponse<T>> validationError(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.validationError(message));
    }
    
    /**
     * 비즈니스 예외 응답
     */
    public <T> ResponseEntity<ApiResponse<T>> businessError(BusinessException exception) {
        HttpStatus httpStatus = getHttpStatusFromErrorCode(exception.getErrorCode());
        
        Map<String, Object> metadata = new HashMap<>();
        
        return ResponseEntity.status(httpStatus)
                .body(ApiResponse.error(
                    exception.getErrorCode().name(), 
                    exception.getMessage(),
                    metadata.isEmpty() ? null : metadata
                ));
    }
    
    /**
     * 일반 오류 응답
     */
    public <T> ResponseEntity<ApiResponse<T>> error(ErrorCode errorCode, String message) {
        HttpStatus httpStatus = getHttpStatusFromErrorCode(errorCode);
        return ResponseEntity.status(httpStatus)
                .body(ApiResponse.error(errorCode.name(), message));
    }
    
    /**
     * 권한 없음 응답 (401 Unauthorized)
     */
    public <T> ResponseEntity<ApiResponse<T>> unauthorized(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.unauthorized(message));
    }
    
    /**
     * 접근 거부 응답 (403 Forbidden)
     */
    public <T> ResponseEntity<ApiResponse<T>> forbidden(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("FORBIDDEN", message));
    }
    
    /**
     * 리소스 없음 응답 (404 Not Found)
     */
    public <T> ResponseEntity<ApiResponse<T>> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.notFound(message));
    }
    
    /**
     * 충돌 응답 (409 Conflict)
     */
    public <T> ResponseEntity<ApiResponse<T>> conflict(String message) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("CONFLICT", message));
    }
    
    /**
     * 내부 서버 오류 응답 (500 Internal Server Error)
     */
    public <T> ResponseEntity<ApiResponse<T>> internalError(String message) {
        log.error("내부 서버 오류: {}", message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.internalError(message));
    }
    
    /**
     * 내부 서버 오류 응답 (예외 포함)
     */
    public <T> ResponseEntity<ApiResponse<T>> internalError(String message, Exception exception) {
        log.error("내부 서버 오류: {}", message, exception);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("exception", exception.getClass().getSimpleName());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_SERVER_ERROR", message, metadata));
    }
    
    /**
     * 페이지 메타데이터 생성
     */
    private <T> Map<String, Object> createPageMetadata(Page<T> page) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("page", page.getNumber());
        metadata.put("size", page.getSize());
        metadata.put("totalElements", page.getTotalElements());
        metadata.put("totalPages", page.getTotalPages());
        metadata.put("first", page.isFirst());
        metadata.put("last", page.isLast());
        metadata.put("hasNext", page.hasNext());
        metadata.put("hasPrevious", page.hasPrevious());
        return metadata;
    }
    
    /**
     * ErrorCode에서 HTTP 상태 코드 매핑
     */
    private HttpStatus getHttpStatusFromErrorCode(ErrorCode errorCode) {
        return switch (errorCode) {
            case MEMBER_NOT_FOUND, ACCOUNT_NOT_FOUND, TRANSACTION_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case INVALID_CREDENTIALS, INVALID_PIN -> HttpStatus.UNAUTHORIZED;
            case INSUFFICIENT_BALANCE, SAME_ACCOUNT_TRANSFER -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}