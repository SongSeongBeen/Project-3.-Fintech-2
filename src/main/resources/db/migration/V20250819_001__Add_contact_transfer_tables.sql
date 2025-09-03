-- 연락처 테이블
CREATE TABLE IF NOT EXISTS contacts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    contact_name VARCHAR(50) NOT NULL,
    contact_phone_number VARCHAR(20) NOT NULL,
    registered_user_id BIGINT,
    is_registered BOOLEAN DEFAULT FALSE,
    is_verified BOOLEAN DEFAULT FALSE,
    is_favorite BOOLEAN DEFAULT FALSE,
    memo VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id),
    FOREIGN KEY (registered_user_id) REFERENCES users(id),
    UNIQUE (owner_id, contact_phone_number)
);

-- 연락처 관련 인덱스
CREATE INDEX IF NOT EXISTS idx_owner_phone ON contacts (owner_id, contact_phone_number);
CREATE INDEX IF NOT EXISTS idx_contact_phone ON contacts (contact_phone_number);

-- 대기 중인 연락처 송금 테이블
CREATE TABLE IF NOT EXISTS pending_contact_transfers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(50) NOT NULL UNIQUE,
    sender_id BIGINT NOT NULL,
    sender_account_number VARCHAR(50) NOT NULL,
    receiver_phone_number VARCHAR(20) NOT NULL,
    receiver_name VARCHAR(50) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    memo VARCHAR(100),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    invitation_code VARCHAR(10) UNIQUE,
    invitation_sent_at TIMESTAMP,
    invitation_sent_count INT DEFAULT 0,
    expires_at TIMESTAMP NOT NULL,
    completed_transfer_id BIGINT,
    cancellation_reason VARCHAR(255),
    cancelled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (completed_transfer_id) REFERENCES transfers(id)
);

-- 대기 중인 송금 관련 인덱스
CREATE INDEX IF NOT EXISTS idx_receiver_phone ON pending_contact_transfers (receiver_phone_number);
CREATE INDEX IF NOT EXISTS idx_status ON pending_contact_transfers (status);
CREATE INDEX IF NOT EXISTS idx_expires_at ON pending_contact_transfers (expires_at);

-- AccountBalance 테이블에 hold_amount 컬럼 추가
ALTER TABLE account_balances ADD COLUMN IF NOT EXISTS hold_amount DECIMAL(15,2) DEFAULT 0;