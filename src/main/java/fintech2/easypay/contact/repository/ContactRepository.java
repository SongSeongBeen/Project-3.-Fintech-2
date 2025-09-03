package fintech2.easypay.contact.repository;

import fintech2.easypay.contact.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    
    Optional<Contact> findByOwnerIdAndContactPhoneNumber(Long ownerId, String contactPhoneNumber);
    
    Page<Contact> findByOwnerIdOrderByContactNameAsc(Long ownerId, Pageable pageable);
    
    Page<Contact> findByOwnerIdAndIsFavoriteTrueOrderByContactNameAsc(Long ownerId, Pageable pageable);
    
    @Query("SELECT c FROM Contact c WHERE c.owner.id = :ownerId AND " +
           "(LOWER(c.contactName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "c.contactPhoneNumber LIKE CONCAT('%', :keyword, '%'))")
    Page<Contact> searchContacts(@Param("ownerId") Long ownerId, 
                                 @Param("keyword") String keyword, 
                                 Pageable pageable);
    
    @Query("SELECT c FROM Contact c WHERE c.owner.id = :ownerId AND c.isRegistered = true")
    List<Contact> findRegisteredContactsByOwnerId(@Param("ownerId") Long ownerId);
    
    @Query("SELECT c FROM Contact c WHERE c.contactPhoneNumber = :phoneNumber")
    List<Contact> findByContactPhoneNumber(@Param("phoneNumber") String phoneNumber);
    
    boolean existsByOwnerIdAndContactPhoneNumber(Long ownerId, String contactPhoneNumber);
    
    @Query("UPDATE Contact c SET c.isRegistered = true, c.registeredUser.id = :userId " +
           "WHERE c.contactPhoneNumber = :phoneNumber")
    void updateRegistrationStatus(@Param("phoneNumber") String phoneNumber, 
                                  @Param("userId") Long userId);
    
    Optional<Contact> findByIdAndOwnerId(Long id, Long ownerId);
    
    Page<Contact> findByOwnerIdOrderByIsFavoriteDescCreatedAtDesc(Long ownerId, Pageable pageable);
}