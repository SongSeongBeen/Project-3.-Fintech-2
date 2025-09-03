package fintech2.easypay.transfer.dto;

import fintech2.easypay.transfer.entity.ScheduledTransfer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledTransferResponse {
    private String scheduleId;
    private String senderAccountNumber;
    private String receiverAccountNumber;
    private String receiverName;
    private BigDecimal amount;
    private String memo;
    private String scheduleType;
    private String repeatCycle;
    private Integer repeatDayOfMonth;
    private Integer repeatDayOfWeek;
    private LocalTime executionTime;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime nextExecutionTime;
    private LocalDateTime lastExecutionTime;
    private Integer executionCount;
    private Integer maxExecutionCount;
    private String status;
    private Integer failureCount;
    private String lastFailureReason;
    private boolean notificationEnabled;
    private LocalDateTime createdAt;
    
    public static ScheduledTransferResponse from(ScheduledTransfer scheduled) {
        return ScheduledTransferResponse.builder()
                .scheduleId(scheduled.getScheduleId())
                .senderAccountNumber(scheduled.getSenderAccountNumber())
                .receiverAccountNumber(scheduled.getReceiverAccountNumber())
                .receiverName(scheduled.getReceiverName())
                .amount(scheduled.getAmount())
                .memo(scheduled.getMemo())
                .scheduleType(scheduled.getScheduleType().getDescription())
                .repeatCycle(scheduled.getRepeatCycle() != null ? 
                        scheduled.getRepeatCycle().getDescription() : null)
                .repeatDayOfMonth(scheduled.getRepeatDayOfMonth())
                .repeatDayOfWeek(scheduled.getRepeatDayOfWeek())
                .executionTime(scheduled.getExecutionTime())
                .startDate(scheduled.getStartDate())
                .endDate(scheduled.getEndDate())
                .nextExecutionTime(scheduled.getNextExecutionTime())
                .lastExecutionTime(scheduled.getLastExecutionTime())
                .executionCount(scheduled.getExecutionCount())
                .maxExecutionCount(scheduled.getMaxExecutionCount())
                .status(scheduled.getStatus().getDescription())
                .failureCount(scheduled.getFailureCount())
                .lastFailureReason(scheduled.getLastFailureReason())
                .notificationEnabled(scheduled.isNotificationEnabled())
                .createdAt(scheduled.getCreatedAt())
                .build();
    }
}