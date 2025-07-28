package fintech2.easypay.audit.service;

import fintech2.easypay.audit.entity.AuditEventType;
import fintech2.easypay.audit.entity.AuditLog;
import fintech2.easypay.audit.entity.AuditStatus;
import fintech2.easypay.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuditLogService {
    
    private final AuditLogRepository auditLogRepository;
    
    @Async("auditExecutor")
    @Transactional
    public void logEvent(Long memberId, String phoneNumber, AuditEventType eventType, 
                        String eventDescription, String ipAddress, String userAgent,
                        String requestData, String responseData, AuditStatus status, 
                        String errorMessage) {
        
        AuditLog auditLog = AuditLog.builder()
                .memberId(memberId)
                .phoneNumber(phoneNumber)
                .eventType(eventType)
                .eventDescription(eventDescription)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .requestData(requestData)
                .responseData(responseData)
                .status(status)
                .errorMessage(errorMessage)
                .build();
        
        auditLogRepository.save(auditLog);
        
        log.info("감사 로그 기록: {} - {} - {}", phoneNumber, eventType, eventDescription);
    }
    
    @Async("auditExecutor")
    @Transactional
    public void logSuccess(Long memberId, String phoneNumber, AuditEventType eventType, 
                          String eventDescription, String ipAddress, String userAgent,
                          String requestData, String responseData) {
        logEvent(memberId, phoneNumber, eventType, eventDescription, ipAddress, userAgent,
                requestData, responseData, AuditStatus.SUCCESS, null);
    }
    
    @Async("auditExecutor")
    @Transactional
    public void logFailure(Long memberId, String phoneNumber, AuditEventType eventType, 
                          String eventDescription, String ipAddress, String userAgent,
                          String requestData, String errorMessage) {
        logEvent(memberId, phoneNumber, eventType, eventDescription, ipAddress, userAgent,
                requestData, null, AuditStatus.FAILED, errorMessage);
    }
    
    @Async("auditExecutor")
    @Transactional
    public void logError(Long memberId, String phoneNumber, AuditEventType eventType, 
                        String eventDescription, String ipAddress, String userAgent,
                        String requestData, String errorMessage) {
        logEvent(memberId, phoneNumber, eventType, eventDescription, ipAddress, userAgent,
                requestData, null, AuditStatus.ERROR, errorMessage);
    }
    
    public Page<AuditLog> getAuditLogs(Long memberId, Pageable pageable) {
        return auditLogRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable);
    }
    
    public Page<AuditLog> getAuditLogsByPhoneNumber(String phoneNumber, Pageable pageable) {
        return auditLogRepository.findByPhoneNumberOrderByCreatedAtDesc(phoneNumber, pageable);
    }
    
    public Page<AuditLog> getAuditLogsByEventType(AuditEventType eventType, Pageable pageable) {
        return auditLogRepository.findByEventTypeOrderByCreatedAtDesc(eventType, pageable);
    }
    
    public Page<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.findByCreatedAtBetween(startDate, endDate, pageable);
    }
}
