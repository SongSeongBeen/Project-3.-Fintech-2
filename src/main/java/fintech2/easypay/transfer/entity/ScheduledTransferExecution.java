package fintech2.easypay.transfer.entity;

import fintech2.easypay.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "scheduled_transfer_executions",
    indexes = {
        @Index(name = "idx_schedule_id", columnList = "scheduled_transfer_id"),
        @Index(name = "idx_execution_time", columnList = "execution_time")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledTransferExecution extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheduled_transfer_id", nullable = false)
    private ScheduledTransfer scheduledTransfer;
    
    @Column(name = "execution_time", nullable = false)
    private LocalDateTime executionTime;
    
    @Column(name = "transaction_id")
    private String transactionId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id")
    private Transfer transfer;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ExecutionStatus status;
    
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "error_message")
    private String errorMessage;
    
    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;
    
    @Column(name = "next_retry_time")
    private LocalDateTime nextRetryTime;
    
    public void markAsSuccess(Transfer transfer) {
        this.status = ExecutionStatus.SUCCESS;
        this.transfer = transfer;
        this.transactionId = transfer.getTransactionId();
    }
    
    public void markAsFailed(String errorMessage) {
        this.status = ExecutionStatus.FAILED;
        this.errorMessage = errorMessage;
    }
    
    public void incrementRetry() {
        this.retryCount++;
        this.nextRetryTime = LocalDateTime.now().plusMinutes(10 * retryCount);
    }
}