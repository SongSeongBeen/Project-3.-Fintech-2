package fintech2.easypay.audit.entity;

import fintech2.easypay.common.enums.AuditEventType;
import fintech2.easypay.common.enums.AuditResult;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AuditEventType eventType; // 이벤트 유형
    
    @Enumerated(EnumType.STRING)
    private AuditResult status; // result -> status
    
    private Long memberId; // userId -> memberId (payment/transfer 모듈과 호환)
    private String phoneNumber; // 휴대폰 번호
    
    private String eventDescription;
    
    @Column(columnDefinition = "TEXT")
    private String requestData; // oldValue -> requestData
    
    @Column(columnDefinition = "TEXT")
    private String responseData; // newValue -> responseData
    
    @Column(columnDefinition = "TEXT")
    private String errorMessage; // 오류 메시지
    
    private String ipAddress;
    private String userAgent;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
} 