package fintech2.easypay.transfer.repository;

import fintech2.easypay.transfer.entity.ExecutionStatus;
import fintech2.easypay.transfer.entity.ScheduledTransferExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduledTransferExecutionRepository extends JpaRepository<ScheduledTransferExecution, Long> {
    
    @Query("SELECT e FROM ScheduledTransferExecution e WHERE e.scheduledTransfer.id = :scheduledTransferId " +
           "ORDER BY e.executionTime DESC")
    Page<ScheduledTransferExecution> findByScheduledTransferId(@Param("scheduledTransferId") Long scheduledTransferId,
                                                               Pageable pageable);
    
    @Query("SELECT e FROM ScheduledTransferExecution e WHERE e.status = 'RETRY' " +
           "AND e.nextRetryTime <= :now")
    List<ScheduledTransferExecution> findRetryExecutions(@Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(e) FROM ScheduledTransferExecution e WHERE e.scheduledTransfer.id = :scheduledTransferId " +
           "AND e.status = 'SUCCESS'")
    long countSuccessfulExecutions(@Param("scheduledTransferId") Long scheduledTransferId);
    
    @Query("SELECT e FROM ScheduledTransferExecution e WHERE e.scheduledTransfer.sender.id = :senderId " +
           "ORDER BY e.executionTime DESC")
    Page<ScheduledTransferExecution> findBySenderId(@Param("senderId") Long senderId, Pageable pageable);
}