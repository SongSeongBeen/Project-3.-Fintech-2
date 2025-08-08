package fintech2.easypay.transfer.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecureTransferRequest {
    
    @NotBlank(message = "받는 사람 계좌번호는 필수입니다")
    private String receiverAccountNumber;
    
    // 송금할 계좌번호 추가 (선택한 내 계좌)
    private String senderAccountNumber;
    
    @NotNull(message = "송금 금액은 필수입니다")
    @DecimalMin(value = "1000", message = "최소 송금 금액은 1,000원입니다")
    private BigDecimal amount;
    
    private String memo;
    
    @NotBlank(message = "PIN 인증 세션 토큰은 필수입니다")
    private String pinSessionToken;
    
    // TransferRequest로 변환하는 메서드
    public TransferRequest toTransferRequest() {
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setReceiverAccountNumber(this.receiverAccountNumber);
        transferRequest.setSenderAccountNumber(this.senderAccountNumber); // 송금 계좌 추가
        transferRequest.setAmount(this.amount);
        transferRequest.setMemo(this.memo);
        return transferRequest;
    }
}