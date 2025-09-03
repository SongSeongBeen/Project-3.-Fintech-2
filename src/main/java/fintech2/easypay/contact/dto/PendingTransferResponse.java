package fintech2.easypay.contact.dto;

import fintech2.easypay.contact.entity.PendingContactTransfer;
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
public class PendingTransferResponse {
    private String transactionId;
    private String senderName;
    private String senderPhoneNumber;
    private String receiverName;
    private String receiverPhoneNumber;
    private BigDecimal amount;
    private String memo;
    private String status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private String cancellationReason;
    
    public static PendingTransferResponse from(PendingContactTransfer pending) {
        return PendingTransferResponse.builder()
                .transactionId(pending.getTransactionId())
                .senderName(pending.getSender().getName())
                .senderPhoneNumber(pending.getSender().getPhoneNumber())
                .receiverName(pending.getReceiverName())
                .receiverPhoneNumber(pending.getReceiverPhoneNumber())
                .amount(pending.getAmount())
                .memo(pending.getMemo())
                .status(pending.getStatus().getDescription())
                .expiresAt(pending.getExpiresAt())
                .createdAt(pending.getCreatedAt())
                .cancellationReason(pending.getCancellationReason())
                .build();
    }
}