-- 예약 송금 테이블
CREATE TABLE IF NOT EXISTS scheduled_transfers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_id VARCHAR(50) NOT NULL UNIQUE,
    sender_id BIGINT NOT NULL,
    sender_account_number VARCHAR(50) NOT NULL,
    receiver_account_number VARCHAR(50) NOT NULL,
    receiver_name VARCHAR(50),
    receiver_phone_number VARCHAR(20),
    amount DECIMAL(19,2) NOT NULL,
    memo VARCHAR(100),
    schedule_type VARCHAR(20) NOT NULL,
    repeat_cycle VARCHAR(20),
    repeat_day_of_month INT,
    repeat_day_of_week INT,
    execution_time TIME,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP,
    next_execution_time TIMESTAMP,
    last_execution_time TIMESTAMP,
    execution_count INT DEFAULT 0,
    max_execution_count INT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    failure_count INT DEFAULT 0,
    last_failure_reason VARCHAR(255),
    notification_enabled BOOLEAN DEFAULT TRUE,
    notification_minutes_before INT DEFAULT 30,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id)
);

-- 예약 송금 관련 인덱스
CREATE INDEX IF NOT EXISTS idx_sender_status ON scheduled_transfers (sender_id, status);
CREATE INDEX IF NOT EXISTS idx_next_execution ON scheduled_transfers (next_execution_time);
CREATE INDEX IF NOT EXISTS idx_status ON scheduled_transfers (status);

-- 예약 송금 실행 기록 테이블
CREATE TABLE IF NOT EXISTS scheduled_transfer_executions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scheduled_transfer_id BIGINT NOT NULL,
    execution_time TIMESTAMP NOT NULL,
    transaction_id VARCHAR(50),
    transfer_id BIGINT,
    status VARCHAR(20) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    error_message VARCHAR(500),
    retry_count INT DEFAULT 0,
    next_retry_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (scheduled_transfer_id) REFERENCES scheduled_transfers(id),
    FOREIGN KEY (transfer_id) REFERENCES transfers(id)
);

-- 예약 송금 실행 관련 인덱스
CREATE INDEX IF NOT EXISTS idx_schedule_id ON scheduled_transfer_executions (scheduled_transfer_id);
CREATE INDEX IF NOT EXISTS idx_execution_time ON scheduled_transfer_executions (execution_time);