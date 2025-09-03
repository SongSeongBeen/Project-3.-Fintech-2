-- 분산 락 테이블 생성
CREATE TABLE IF NOT EXISTS distributed_locks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lock_key VARCHAR(255) NOT NULL UNIQUE,
    owner_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    metadata TEXT
);

-- 만료된 락 자동 정리를 위한 인덱스
CREATE INDEX IF NOT EXISTS idx_distributed_locks_expires ON distributed_locks (expires_at);
CREATE INDEX IF NOT EXISTS idx_distributed_locks_key_expires ON distributed_locks (lock_key, expires_at);