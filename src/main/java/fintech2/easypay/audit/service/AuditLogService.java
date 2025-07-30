package fintech2.easypay.audit.service;

import fintech2.easypay.audit.entity.AuditLog;
import fintech2.easypay.audit.repository.AuditLogRepository;
import fintech2.easypay.common.enums.AuditResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final AlarmService alarmService;

    @Transactional
    public void logSuccess(Long userId, String action, String resourceType, String resourceId, String oldValue, String newValue) {
        AuditLog log = createAuditLog(userId, action, resourceType, resourceId, oldValue, newValue, AuditResult.SUCCESS);
        auditLogRepository.save(log);
        
        // 중요 비즈니스 이벤트는 알람 전송
        if (isImportantEvent(action)) {
            alarmService.sendBusinessEvent(action, userId != null ? userId.toString() : "anonymous", action + " 완료");
        }
    }

    @Transactional
    public void logSuccess(String action, String resourceType, String resourceId, String description, Map<String, Object> details) {
        AuditLog log = createAuditLog(null, action, resourceType, resourceId, null, description, AuditResult.SUCCESS);
        auditLogRepository.save(log);
        
        // 중요 비즈니스 이벤트는 알람 전송
        if (isImportantEvent(action)) {
            alarmService.sendBusinessEvent(action, "system", description);
        }
    }

    @Transactional
    public void logSuccess(Long userId, String phoneNumber, fintech2.easypay.common.enums.AuditEventType eventType, String description, String resourceType, String resourceId, String requestData, String responseData) {
        AuditLog log = new AuditLog();
        log.setMemberId(userId);
        log.setPhoneNumber(phoneNumber);
        log.setEventType(eventType);
        log.setEventDescription(description);
        log.setRequestData(requestData);
        log.setResponseData(responseData);
        log.setStatus(AuditResult.SUCCESS);
        auditLogRepository.save(log);
        
        // 중요 비즈니스 이벤트는 알람 전송
        if (isImportantEvent(eventType.getDescription())) {
            alarmService.sendBusinessEvent(eventType.getDescription(), userId != null ? userId.toString() : "anonymous", description);
        }
    }

    @Transactional
    public void logError(Long userId, String action, String resourceType, String resourceId, String error) {
        AuditLog log = createAuditLog(userId, action, resourceType, resourceId, null, error, AuditResult.ERROR);
        auditLogRepository.save(log);
        
        // 에러는 항상 알람
        alarmService.sendSystemAlert(resourceType, error, null);
    }

    @Transactional
    public void logError(String action, String resourceType, String resourceId, String description, Exception exception) {
        AuditLog log = createAuditLog(null, action, resourceType, resourceId, null, description, AuditResult.ERROR);
        auditLogRepository.save(log);
        
        // 에러는 항상 알람
        alarmService.sendSystemAlert(resourceType, description, exception);
    }

    @Transactional
    public void logWarning(Long userId, String action, String resourceType, String resourceId, String warning) {
        AuditLog log = createAuditLog(userId, action, resourceType, resourceId, null, warning, AuditResult.WARNING);
        auditLogRepository.save(log);
    }

    @Transactional
    public void logWarning(String action, String resourceType, String resourceId, String description) {
        AuditLog log = createAuditLog(null, action, resourceType, resourceId, null, description, AuditResult.WARNING);
        auditLogRepository.save(log);
    }

    @Transactional
    public void logFailure(Long userId, String phoneNumber, fintech2.easypay.common.enums.AuditEventType eventType, String description, String resourceType, String resourceId, String requestData, String errorMessage) {
        AuditLog log = new AuditLog();
        log.setMemberId(userId);
        log.setPhoneNumber(phoneNumber);
        log.setEventType(eventType);
        log.setEventDescription(description);
        log.setRequestData(requestData);
        log.setErrorMessage(errorMessage);
        log.setStatus(AuditResult.FAIL);
        auditLogRepository.save(log);
        
        // 실패는 항상 알람
        alarmService.sendSystemAlert(eventType.getDescription(), description, null);
    }

    @Transactional
    public void logError(Long userId, String phoneNumber, fintech2.easypay.common.enums.AuditEventType eventType, String description, String resourceType, String resourceId, String requestData, String errorMessage) {
        AuditLog log = new AuditLog();
        log.setMemberId(userId);
        log.setPhoneNumber(phoneNumber);
        log.setEventType(eventType);
        log.setEventDescription(description);
        log.setRequestData(requestData);
        log.setErrorMessage(errorMessage);
        log.setStatus(AuditResult.ERROR);
        auditLogRepository.save(log);
        
        // 에러는 항상 알람
        alarmService.sendSystemAlert(eventType.getDescription(), description, null);
    }

    private AuditLog createAuditLog(Long userId, String action, String resourceType, String resourceId, String oldValue, String newValue, AuditResult result) {
        AuditLog log = new AuditLog();
        log.setMemberId(userId);
        log.setEventDescription(action);
        // resourceType은 AuditEventType enum으로 변환 필요하지만 일단 생략
        log.setRequestData(oldValue);
        log.setResponseData(newValue);
        log.setStatus(result);
        return log;
    }

    private boolean isImportantEvent(String action) {
        return action.contains("REGISTER") || action.contains("LOGIN") || action.contains("BALANCE_UPDATE") || action.contains("ACCOUNT_CREATE");
    }
} 