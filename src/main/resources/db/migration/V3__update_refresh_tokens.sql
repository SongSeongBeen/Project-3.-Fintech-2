-- Refresh tokens 테이블 업데이트
ALTER TABLE refresh_tokens ADD COLUMN phone_number VARCHAR(20) NOT NULL DEFAULT '';
ALTER TABLE refresh_tokens ADD COLUMN is_revoked BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE refresh_tokens ADD COLUMN revoked_at TIMESTAMP NULL;

-- 기존 데이터에 대한 phone_number 업데이트
UPDATE refresh_tokens rt 
SET phone_number = (SELECT phone_number FROM users u WHERE u.id = rt.user_id)
WHERE rt.phone_number = ''; 