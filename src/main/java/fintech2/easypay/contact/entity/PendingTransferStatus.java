package fintech2.easypay.contact.entity;

public enum PendingTransferStatus {
    PENDING("대기중"),
    INVITATION_SENT("초대 전송됨"),
    RECEIVER_REGISTERED("수신자 가입됨"),
    COMPLETED("완료"),
    CANCELLED("취소됨"),
    EXPIRED("만료됨");
    
    private final String description;
    
    PendingTransferStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}