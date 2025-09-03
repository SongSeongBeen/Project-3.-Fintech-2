package fintech2.easypay.common.enums;

/**
 * 감사 이벤트 유형
 */
public enum AuditEventType {
    // 결제 관련 이벤트
    PAYMENT_REQUEST("결제 요청"),
    PAYMENT_SUCCESS("결제 성공"),
    PAYMENT_FAILED("결제 실패"),
    PAYMENT_CANCELLED("결제 취소"),
    PAYMENT_REFUNDED("결제 환불"),
    PAYMENT_CANCEL("결제 취소"), // PaymentService에서 사용
    PAYMENT_REFUND("결제 환불"), // PaymentService에서 사용
    
    // 송금 관련 이벤트
    TRANSFER_REQUEST("송금 요청"),
    TRANSFER_SUCCESS("송금 성공"),
    TRANSFER_FAILED("송금 실패"),
    TRANSFER_CANCELLED("송금 취소"),
    TRANSFER_PENDING("송금 대기중"),
    
    // 예약 송금 관련 이벤트
    SCHEDULED_TRANSFER_CREATED("예약 송금 생성"),
    SCHEDULED_TRANSFER_UPDATED("예약 송금 수정"),
    SCHEDULED_TRANSFER_CANCELLED("예약 송금 취소"),
    SCHEDULED_TRANSFER_EXECUTED("예약 송금 실행"),
    
    // 계좌 관련 이벤트
    ACCOUNT_CREATED("계좌 생성"),
    ACCOUNT_DEPOSIT("계좌 입금"),
    ACCOUNT_WITHDRAW("계좌 출금"),
    
    // 인증 관련 이벤트
    LOGIN_ATTEMPT("로그인 시도"),
    LOGIN_SUCCESS("로그인 성공"),
    LOGIN_FAILED("로그인 실패"),
    LOGOUT("로그아웃");
    
    private final String description;
    
    AuditEventType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}