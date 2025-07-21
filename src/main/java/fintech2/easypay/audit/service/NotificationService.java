package fintech2.easypay.audit.service;

import fintech2.easypay.audit.entity.*;
import fintech2.easypay.audit.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    
    @Async("notificationExecutor")
    @Transactional
    public void sendNotification(Long memberId, String phoneNumber, NotificationType type,
                               String title, String message, NotificationChannel channel) {
        
        Notification notification = Notification.builder()
                .memberId(memberId)
                .phoneNumber(phoneNumber)
                .notificationType(type)
                .title(title)
                .message(message)
                .channel(channel)
                .build();
        
        Notification savedNotification = notificationRepository.save(notification);
        
        // 실제 알림 전송 로직 (모킹)
        try {
            switch (channel) {
                case EMAIL:
                    sendEmailNotification(phoneNumber, title, message);
                    break;
                case SMS:
                    sendSmsNotification(phoneNumber, title, message);
                    break;
                case PUSH:
                    sendPushNotification(phoneNumber, title, message);
                    break;
                case SLACK:
                    sendSlackNotification(phoneNumber, title, message);
                    break;
                case WEBHOOK:
                    sendWebhookNotification(phoneNumber, title, message);
                    break;
            }
            
            savedNotification.markAsSent();
            log.info("알림 전송 성공: {} - {} - {}", phoneNumber, channel, title);
            
        } catch (Exception e) {
            savedNotification.markAsFailed(e.getMessage());
            log.error("알림 전송 실패: {} - {} - {}", phoneNumber, channel, title, e);
        }
    }
    
    // 계좌 활동 알림
    @Async("notificationExecutor")
    @Transactional
    public void sendAccountActivityNotification(Long memberId, String phoneNumber, String message) {
        sendNotification(memberId, phoneNumber, NotificationType.ACCOUNT_ACTIVITY,
                        "계좌 활동 알림", message, NotificationChannel.PUSH);
    }
    
    // 송금 활동 알림
    @Async("notificationExecutor")
    @Transactional
    public void sendTransferActivityNotification(Long memberId, String phoneNumber, String message) {
        sendNotification(memberId, phoneNumber, NotificationType.TRANSFER_ACTIVITY,
                        "송금 활동 알림", message, NotificationChannel.PUSH);
    }
    
    // 결제 활동 알림
    @Async("notificationExecutor")
    @Transactional
    public void sendPaymentActivityNotification(Long memberId, String phoneNumber, String message) {
        sendNotification(memberId, phoneNumber, NotificationType.PAYMENT_ACTIVITY,
                        "결제 활동 알림", message, NotificationChannel.PUSH);
    }
    
    // 보안 알림
    @Async("notificationExecutor")
    @Transactional
    public void sendSecurityAlert(Long memberId, String phoneNumber, String message) {
        sendNotification(memberId, phoneNumber, NotificationType.SECURITY_ALERT,
                        "보안 알림", message, NotificationChannel.EMAIL);
        
        // 슬랙으로도 전송
        sendNotification(memberId, phoneNumber, NotificationType.SECURITY_ALERT,
                        "보안 알림", message, NotificationChannel.SLACK);
    }
    
    public Page<Notification> getNotifications(Long memberId, Pageable pageable) {
        return notificationRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable);
    }
    
    public List<Notification> getPendingNotifications() {
        return notificationRepository.findByStatusOrderByCreatedAtAsc(NotificationStatus.PENDING);
    }
    
    // 모킹된 알림 전송 메서드들
    private void sendEmailNotification(String phoneNumber, String title, String message) {
        log.info("[EMAIL] To: {}, Title: {}, Message: {}", phoneNumber, title, message);
        // 실제 이메일 전송 로직
    }
    
    private void sendSmsNotification(String phoneNumber, String title, String message) {
        log.info("[SMS] To: {}, Title: {}, Message: {}", phoneNumber, title, message);
        // 실제 SMS 전송 로직
    }
    
    private void sendPushNotification(String phoneNumber, String title, String message) {
        log.info("[PUSH] To: {}, Title: {}, Message: {}", phoneNumber, title, message);
        // 실제 푸시 알림 전송 로직
    }
    
    private void sendSlackNotification(String phoneNumber, String title, String message) {
        log.info("[SLACK] To: {}, Title: {}, Message: {}", phoneNumber, title, message);
        // 실제 슬랙 알림 전송 로직
    }
    
    private void sendWebhookNotification(String phoneNumber, String title, String message) {
        log.info("[WEBHOOK] To: {}, Title: {}, Message: {}", phoneNumber, title, message);
        // 실제 웹훅 전송 로직
    }
}
