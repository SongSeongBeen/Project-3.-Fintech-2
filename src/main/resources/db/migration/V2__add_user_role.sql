-- User 테이블에 role 컬럼 추가
ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';

-- 기존 사용자들을 USER 역할로 설정
UPDATE users SET role = 'USER' WHERE role IS NULL;

-- role 컬럼에 인덱스 추가 (권한 확인 성능 향상)
CREATE INDEX idx_users_role ON users(role); 