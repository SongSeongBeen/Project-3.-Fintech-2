package fintech2.easypay.contact.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactTransferResponse {
    private String transactionId;
    private String status;
    private String message;
    private BigDecimal amount;
    private String receiverName;
    private String receiverPhoneNumber;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}