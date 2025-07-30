package fintech2.easypay.audit.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 간단한 알림 서비스 구현체
 * 실제 알림 발송 대신 로그로 처리
 */
@Service
@Slf4j
public class SimpleNotificationService implements NotificationService {
    
    @Override
    public void sendPaymentActivityNotification(Long userId, String phoneNumber, String message) {
        log.info("결제 알림 - 사용자ID: {}, 전화번호: {}, 메시지: {}", userId, phoneNumber, message);
        // 실제 구현에서는 SMS, 푸시 알림 등을 전송
    }
    
    @Override
    public void sendTransferActivityNotification(Long userId, String phoneNumber, String message) {
        log.info("송금 알림 - 사용자ID: {}, 전화번호: {}, 메시지: {}", userId, phoneNumber, message);
        // 실제 구현에서는 SMS, 푸시 알림 등을 전송
    }
    
    @Override
    public void sendSecurityAlert(Long userId, String phoneNumber, String message) {
        log.warn("보안 알림 - 사용자ID: {}, 전화번호: {}, 메시지: {}", userId, phoneNumber, message);
        // 실제 구현에서는 긴급 SMS, 이메일 등을 전송
    }
}