package fintech2.easypay.audit.service;

/**
 * 알림 서비스 인터페이스
 * 결제, 송금 등의 이벤트 발생 시 사용자에게 알림을 전송
 */
public interface NotificationService {
    
    /**
     * 결제 활동 알림 전송
     * @param userId 사용자 ID
     * @param phoneNumber 휴대폰 번호
     * @param message 알림 메시지
     */
    void sendPaymentActivityNotification(Long userId, String phoneNumber, String message);
    
    /**
     * 송금 활동 알림 전송
     * @param userId 사용자 ID
     * @param phoneNumber 휴대폰 번호
     * @param message 알림 메시지
     */
    void sendTransferActivityNotification(Long userId, String phoneNumber, String message);
    
    /**
     * 보안 알림 전송
     * @param userId 사용자 ID
     * @param phoneNumber 휴대폰 번호
     * @param message 보안 관련 메시지
     */
    void sendSecurityAlert(Long userId, String phoneNumber, String message);
}