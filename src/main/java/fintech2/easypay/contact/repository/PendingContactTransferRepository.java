package fintech2.easypay.contact.repository;

import fintech2.easypay.contact.entity.PendingContactTransfer;
import fintech2.easypay.contact.entity.PendingTransferStatus;
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
public interface PendingContactTransferRepository extends JpaRepository<PendingContactTransfer, Long> {
    
    Optional<PendingContactTransfer> findByTransactionId(String transactionId);
    
    Optional<PendingContactTransfer> findByInvitationCode(String invitationCode);
    
    @Query("SELECT p FROM PendingContactTransfer p WHERE p.receiverPhoneNumber = :phoneNumber " +
           "AND p.status IN ('PENDING', 'INVITATION_SENT')")
    List<PendingContactTransfer> findPendingByReceiverPhoneNumber(@Param("phoneNumber") String phoneNumber);
    
    @Query("SELECT p FROM PendingContactTransfer p WHERE p.sender.id = :senderId " +
           "ORDER BY p.createdAt DESC")
    Page<PendingContactTransfer> findBySenderId(@Param("senderId") Long senderId, Pageable pageable);
    
    @Query("SELECT p FROM PendingContactTransfer p WHERE p.expiresAt < :now " +
           "AND p.status IN ('PENDING', 'INVITATION_SENT')")
    List<PendingContactTransfer> findExpiredTransfers(@Param("now") LocalDateTime now);
    
    @Query("SELECT p FROM PendingContactTransfer p WHERE p.sender.id = :senderId " +
           "AND p.receiverPhoneNumber = :receiverPhone " +
           "AND p.status IN ('PENDING', 'INVITATION_SENT')")
    List<PendingContactTransfer> findActivePendingTransfers(@Param("senderId") Long senderId,
                                                            @Param("receiverPhone") String receiverPhone);
    
    boolean existsByTransactionId(String transactionId);
    
    @Query("SELECT COUNT(p) FROM PendingContactTransfer p WHERE p.sender.id = :senderId " +
           "AND p.status IN ('PENDING', 'INVITATION_SENT')")
    long countActivePendingTransfersBySender(@Param("senderId") Long senderId);
}