package fintech2.easypay.transfer.entity;

import fintech2.easypay.auth.entity.User;
import fintech2.easypay.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "scheduled_transfers",
    indexes = {
        @Index(name = "idx_sender_status", columnList = "sender_id, status"),
        @Index(name = "idx_next_execution", columnList = "next_execution_time"),
        @Index(name = "idx_status", columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledTransfer extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "schedule_id", unique = true, nullable = false)
    private String scheduleId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;
    
    @Column(name = "sender_account_number", nullable = false)
    private String senderAccountNumber;
    
    @Column(name = "receiver_account_number", nullable = false)
    private String receiverAccountNumber;
    
    @Column(name = "receiver_name")
    private String receiverName;
    
    @Column(name = "receiver_phone_number")
    private String receiverPhoneNumber;
    
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "memo")
    private String memo;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", nullable = false)
    private ScheduleType scheduleType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "repeat_cycle")
    private RepeatCycle repeatCycle;
    
    @Column(name = "repeat_day_of_month")
    private Integer repeatDayOfMonth;
    
    @Column(name = "repeat_day_of_week")
    private Integer repeatDayOfWeek;
    
    @Column(name = "execution_time")
    private LocalTime executionTime;
    
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;
    
    @Column(name = "end_date")
    private LocalDateTime endDate;
    
    @Column(name = "next_execution_time")
    private LocalDateTime nextExecutionTime;
    
    @Column(name = "last_execution_time")
    private LocalDateTime lastExecutionTime;
    
    @Column(name = "execution_count")
    @Builder.Default
    private Integer executionCount = 0;
    
    @Column(name = "max_execution_count")
    private Integer maxExecutionCount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ScheduledTransferStatus status = ScheduledTransferStatus.ACTIVE;
    
    @Column(name = "failure_count")
    @Builder.Default
    private Integer failureCount = 0;
    
    @Column(name = "last_failure_reason")
    private String lastFailureReason;
    
    @Column(name = "notification_enabled")
    @Builder.Default
    private boolean notificationEnabled = true;
    
    @Column(name = "notification_minutes_before")
    @Builder.Default
    private Integer notificationMinutesBefore = 30;
    
    public void incrementExecutionCount() {
        this.executionCount++;
        this.lastExecutionTime = LocalDateTime.now();
    }
    
    public void incrementFailureCount(String reason) {
        this.failureCount++;
        this.lastFailureReason = reason;
        
        if (this.failureCount >= 3) {
            this.status = ScheduledTransferStatus.FAILED;
        }
    }
    
    public void resetFailureCount() {
        this.failureCount = 0;
        this.lastFailureReason = null;
    }
    
    public void complete() {
        if (this.scheduleType == ScheduleType.ONE_TIME) {
            this.status = ScheduledTransferStatus.COMPLETED;
        } else if (this.maxExecutionCount != null && this.executionCount >= this.maxExecutionCount) {
            this.status = ScheduledTransferStatus.COMPLETED;
        }
    }
    
    public void cancel() {
        this.status = ScheduledTransferStatus.CANCELLED;
    }
    
    public void pause() {
        this.status = ScheduledTransferStatus.PAUSED;
    }
    
    public void resume() {
        this.status = ScheduledTransferStatus.ACTIVE;
        this.failureCount = 0;
    }
    
    public boolean isActive() {
        return this.status == ScheduledTransferStatus.ACTIVE;
    }
    
    public boolean shouldExecute() {
        if (!isActive()) return false;
        if (this.nextExecutionTime == null) return false;
        if (LocalDateTime.now().isBefore(this.nextExecutionTime)) return false;
        if (this.endDate != null && LocalDateTime.now().isAfter(this.endDate)) {
            this.status = ScheduledTransferStatus.COMPLETED;
            return false;
        }
        return true;
    }
}