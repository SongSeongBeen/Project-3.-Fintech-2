package fintech2.easypay.common.enums;

public enum AuditResult {
    SUCCESS,
    FAIL,
    FAILURE, // PaymentAuditServiceImpl에서 사용
    ERROR,
    WARNING
} 