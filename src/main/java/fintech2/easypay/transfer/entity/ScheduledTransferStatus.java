package fintech2.easypay.transfer.entity;

public enum ScheduledTransferStatus {
    ACTIVE("활성"),
    PAUSED("일시정지"),
    COMPLETED("완료"),
    CANCELLED("취소"),
    FAILED("실패");
    
    private final String description;
    
    ScheduledTransferStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}