package fintech2.easypay.auth.repository;

import fintech2.easypay.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByEmail(String email);
    Optional<User> findByName(String name);
    
    // username을 phoneNumber로 매핑 (payment/transfer 모듈 호환성을 위해)
    @Query("SELECT u FROM User u WHERE u.phoneNumber = :username")
    Optional<User> findByUsername(@Param("username") String username);
    
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);
} 