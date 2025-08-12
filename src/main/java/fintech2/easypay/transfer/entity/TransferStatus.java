package fintech2.easypay.transfer.entity;

public enum TransferStatus {
    REQUESTED,    // 요청됨
    PROCESSING,   // 처리중
    COMPLETED,    // 완료
    FAILED,       // 실패
    CANCELLED,    // 취소
    TIMEOUT,      // 타임아웃
    UNKNOWN;      // 알 수 없음
    
    /**
     * 성공류 상태인지 확인 (성공/처리중/타임아웃 등)
     */
    public boolean isSuccessLike() {
        return this == COMPLETED || this == PROCESSING;
    }
    
    /**
     * 실패 상태인지 확인
     */
    public boolean isFailure() {
        return this == FAILED || this == CANCELLED;
    }
    
    /**
     * 별도 확인이 필요한 상태인지 확인
     */
    public boolean needsConfirmation() {
        return this == TIMEOUT || this == UNKNOWN;
    }
    
    /**
     * 최종 상태인지 확인 (더 이상 처리가 필요없는 상태)
     */
    public boolean isFinal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }
}
