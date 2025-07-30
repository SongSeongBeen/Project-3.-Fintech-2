package fintech2.easypay.account.repository;

import fintech2.easypay.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

/**
 * 계좌 Repository
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    /**
     * 사용자 ID로 계좌 조회 (단일 계좌 가정)
     */
    Optional<Account> findByUserId(Long userId); // memberId -> userId 통일
    
    /**
     * 사용자 ID로 계좌 조회 (복수 계좌 지원)
     */
    List<Account> findAllByUserId(Long userId);
    
    /**
     * 계좌번호로 계좌 조회
     */
    Optional<Account> findByAccountNumber(String accountNumber);
    
    /**
     * 사용자 ID로 계좌 조회 (비관적 락 적용)
     * 동시성 제어가 필요한 송금/결제 시 사용
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.userId = :userId")
    Optional<Account> findByUserIdWithLock(@Param("userId") Long userId); // findByMemberIdWithLock -> findByUserIdWithLock
    
    /**
     * 계좌번호로 계좌 조회 (비관적 락 적용)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber")
    Optional<Account> findByAccountNumberWithLock(@Param("accountNumber") String accountNumber);
    
    /**
     * 활성 상태인 계좌만 조회
     */
    @Query("SELECT a FROM Account a WHERE a.userId = :userId AND a.status = 'ACTIVE'")
    List<Account> findActiveAccountsByUserId(@Param("userId") Long userId);
    
    /**
     * 계좌번호 존재 여부 확인
     */
    boolean existsByAccountNumber(String accountNumber);
    
    /**
     * ID로 계좌 조회 (비관적 락 적용)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdWithLock(@Param("id") Long id);
}