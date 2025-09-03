package fintech2.easypay.contact.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactTransferRequest {
    
    @NotBlank(message = "수신자 전화번호는 필수입니다.")
    @Pattern(regexp = "^01[0-9]{8,9}$", message = "올바른 전화번호 형식이 아닙니다.")
    private String receiverPhoneNumber;
    
    @NotBlank(message = "수신자 이름은 필수입니다.")
    @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하여야 합니다.")
    private String receiverName;
    
    @NotNull(message = "송금 금액은 필수입니다.")
    @DecimalMin(value = "1000", message = "최소 송금 금액은 1,000원입니다.")
    private BigDecimal amount;
    
    @Size(max = 100, message = "메모는 100자 이하여야 합니다.")
    private String memo;
    
    private String senderAccountNumber;
}