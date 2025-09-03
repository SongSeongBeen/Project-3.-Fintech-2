package fintech2.easypay.contact.external;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {
    
    public boolean sendInvitationSms(String phoneNumber, String senderName, 
                                     BigDecimal amount, String invitationCode) {
        try {
            String message = buildInvitationMessage(senderName, amount, invitationCode);
            
            // 실제 환경에서는 SMS API 호출
            // 예: Twilio, Naver Cloud Platform SMS, KT 비즈메카 등
            
            log.info("SMS 발송: {} -> {}", phoneNumber, message);
            
            // Mock 구현 - 항상 성공
            return true;
            
        } catch (Exception e) {
            log.error("SMS 발송 실패: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean sendTransferNotification(String phoneNumber, String message) {
        try {
            log.info("송금 알림 SMS 발송: {} -> {}", phoneNumber, message);
            
            // Mock 구현 - 항상 성공
            return true;
            
        } catch (Exception e) {
            log.error("SMS 발송 실패: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean sendCancellationNotification(String phoneNumber, String senderName, 
                                                BigDecimal amount, String reason) {
        try {
            String message = buildCancellationMessage(senderName, amount, reason);
            
            log.info("취소 알림 SMS 발송: {} -> {}", phoneNumber, message);
            
            // Mock 구현 - 항상 성공
            return true;
            
        } catch (Exception e) {
            log.error("SMS 발송 실패: {}", e.getMessage());
            return false;
        }
    }
    
    private String buildInvitationMessage(String senderName, BigDecimal amount, String invitationCode) {
        StringBuilder sb = new StringBuilder();
        sb.append("[EasyPay] ");
        sb.append(senderName).append("님이 ");
        sb.append(String.format("%,d", amount.intValue())).append("원을 보내셨습니다.\n");
        sb.append("24시간 내에 EasyPay에 가입하여 수령하세요.\n");
        sb.append("초대코드: ").append(invitationCode).append("\n");
        sb.append("앱 다운로드: https://easypay.link/download");
        
        return sb.toString();
    }
    
    private String buildCancellationMessage(String senderName, BigDecimal amount, String reason) {
        StringBuilder sb = new StringBuilder();
        sb.append("[EasyPay] ");
        sb.append(senderName).append("님이 보낸 ");
        sb.append(String.format("%,d", amount.intValue())).append("원이 취소되었습니다.\n");
        sb.append("사유: ").append(reason);
        
        return sb.toString();
    }
    
    public String generateInvitationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        
        return code.toString();
    }
}