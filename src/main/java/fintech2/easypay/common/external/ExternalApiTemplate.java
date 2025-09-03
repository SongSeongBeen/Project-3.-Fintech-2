package fintech2.easypay.common.external;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 외부 API 호출 템플릿
 * 공통 타임아웃, 재시도, 로깅, 서킷 브레이커 기능을 제공
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExternalApiTemplate {
    
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Map<String, CircuitBreakerState> circuitBreakers = new ConcurrentHashMap<>();
    
    /**
     * 외부 API 호출 (동기식)
     */
    public <T, R> ExternalApiResponse<R> callExternalApi(
            ExternalApiRequest<T> request,
            Function<T, R> apiCall,
            Function<Exception, R> fallbackHandler) {
        
        long startTime = System.currentTimeMillis();
        String apiName = request.getApiName();
        int retryCount = 0;
        
        log.info("외부 API 호출 시작: {} (타임아웃: {}ms, 최대재시도: {})", 
            apiName, request.getTimeout().toMillis(), request.getMaxRetries());
        
        // 서킷 브레이커 상태 확인
        CircuitBreakerState circuitBreaker = getCircuitBreaker(apiName);
        if (circuitBreaker.isOpen()) {
            log.warn("서킷 브레이커 열림 상태: {} - fallback 처리", apiName);
            return handleFallback(request, fallbackHandler, startTime, retryCount, 
                "CIRCUIT_BREAKER_OPEN", "서킷 브레이커가 열린 상태입니다");
        }
        
        Exception lastException = null;
        
        // 재시도 로직
        for (int attempt = 0; attempt <= request.getMaxRetries(); attempt++) {
            retryCount = attempt;
            
            try {
                log.debug("API 호출 시도 {}/{}: {}", attempt + 1, request.getMaxRetries() + 1, apiName);
                
                // 타임아웃 적용하여 API 호출
                R result = callWithTimeout(request, apiCall);
                
                long executionTime = System.currentTimeMillis() - startTime;
                
                // 성공 시 서킷 브레이커 상태 개선
                circuitBreaker.recordSuccess();
                
                log.info("외부 API 호출 성공: {} ({}ms, {}회 시도)", apiName, executionTime, attempt + 1);
                
                Map<String, Object> metadata = createSuccessMetadata(request, attempt);
                return ExternalApiResponse.success(result, executionTime, retryCount, metadata);
                
            } catch (TimeoutException e) {
                lastException = e;
                log.warn("API 호출 타임아웃: {} (시도 {}/{})", apiName, attempt + 1, request.getMaxRetries() + 1);
                
                if (attempt < request.getMaxRetries()) {
                    sleepBetweenRetries(attempt);
                }
                
            } catch (Exception e) {
                lastException = e;
                log.warn("API 호출 실패: {} (시도 {}/{}) - {}", 
                    apiName, attempt + 1, request.getMaxRetries() + 1, e.getMessage());
                
                // 서킷 브레이커에 실패 기록
                circuitBreaker.recordFailure();
                
                if (attempt < request.getMaxRetries()) {
                    sleepBetweenRetries(attempt);
                }
            }
        }
        
        // 모든 재시도 실패 후 처리
        long executionTime = System.currentTimeMillis() - startTime;
        
        log.error("외부 API 호출 최종 실패: {} ({}ms, {}회 시도) - {}", 
            apiName, executionTime, retryCount + 1, lastException.getMessage());
        
        return handleFallback(request, fallbackHandler, startTime, retryCount, 
            getErrorCode(lastException), lastException.getMessage());
    }
    
    /**
     * 외부 API 호출 (비동기식)
     */
    public <T, R> CompletableFuture<ExternalApiResponse<R>> callExternalApiAsync(
            ExternalApiRequest<T> request,
            Function<T, R> apiCall,
            Function<Exception, R> fallbackHandler) {
        
        return CompletableFuture.supplyAsync(() -> 
            callExternalApi(request, apiCall, fallbackHandler), executorService);
    }
    
    /**
     * 타임아웃을 적용한 API 호출
     */
    private <T, R> R callWithTimeout(ExternalApiRequest<T> request, Function<T, R> apiCall) 
            throws TimeoutException, ExecutionException, InterruptedException {
        
        CompletableFuture<R> future = CompletableFuture.supplyAsync(() -> 
            apiCall.apply(request.getRequestData()), executorService);
        
        try {
            return future.get(request.getTimeout().toMillis(), TimeUnit.MILLISECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            future.cancel(true);
            throw new TimeoutException("API 호출 타임아웃: " + request.getApiName());
        }
    }
    
    /**
     * Fallback 처리
     */
    private <T, R> ExternalApiResponse<R> handleFallback(
            ExternalApiRequest<T> request,
            Function<Exception, R> fallbackHandler,
            long startTime,
            int retryCount,
            String errorCode,
            String errorMessage) {
        
        if (fallbackHandler != null) {
            try {
                R fallbackResult = fallbackHandler.apply(new RuntimeException(errorMessage));
                long executionTime = System.currentTimeMillis() - startTime;
                
                Map<String, Object> metadata = createFailureMetadata(request, retryCount, true);
                return ExternalApiResponse.success(fallbackResult, executionTime, retryCount, metadata);
                
            } catch (Exception fallbackException) {
                log.error("Fallback 처리 실패: {} - {}", request.getApiName(), fallbackException.getMessage());
            }
        }
        
        long executionTime = System.currentTimeMillis() - startTime;
        Map<String, Object> metadata = createFailureMetadata(request, retryCount, false);
        
        return ExternalApiResponse.failure(errorCode, errorMessage, executionTime, retryCount, metadata);
    }
    
    /**
     * 서킷 브레이커 가져오기 또는 생성
     */
    private CircuitBreakerState getCircuitBreaker(String apiName) {
        return circuitBreakers.computeIfAbsent(apiName, k -> new CircuitBreakerState());
    }
    
    /**
     * 재시도 간 대기
     */
    private void sleepBetweenRetries(int attempt) {
        try {
            long delay = (long) Math.pow(2, attempt) * 1000; // 지수 백오프
            Thread.sleep(Math.min(delay, 10000)); // 최대 10초
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 예외에서 에러 코드 추출
     */
    private String getErrorCode(Exception exception) {
        if (exception instanceof TimeoutException) {
            return "TIMEOUT";
        } else if (exception instanceof java.net.ConnectException) {
            return "CONNECTION_FAILED";
        } else if (exception instanceof ExecutionException) {
            return "EXECUTION_FAILED";
        } else {
            return "UNKNOWN_ERROR";
        }
    }
    
    /**
     * 성공 메타데이터 생성
     */
    private <T> Map<String, Object> createSuccessMetadata(ExternalApiRequest<T> request, int retryAttempt) {
        Map<String, Object> metadata = new HashMap<>(request.getMetadata());
        metadata.put("retryAttempt", retryAttempt);
        metadata.put("timeout", request.getTimeout().toString());
        metadata.put("maxRetries", request.getMaxRetries());
        return metadata;
    }
    
    /**
     * 실패 메타데이터 생성
     */
    private <T> Map<String, Object> createFailureMetadata(ExternalApiRequest<T> request, int retryCount, boolean fallbackUsed) {
        Map<String, Object> metadata = new HashMap<>(request.getMetadata());
        metadata.put("retryCount", retryCount);
        metadata.put("timeout", request.getTimeout().toString());
        metadata.put("maxRetries", request.getMaxRetries());
        metadata.put("fallbackUsed", fallbackUsed);
        return metadata;
    }
    
    /**
     * 간단한 서킷 브레이커 구현
     */
    private static class CircuitBreakerState {
        private volatile int failureCount = 0;
        private volatile long lastFailureTime = 0;
        private volatile boolean isOpen = false;
        
        private static final int FAILURE_THRESHOLD = 5;
        private static final long TIMEOUT_MS = 60000; // 1분
        
        public boolean isOpen() {
            if (isOpen && (System.currentTimeMillis() - lastFailureTime > TIMEOUT_MS)) {
                isOpen = false;
                failureCount = 0;
            }
            return isOpen;
        }
        
        public void recordSuccess() {
            failureCount = 0;
            isOpen = false;
        }
        
        public void recordFailure() {
            failureCount++;
            lastFailureTime = System.currentTimeMillis();
            if (failureCount >= FAILURE_THRESHOLD) {
                isOpen = true;
            }
        }
    }
}