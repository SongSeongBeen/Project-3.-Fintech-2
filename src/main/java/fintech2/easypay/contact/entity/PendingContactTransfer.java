package fintech2.easypay.contact.entity;

import fintech2.easypay.auth.entity.User;
import fintech2.easypay.common.BaseEntity;
import fintech2.easypay.transfer.entity.Transfer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pending_contact_transfers",
    indexes = {
        @Index(name = "idx_receiver_phone", columnList = "receiver_phone_number"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_expires_at", columnList = "expires_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingContactTransfer extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;
    
    @Column(name = "sender_account_number", nullable = false)
    private String senderAccountNumber;
    
    @Column(name = "receiver_phone_number", nullable = false)
    private String receiverPhoneNumber;
    
    @Column(name = "receiver_name", nullable = false)
    private String receiverName;
    
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "memo")
    private String memo;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private PendingTransferStatus status = PendingTransferStatus.PENDING;
    
    @Column(name = "invitation_code", unique = true)
    private String invitationCode;
    
    @Column(name = "invitation_sent_at")
    private LocalDateTime invitationSentAt;
    
    @Column(name = "invitation_sent_count")
    @Builder.Default
    private Integer invitationSentCount = 0;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_transfer_id")
    private Transfer completedTransfer;
    
    @Column(name = "cancellation_reason")
    private String cancellationReason;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    public void markAsInvitationSent(String invitationCode) {
        this.invitationCode = invitationCode;
        this.invitationSentAt = LocalDateTime.now();
        this.invitationSentCount++;
        this.status = PendingTransferStatus.INVITATION_SENT;
    }
    
    public void markAsRegistered(User receiver) {
        this.status = PendingTransferStatus.RECEIVER_REGISTERED;
    }
    
    public void markAsCompleted(Transfer transfer) {
        this.status = PendingTransferStatus.COMPLETED;
        this.completedTransfer = transfer;
    }
    
    public void markAsCancelled(String reason) {
        this.status = PendingTransferStatus.CANCELLED;
        this.cancellationReason = reason;
        this.cancelledAt = LocalDateTime.now();
    }
    
    public void markAsExpired() {
        this.status = PendingTransferStatus.EXPIRED;
        this.cancellationReason = "24시간 내 수령하지 않아 자동 취소됨";
        this.cancelledAt = LocalDateTime.now();
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
    
    public boolean canResendInvitation() {
        return this.invitationSentCount < 3 && 
               this.status == PendingTransferStatus.INVITATION_SENT;
    }
}