package fintech2.easypay.common.audit;

import fintech2.easypay.audit.service.AuditLogService;
import fintech2.easypay.audit.service.NotificationService;
import fintech2.easypay.audit.service.AlarmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

/**
 * 감사 로그 AOP 처리
 * @Auditable 어노테이션이 적용된 메서드의 실행 전후에 감사 로그 및 알림을 처리합니다
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {
    
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final AlarmService alarmService;
    
    private final ExpressionParser parser = new SpelExpressionParser();
    
    @Around("@annotation(auditable)")
    public Object auditBusinessLogic(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        
        // SpEL 컨텍스트 준비
        EvaluationContext context = createEvaluationContext(joinPoint, null, null);
        
        // 사용자 정보 추출
        Long userId = extractUserId(auditable, context);
        String phoneNumber = extractPhoneNumber(auditable, context);
        
        log.debug("감사 로그 처리 시작: {} - 사용자ID: {}, 전화번호: {}", methodName, userId, phoneNumber);
        
        try {
            // 실제 비즈니스 로직 실행
            Object result = joinPoint.proceed();
            
            // 성공 처리
            handleSuccess(joinPoint, auditable, result, userId, phoneNumber, startTime);
            
            return result;
            
        } catch (Exception exception) {
            // 실패 처리
            handleFailure(joinPoint, auditable, exception, userId, phoneNumber, startTime);
            
            throw exception;
        }
    }
    
    /**
     * 성공 시 감사 로그 및 알림 처리
     */
    private void handleSuccess(ProceedingJoinPoint joinPoint, Auditable auditable, Object result, 
                             Long userId, String phoneNumber, long startTime) {
        try {
            long executionTime = System.currentTimeMillis() - startTime;
            
            // SpEL 컨텍스트 업데이트 (결과 포함)
            EvaluationContext context = createEvaluationContext(joinPoint, result, null);
            
            // 성공 메시지 생성
            String successMessage = evaluateMessage(auditable.successMessage(), context, 
                String.format("%s 성공", joinPoint.getSignature().getName()));
            
            // 감사 로그 기록
            if (userId != null) {
                auditLogService.logSuccess(
                    userId,
                    phoneNumber,
                    auditable.eventType(),
                    successMessage,
                    null, null,
                    buildContextInfo(joinPoint, result),
                    String.format("실행시간: %dms", executionTime)
                );
            }
            
            // 성공 알림 발송
            if (auditable.sendSuccessNotification() && userId != null) {
                String notificationMessage = evaluateMessage(auditable.successNotificationMessage(), context, successMessage);
                notificationService.sendTransferActivityNotification(userId, phoneNumber, notificationMessage);
            }
            
            // 시스템 알람 발송
            if (auditable.sendSystemAlarm() && userId != null) {
                alarmService.sendBusinessEvent(auditable.eventType().name(), userId.toString(), successMessage);
            }
            
            log.debug("성공 감사 로그 처리 완료: {} ({}ms)", joinPoint.getSignature().toShortString(), executionTime);
            
        } catch (Exception e) {
            log.error("성공 감사 로그 처리 중 오류 발생: {}", joinPoint.getSignature().toShortString(), e);
        }
    }
    
    /**
     * 실패 시 감사 로그 및 알림 처리
     */
    private void handleFailure(ProceedingJoinPoint joinPoint, Auditable auditable, Exception exception,
                             Long userId, String phoneNumber, long startTime) {
        try {
            long executionTime = System.currentTimeMillis() - startTime;
            
            // SpEL 컨텍스트 업데이트 (예외 포함)
            EvaluationContext context = createEvaluationContext(joinPoint, null, exception);
            
            // 실패 메시지 생성
            String failureMessage = evaluateMessage(auditable.failureMessage(), context, 
                String.format("%s 실패: %s", joinPoint.getSignature().getName(), exception.getMessage()));
            
            // 감사 로그 기록
            if (userId != null) {
                auditLogService.logFailure(
                    userId,
                    phoneNumber,
                    auditable.eventType(),
                    failureMessage,
                    null, null,
                    buildContextInfo(joinPoint, null),
                    exception.getMessage()
                );
            }
            
            // 실패 알림 발송
            if (auditable.sendFailureNotification() && userId != null) {
                String notificationMessage = evaluateMessage(auditable.failureNotificationMessage(), context, failureMessage);
                notificationService.sendTransferActivityNotification(userId, phoneNumber, notificationMessage);
            }
            
            // 시스템 알람 발송 (실패는 항상 발송)
            if (userId != null) {
                alarmService.sendBusinessEvent(auditable.eventType().name(), userId.toString(), failureMessage);
            }
            
            log.debug("실패 감사 로그 처리 완료: {} ({}ms)", joinPoint.getSignature().toShortString(), executionTime);
            
        } catch (Exception e) {
            log.error("실패 감사 로그 처리 중 오류 발생: {}", joinPoint.getSignature().toShortString(), e);
        }
    }
    
    /**
     * SpEL 평가 컨텍스트 생성
     */
    private EvaluationContext createEvaluationContext(ProceedingJoinPoint joinPoint, Object result, Exception exception) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        
        // 메서드 인자들
        context.setVariable("args", joinPoint.getArgs());
        
        // 결과 객체
        if (result != null) {
            context.setVariable("result", result);
        }
        
        // 예외 객체
        if (exception != null) {
            context.setVariable("exception", exception);
        }
        
        // 메서드 이름
        context.setVariable("methodName", joinPoint.getSignature().getName());
        
        return context;
    }
    
    /**
     * 사용자 ID 추출
     */
    private Long extractUserId(Auditable auditable, EvaluationContext context) {
        if (auditable.userIdExpression().isEmpty()) {
            return null;
        }
        
        try {
            Expression expression = parser.parseExpression(auditable.userIdExpression());
            Object value = expression.getValue(context);
            
            if (value instanceof Long) {
                return (Long) value;
            } else if (value instanceof Number) {
                return ((Number) value).longValue();
            } else if (value instanceof String) {
                return Long.parseLong((String) value);
            }
        } catch (Exception e) {
            log.warn("사용자 ID 추출 실패: {}", auditable.userIdExpression(), e);
        }
        
        return null;
    }
    
    /**
     * 휴대폰 번호 추출
     */
    private String extractPhoneNumber(Auditable auditable, EvaluationContext context) {
        if (auditable.phoneNumberExpression().isEmpty()) {
            return null;
        }
        
        try {
            Expression expression = parser.parseExpression(auditable.phoneNumberExpression());
            Object value = expression.getValue(context);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            log.warn("휴대폰 번호 추출 실패: {}", auditable.phoneNumberExpression(), e);
            return null;
        }
    }
    
    /**
     * 메시지 평가 (SpEL 지원)
     */
    private String evaluateMessage(String messageTemplate, EvaluationContext context, String defaultMessage) {
        if (messageTemplate.isEmpty()) {
            return defaultMessage;
        }
        
        try {
            Expression expression = parser.parseExpression(messageTemplate);
            Object value = expression.getValue(context);
            return value != null ? value.toString() : defaultMessage;
        } catch (Exception e) {
            log.warn("메시지 평가 실패: {}", messageTemplate, e);
            return defaultMessage;
        }
    }
    
    /**
     * 컨텍스트 정보 생성
     */
    private String buildContextInfo(ProceedingJoinPoint joinPoint, Object result) {
        StringBuilder contextBuilder = new StringBuilder();
        
        // 메서드 파라미터 정보
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            contextBuilder.append("파라미터: ");
            for (int i = 0; i < args.length; i++) {
                if (i > 0) contextBuilder.append(", ");
                contextBuilder.append(args[i] != null ? args[i].toString() : "null");
                if (contextBuilder.length() > 500) { // 너무 길면 잘라내기
                    contextBuilder.append("...");
                    break;
                }
            }
        }
        
        // 결과 정보 (간략하게)
        if (result != null) {
            if (contextBuilder.length() > 0) contextBuilder.append("; ");
            contextBuilder.append("결과타입: ").append(result.getClass().getSimpleName());
        }
        
        return contextBuilder.toString();
    }
}