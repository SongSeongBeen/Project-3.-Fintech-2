package fintech2.easypay.audit.repository;

import fintech2.easypay.audit.entity.AuditEventType;
import fintech2.easypay.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    Page<AuditLog> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);
    
    Page<AuditLog> findByPhoneNumberOrderByCreatedAtDesc(String phoneNumber, Pageable pageable);
    
    Page<AuditLog> findByEventTypeOrderByCreatedAtDesc(AuditEventType eventType, Pageable pageable);
    
    @Query("SELECT a FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    Page<AuditLog> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate, 
                                         Pageable pageable);
    
    @Query("SELECT a FROM AuditLog a WHERE a.memberId = :memberId AND a.eventType = :eventType ORDER BY a.createdAt DESC")
    List<AuditLog> findByMemberIdAndEventType(@Param("memberId") Long memberId, 
                                            @Param("eventType") AuditEventType eventType);
}
