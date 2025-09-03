package fintech2.easypay.common.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * 분산 락 서비스
 * 데이터베이스를 기반으로 한 분산 락 구현
 * 데드락 방지를 위한 정렬된 락 획득 지원
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DistributedLockService {
    
    private final DataSource dataSource;
    
    // 로컬 락 맵 (같은 JVM 내에서의 중복 획득 방지)
    private final Map<String, ReentrantLock> localLocks = new ConcurrentHashMap<>();
    
    /**
     * 단일 락으로 작업 실행
     */
    public <T> T executeWithLock(String lockKey, Duration timeout, Supplier<T> operation) {
        return executeWithLock(Collections.singletonList(lockKey), timeout, operation);
    }
    
    /**
     * 여러 락을 순서대로 획득하여 작업 실행 (데드락 방지)
     */
    public <T> T executeWithLock(List<String> lockKeys, Duration timeout, Supplier<T> operation) {
        if (lockKeys == null || lockKeys.isEmpty()) {
            throw new IllegalArgumentException("락 키가 비어있습니다");
        }
        
        // 데드락 방지를 위해 락 키를 정렬
        List<String> sortedKeys = new ArrayList<>(lockKeys);
        Collections.sort(sortedKeys);
        
        log.debug("분산 락 획득 시작: {} (타임아웃: {})", sortedKeys, timeout);
        
        List<String> acquiredLocks = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        
        try {
            // 정렬된 순서로 락 획득
            for (String lockKey : sortedKeys) {
                boolean acquired = acquireLock(lockKey, timeout);
                if (!acquired) {
                    throw new LockAcquisitionException("락 획득 실패: " + lockKey);
                }
                acquiredLocks.add(lockKey);
                
                // 타임아웃 체크
                if (System.currentTimeMillis() - startTime > timeout.toMillis()) {
                    throw new LockAcquisitionException("락 획득 타임아웃: " + sortedKeys);
                }
            }
            
            long acquisitionTime = System.currentTimeMillis() - startTime;
            log.debug("분산 락 획득 완료: {} ({}ms)", sortedKeys, acquisitionTime);
            
            // 실제 비즈니스 로직 실행
            T result = operation.get();
            
            long totalTime = System.currentTimeMillis() - startTime;
            log.debug("분산 락 작업 완료: {} (총 {}ms)", sortedKeys, totalTime);
            
            return result;
            
        } finally {
            // 역순으로 락 해제
            Collections.reverse(acquiredLocks);
            for (String lockKey : acquiredLocks) {
                try {
                    releaseLock(lockKey);
                } catch (Exception e) {
                    log.error("락 해제 실패: {} - {}", lockKey, e.getMessage());
                }
            }
        }
    }
    
    /**
     * 락 획득 (데이터베이스 기반)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean acquireLock(String lockKey, Duration timeout) {
        String normalizedKey = normalizeLockKey(lockKey);
        
        // 로컬 락 먼저 획득 (같은 JVM 내 중복 방지)
        ReentrantLock localLock = localLocks.computeIfAbsent(normalizedKey, k -> new ReentrantLock(true));
        
        try {
            boolean localLockAcquired = localLock.tryLock(timeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
            if (!localLockAcquired) {
                log.warn("로컬 락 획득 실패: {}", normalizedKey);
                return false;
            }
            
            // 데이터베이스 락 획득
            return acquireDbLock(normalizedKey, timeout);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("락 획득 중단됨: {}", normalizedKey);
            return false;
        } catch (Exception e) {
            localLock.unlock();
            throw e;
        }
    }
    
    /**
     * 락 해제
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void releaseLock(String lockKey) {
        String normalizedKey = normalizeLockKey(lockKey);
        
        try {
            // 데이터베이스 락 해제
            releaseDbLock(normalizedKey);
        } finally {
            // 로컬 락 해제
            ReentrantLock localLock = localLocks.get(normalizedKey);
            if (localLock != null && localLock.isHeldByCurrentThread()) {
                localLock.unlock();
            }
        }
    }
    
    /**
     * 데이터베이스 락 획득
     */
    private boolean acquireDbLock(String lockKey, Duration timeout) {
        long endTime = System.currentTimeMillis() + timeout.toMillis();
        
        while (System.currentTimeMillis() < endTime) {
            try (Connection conn = dataSource.getConnection()) {
                conn.setAutoCommit(false);
                
                // 기존 락 정리 (만료된 락들)
                cleanupExpiredLocks(conn, lockKey);
                
                // 락 획득 시도
                boolean acquired = tryAcquireDbLock(conn, lockKey);
                if (acquired) {
                    conn.commit();
                    log.debug("DB 락 획득 성공: {}", lockKey);
                    return true;
                }
                
                conn.rollback();
                
                // 잠시 대기 후 재시도
                Thread.sleep(100);
                
            } catch (SQLException e) {
                log.error("DB 락 획득 중 오류: {} - {}", lockKey, e.getMessage());
                return false;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("DB 락 획득 중단됨: {}", lockKey);
                return false;
            }
        }
        
        log.warn("DB 락 획득 타임아웃: {}", lockKey);
        return false;
    }
    
    /**
     * 데이터베이스 락 획득 시도
     */
    private boolean tryAcquireDbLock(Connection conn, String lockKey) throws SQLException {
        // 1. 기존 락 확인
        String checkSql = """
            SELECT COUNT(*) FROM distributed_locks 
            WHERE lock_key = ? AND expires_at > ?
            """;
        
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, lockKey);
            checkStmt.setObject(2, LocalDateTime.now());
            
            var rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                // 이미 락이 존재함
                return false;
            }
        }
        
        // 2. 락 생성
        String insertSql = """
            INSERT INTO distributed_locks (lock_key, owner_id, created_at, expires_at, metadata)
            VALUES (?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            String ownerId = generateOwnerId();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiresAt = now.plusMinutes(5); // 5분 후 만료
            
            insertStmt.setString(1, lockKey);
            insertStmt.setString(2, ownerId);
            insertStmt.setObject(3, now);
            insertStmt.setObject(4, expiresAt);
            insertStmt.setString(5, createLockMetadata());
            
            int inserted = insertStmt.executeUpdate();
            return inserted > 0;
        }
    }
    
    /**
     * 데이터베이스 락 해제
     */
    private void releaseDbLock(String lockKey) {
        try (Connection conn = dataSource.getConnection()) {
            String deleteSql = """
                DELETE FROM distributed_locks 
                WHERE lock_key = ? AND owner_id = ?
                """;
            
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setString(1, lockKey);
                deleteStmt.setString(2, generateOwnerId());
                
                int deleted = deleteStmt.executeUpdate();
                if (deleted > 0) {
                    log.debug("DB 락 해제 성공: {}", lockKey);
                } else {
                    log.warn("DB 락 해제 실패 (락을 찾을 수 없음): {}", lockKey);
                }
            }
            
        } catch (SQLException e) {
            log.error("DB 락 해제 중 오류: {} - {}", lockKey, e.getMessage());
        }
    }
    
    /**
     * 만료된 락 정리
     */
    private void cleanupExpiredLocks(Connection conn, String lockKey) throws SQLException {
        String deleteSql = """
            DELETE FROM distributed_locks 
            WHERE lock_key = ? AND expires_at <= ?
            """;
        
        try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
            deleteStmt.setString(1, lockKey);
            deleteStmt.setObject(2, LocalDateTime.now());
            
            int deleted = deleteStmt.executeUpdate();
            if (deleted > 0) {
                log.debug("만료된 락 정리: {} ({}개)", lockKey, deleted);
            }
        }
    }
    
    /**
     * 락 키 정규화
     */
    private String normalizeLockKey(String lockKey) {
        if (lockKey == null || lockKey.trim().isEmpty()) {
            throw new IllegalArgumentException("락 키가 비어있습니다");
        }
        return lockKey.trim().toLowerCase();
    }
    
    /**
     * 소유자 ID 생성 (스레드별 고유)
     */
    private String generateOwnerId() {
        return String.format("%d-%d", 
            Thread.currentThread().getId(), 
            System.currentTimeMillis());
    }
    
    /**
     * 락 메타데이터 생성
     */
    private String createLockMetadata() {
        return String.format("{\"thread\":\"%s\",\"timestamp\":\"%s\"}", 
            Thread.currentThread().getName(), 
            LocalDateTime.now());
    }
}