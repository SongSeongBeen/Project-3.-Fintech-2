package fintech2.easypay.transfer.dto;

import fintech2.easypay.transfer.entity.RepeatCycle;
import fintech2.easypay.transfer.entity.ScheduleType;
import jakarta.validation.constraints.*;
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
public class ScheduledTransferRequest {
    
    @NotBlank(message = "수신자 계좌번호는 필수입니다.")
    private String receiverAccountNumber;
    
    private String receiverName;
    
    @NotNull(message = "송금 금액은 필수입니다.")
    @DecimalMin(value = "1000", message = "최소 송금 금액은 1,000원입니다.")
    private BigDecimal amount;
    
    @Size(max = 100, message = "메모는 100자 이하여야 합니다.")
    private String memo;
    
    private String senderAccountNumber;
    
    @NotNull(message = "예약 유형은 필수입니다.")
    private ScheduleType scheduleType;
    
    private RepeatCycle repeatCycle;
    
    @Min(value = 1, message = "일자는 1 이상이어야 합니다.")
    @Max(value = 31, message = "일자는 31 이하여야 합니다.")
    private Integer repeatDayOfMonth;
    
    @Min(value = 1, message = "요일은 1(월요일) 이상이어야 합니다.")
    @Max(value = 7, message = "요일은 7(일요일) 이하여야 합니다.")
    private Integer repeatDayOfWeek;
    
    private LocalTime executionTime;
    
    @NotNull(message = "시작 일시는 필수입니다.")
    @Future(message = "시작 일시는 현재보다 미래여야 합니다.")
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    private Integer maxExecutionCount;
    
    @Builder.Default
    private boolean notificationEnabled = true;
    
    @Builder.Default
    private Integer notificationMinutesBefore = 30;
}