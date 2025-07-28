package fintech2.easypay.audit.entity;

public enum NotificationType {
    ACCOUNT_ACTIVITY,    // 계좌 활동
    TRANSFER_ACTIVITY,   // 송금 활동
    PAYMENT_ACTIVITY,    // 결제 활동
    SECURITY_ALERT,      // 보안 알림
    SYSTEM_MAINTENANCE,  // 시스템 점검
    PROMOTIONAL         // 프로모션
}
