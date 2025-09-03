package fintech2.easypay.transfer.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
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
public class ScheduledTransferUpdateRequest {
    
    @DecimalMin(value = "1000", message = "최소 송금 금액은 1,000원입니다.")
    private BigDecimal amount;
    
    @Size(max = 100, message = "메모는 100자 이하여야 합니다.")
    private String memo;
    
    private LocalTime executionTime;
    
    private LocalDateTime endDate;
    
    private Boolean notificationEnabled;
}