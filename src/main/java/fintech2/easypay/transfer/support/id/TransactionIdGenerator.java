package fintech2.easypay.transfer.support.id;

/**
 * 거래 ID 생성 인터페이스
 * 다양한 거래 ID 생성 전략을 구현할 수 있도록 추상화
 */
public interface TransactionIdGenerator {
    
    /**
     * 고유한 거래 ID 생성
     * @return 생성된 거래 ID
     */
    String generate();
    
    /**
     * 특정 접두어를 포함한 거래 ID 생성
     * @param prefix 거래 ID 접두어
     * @return 생성된 거래 ID
     */
    String generateWithPrefix(String prefix);
    
    /**
     * 거래 ID 유효성 검증
     * @param transactionId 검증할 거래 ID
     * @return 유효한 경우 true
     */
    boolean isValid(String transactionId);
}