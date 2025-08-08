package fintech2.easypay.audit.repository;

import fintech2.easypay.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByMemberIdOrderByCreatedAtDesc(Long memberId);
    List<AuditLog> findByEventDescriptionOrderByCreatedAtDesc(String eventDescription);
} 