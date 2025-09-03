package fintech2.easypay.transfer.repository;

import fintech2.easypay.transfer.entity.ScheduledTransfer;
import fintech2.easypay.transfer.entity.ScheduledTransferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduledTransferRepository extends JpaRepository<ScheduledTransfer, Long> {
    
    Optional<ScheduledTransfer> findByScheduleId(String scheduleId);
    
    @Query("SELECT s FROM ScheduledTransfer s WHERE s.sender.id = :senderId ORDER BY s.createdAt DESC")
    Page<ScheduledTransfer> findBySenderId(@Param("senderId") Long senderId, Pageable pageable);
    
    @Query("SELECT s FROM ScheduledTransfer s WHERE s.sender.id = :senderId AND s.status = :status")
    Page<ScheduledTransfer> findBySenderIdAndStatus(@Param("senderId") Long senderId, 
                                                     @Param("status") ScheduledTransferStatus status,
                                                     Pageable pageable);
    
    @Query("SELECT s FROM ScheduledTransfer s WHERE s.status = 'ACTIVE' " +
           "AND s.nextExecutionTime <= :now")
    List<ScheduledTransfer> findScheduledTransfersToExecute(@Param("now") LocalDateTime now);
    
    @Query("SELECT s FROM ScheduledTransfer s WHERE s.status = 'ACTIVE' " +
           "AND s.notificationEnabled = true " +
           "AND s.nextExecutionTime BETWEEN :startTime AND :endTime")
    List<ScheduledTransfer> findTransfersForNotification(@Param("startTime") LocalDateTime startTime,
                                                         @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT COUNT(s) FROM ScheduledTransfer s WHERE s.sender.id = :senderId " +
           "AND s.status IN ('ACTIVE', 'PAUSED')")
    long countActiveBySenderId(@Param("senderId") Long senderId);
    
    boolean existsByScheduleId(String scheduleId);
}