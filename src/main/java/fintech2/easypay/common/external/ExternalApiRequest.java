package fintech2.easypay.common.external;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Map;

/**
 * 외부 API 요청 래퍼 클래스
 * 타임아웃, 재시도, 메타데이터 등을 포함
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ExternalApiRequest<T> {
    
    private final String apiName;
    private final T requestData;
    private final Duration timeout;
    private final int maxRetries;
    private final Map<String, String> headers;
    private final Map<String, Object> metadata;
    
    public static <T> Builder<T> builder(String apiName, T requestData) {
        return new Builder<>(apiName, requestData);
    }
    
    public static class Builder<T> {
        private final String apiName;
        private final T requestData;
        private Duration timeout = Duration.ofSeconds(30);
        private int maxRetries = 3;
        private Map<String, String> headers = Map.of();
        private Map<String, Object> metadata = Map.of();
        
        private Builder(String apiName, T requestData) {
            this.apiName = apiName;
            this.requestData = requestData;
        }
        
        public Builder<T> timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }
        
        public Builder<T> maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }
        
        public Builder<T> headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }
        
        public Builder<T> metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public ExternalApiRequest<T> build() {
            return new ExternalApiRequest<>(apiName, requestData, timeout, maxRetries, headers, metadata);
        }
    }
}