package fintech2.easypay.transfer.support.id;

import fintech2.easypay.transfer.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * UUID 기반 거래 ID 생성기
 * TXN 접두어 + 날짜 + UUID 조합으로 고유한 거래 ID 생성
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UuidTransactionIdGenerator implements TransactionIdGenerator {
    
    private static final String DEFAULT_PREFIX = "TXN";
    private static final Pattern TRANSACTION_ID_PATTERN = 
        Pattern.compile("^[A-Z]{3}\\d{8}[A-Z0-9]{12}$");
    
    private final TransferRepository transferRepository;
    
    @Override
    public String generate() {
        return generateWithPrefix(DEFAULT_PREFIX);
    }
    
    @Override
    public String generateWithPrefix(String prefix) {
        String transactionId;
        int attempts = 0;
        final int maxAttempts = 10;
        
        do {
            transactionId = createTransactionId(prefix);
            attempts++;
            
            if (attempts >= maxAttempts) {
                throw new IllegalStateException(
                    "거래 ID 생성 실패: 최대 시도 횟수 초과");
            }
        } while (transferRepository.existsByTransactionId(transactionId));
        
        log.debug("거래 ID 생성 완료: {}", transactionId);
        return transactionId;
    }
    
    @Override
    public boolean isValid(String transactionId) {
        if (transactionId == null || transactionId.isEmpty()) {
            return false;
        }
        
        // 기본 패턴 검증
        return TRANSACTION_ID_PATTERN.matcher(transactionId).matches();
    }
    
    /**
     * 실제 거래 ID 생성 로직
     * 형식: [PREFIX][YYYYMMDD][UUID-12자리]
     * 예: TXN20240110A1B2C3D4E5F6
     */
    private String createTransactionId(String prefix) {
        // 날짜 부분 (YYYYMMDD)
        String datePart = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // UUID 부분 (12자리)
        String uuidPart = UUID.randomUUID()
            .toString()
            .replace("-", "")
            .substring(0, 12)
            .toUpperCase();
        
        return prefix + datePart + uuidPart;
    }
}