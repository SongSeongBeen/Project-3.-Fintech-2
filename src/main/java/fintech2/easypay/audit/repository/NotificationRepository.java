package fintech2.easypay.audit.repository;

import fintech2.easypay.audit.entity.Notification;
import fintech2.easypay.audit.entity.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    Page<Notification> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);
    
    Page<Notification> findByPhoneNumberOrderByCreatedAtDesc(String phoneNumber, Pageable pageable);
    
    List<Notification> findByStatusOrderByCreatedAtAsc(NotificationStatus status);
    
    List<Notification> findByMemberIdAndStatusOrderByCreatedAtDesc(Long memberId, NotificationStatus status);
}
