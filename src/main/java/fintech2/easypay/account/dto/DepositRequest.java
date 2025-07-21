package fintech2.easypay.account.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositRequest {
    
    @NotNull(message = "입금 금액은 필수입니다.")
    @DecimalMin(value = "0.01", message = "입금 금액은 0보다 커야 합니다.")
    private BigDecimal amount;
    
    private String memo;
}
