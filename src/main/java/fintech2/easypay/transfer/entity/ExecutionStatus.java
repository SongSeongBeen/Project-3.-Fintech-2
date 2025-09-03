package fintech2.easypay.transfer.entity;

public enum ExecutionStatus {
    PENDING("대기중"),
    PROCESSING("처리중"),
    SUCCESS("성공"),
    FAILED("실패"),
    RETRY("재시도 대기");
    
    private final String description;
    
    ExecutionStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}