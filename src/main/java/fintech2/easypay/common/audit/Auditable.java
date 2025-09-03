package fintech2.easypay.common.audit;

import fintech2.easypay.common.enums.AuditEventType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 감사 로그 및 알림 처리를 위한 어노테이션
 * 메서드에 적용하면 자동으로 성공/실패 감사 로그가 기록되고 알림이 발송됩니다
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    
    /**
     * 감사 이벤트 타입
     */
    AuditEventType eventType();
    
    /**
     * 성공 시 로그 메시지 템플릿
     * SpEL 표현식 사용 가능: #{args[0]}, #{result.amount} 등
     */
    String successMessage() default "";
    
    /**
     * 실패 시 로그 메시지 템플릿
     * SpEL 표현식 사용 가능: #{exception.message} 등
     */
    String failureMessage() default "";
    
    /**
     * 성공 시 알림 발송 여부
     */
    boolean sendSuccessNotification() default false;
    
    /**
     * 실패 시 알림 발송 여부
     */
    boolean sendFailureNotification() default true;
    
    /**
     * 성공 시 알림 메시지 템플릿
     */
    String successNotificationMessage() default "";
    
    /**
     * 실패 시 알림 메시지 템플릿
     */
    String failureNotificationMessage() default "";
    
    /**
     * 시스템 알람 발송 여부
     */
    boolean sendSystemAlarm() default false;
    
    /**
     * 사용자 ID를 추출하기 위한 SpEL 표현식
     * 예: "#{args[0]}" (첫 번째 파라미터가 사용자 ID)
     *     "#{T(fintech2.easypay.common.util.SecurityUtils).getCurrentUserId()}"
     */
    String userIdExpression() default "";
    
    /**
     * 휴대폰 번호를 추출하기 위한 SpEL 표현식
     * 예: "#{args[0]}" (첫 번째 파라미터가 휴대폰 번호)
     */
    String phoneNumberExpression() default "";
}