-- V7__replace_restricted_customer_status_with_blacklist.sql

-- 1. Drop constraint cũ
ALTER TABLE customer
DROP CONSTRAINT IF EXISTS chk_customer_status;

-- 2. Chuyển dữ liệu cũ từ RESTRICTED sang BLACKLIST
UPDATE customer
SET status = 'BLACKLIST'
WHERE status = 'RESTRICTED';

-- 3. Tạo lại constraint mới
ALTER TABLE customer
ADD CONSTRAINT chk_customer_status
CHECK (status IN ('ACTIVE', 'INACTIVE', 'BLACKLIST'));