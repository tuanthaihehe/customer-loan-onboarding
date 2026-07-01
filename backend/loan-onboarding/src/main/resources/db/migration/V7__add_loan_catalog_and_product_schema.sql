-- Customer Loan Onboarding - Loan Catalog and Product Schema
-- PostgreSQL dialect
--
-- This backend Flyway migration folds the new database design from:
-- - database/migrations/V8__add_loan_purpose_catalog.sql
-- - database/migrations/V9__add_loan_term_catalog.sql
-- - database/migrations/V10__add_loan_product_catalog.sql
-- - database/seed/V5__seed_loan_purpose.sql
-- - database/seed/V6__seed_loan_term.sql
-- - database/seed/V7__seed_loan_product.sql

-- =========================================================
-- 1. Loan purpose catalog
-- =========================================================

CREATE TABLE IF NOT EXISTS loan_purpose (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_loan_purpose_code UNIQUE (code)
);

INSERT INTO loan_purpose (
    code,
    name,
    description,
    is_active,
    sort_order
)
VALUES
    ('BUSINESS', 'Kinh doanh', 'Vay phục vụ hoạt động kinh doanh.', TRUE, 10),
    ('PERSONAL_CONSUMPTION', 'Tiêu dùng cá nhân', 'Vay phục vụ nhu cầu tiêu dùng cá nhân.', TRUE, 20),
    ('VEHICLE_REPAIR', 'Sửa chữa xe', 'Vay phục vụ sửa chữa, bảo dưỡng phương tiện.', TRUE, 30),
    ('MEDICAL', 'Y tế', 'Vay phục vụ chi phí khám chữa bệnh hoặc nhu cầu y tế.', TRUE, 40),
    ('EDUCATION', 'Giáo dục', 'Vay phục vụ chi phí học tập hoặc giáo dục.', TRUE, 50),
    ('HOME_REPAIR', 'Sửa chữa nhà', 'Vay phục vụ sửa chữa hoặc cải tạo nhà cửa.', TRUE, 60),
    ('DEBT_REPAYMENT', 'Thanh toán nợ', 'Vay để thanh toán khoản nợ khác.', TRUE, 70),
    ('OTHER', 'Khác', 'Mục đích vay khác.', TRUE, 999)
ON CONFLICT (code) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    is_active = EXCLUDED.is_active,
    sort_order = EXCLUDED.sort_order;

ALTER TABLE loan_application
ADD COLUMN IF NOT EXISTS loan_purpose_id UUID REFERENCES loan_purpose(id);

UPDATE loan_application la
SET loan_purpose_id = lp.id
FROM loan_purpose lp
WHERE la.loan_purpose_id IS NULL
  AND la.loan_purpose IS NOT NULL
  AND lp.code = la.loan_purpose;

ALTER TABLE loan_application
DROP COLUMN IF EXISTS loan_purpose;

CREATE INDEX IF NOT EXISTS idx_loan_application_loan_purpose_id
ON loan_application(loan_purpose_id);

-- =========================================================
-- 2. Loan term catalog
-- =========================================================

CREATE TABLE IF NOT EXISTS loan_term (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL,
    term_months INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_loan_term_code UNIQUE (code),
    CONSTRAINT uq_loan_term_months UNIQUE (term_months),
    CONSTRAINT chk_loan_term_months CHECK (term_months > 0)
);

INSERT INTO loan_term (
    code,
    term_months,
    name,
    description,
    is_active,
    sort_order
)
VALUES
    ('TERM_3M', 3, '3 tháng', 'Kỳ hạn vay 3 tháng.', TRUE, 10),
    ('TERM_6M', 6, '6 tháng', 'Kỳ hạn vay 6 tháng.', TRUE, 20),
    ('TERM_9M', 9, '9 tháng', 'Kỳ hạn vay 9 tháng.', TRUE, 30),
    ('TERM_12M', 12, '12 tháng', 'Kỳ hạn vay 12 tháng.', TRUE, 40),
    ('TERM_18M', 18, '18 tháng', 'Kỳ hạn vay 18 tháng.', TRUE, 50),
    ('TERM_24M', 24, '24 tháng', 'Kỳ hạn vay 24 tháng.', TRUE, 60)
ON CONFLICT (code) DO UPDATE
SET
    term_months = EXCLUDED.term_months,
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    is_active = EXCLUDED.is_active,
    sort_order = EXCLUDED.sort_order;

ALTER TABLE loan_application
ADD COLUMN IF NOT EXISTS loan_term_id UUID REFERENCES loan_term(id);

UPDATE loan_application la
SET loan_term_id = lt.id
FROM loan_term lt
WHERE la.loan_term_id IS NULL
  AND la.loan_term_months IS NOT NULL
  AND lt.term_months = la.loan_term_months;

CREATE INDEX IF NOT EXISTS idx_loan_application_loan_term_id
ON loan_application(loan_term_id);

ALTER TABLE loan_application
DROP CONSTRAINT IF EXISTS chk_loan_application_loan_term_months;

ALTER TABLE loan_application
ADD CONSTRAINT chk_loan_application_loan_term_months
CHECK (
    loan_term_months IS NULL
    OR loan_term_months IN (3, 6, 9, 12, 18, 24)
);

-- =========================================================
-- 3. Loan product catalog
-- =========================================================

CREATE TABLE IF NOT EXISTS score_grade (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(10) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_score_grade_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS loan_product (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_code VARCHAR(50) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    applies_to_all_loan_purposes BOOLEAN NOT NULL DEFAULT FALSE,
    min_loan_amount NUMERIC(18, 2) NOT NULL,
    max_loan_amount NUMERIC(18, 2) NOT NULL,
    max_ltv_percent NUMERIC(5, 2) NOT NULL,
    monthly_interest_rate_percent NUMERIC(5, 2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_loan_product_code UNIQUE (product_code),
    CONSTRAINT chk_loan_product_amount CHECK (min_loan_amount > 0 AND max_loan_amount >= min_loan_amount),
    CONSTRAINT chk_loan_product_ltv CHECK (max_ltv_percent > 0 AND max_ltv_percent <= 100),
    CONSTRAINT chk_loan_product_interest_rate CHECK (monthly_interest_rate_percent > 0)
);

CREATE TABLE IF NOT EXISTS loan_product_purpose (
    loan_product_id UUID NOT NULL REFERENCES loan_product(id) ON DELETE CASCADE,
    loan_purpose_id UUID NOT NULL REFERENCES loan_purpose(id),
    PRIMARY KEY (loan_product_id, loan_purpose_id)
);

CREATE TABLE IF NOT EXISTS loan_product_vehicle_type (
    loan_product_id UUID NOT NULL REFERENCES loan_product(id) ON DELETE CASCADE,
    vehicle_type_id UUID NOT NULL REFERENCES vehicle_type(id),
    PRIMARY KEY (loan_product_id, vehicle_type_id)
);

CREATE TABLE IF NOT EXISTS loan_product_term (
    loan_product_id UUID NOT NULL REFERENCES loan_product(id) ON DELETE CASCADE,
    loan_term_id UUID NOT NULL REFERENCES loan_term(id),
    PRIMARY KEY (loan_product_id, loan_term_id)
);

CREATE TABLE IF NOT EXISTS loan_product_score_grade (
    loan_product_id UUID NOT NULL REFERENCES loan_product(id) ON DELETE CASCADE,
    score_grade_id UUID NOT NULL REFERENCES score_grade(id),
    PRIMARY KEY (loan_product_id, score_grade_id)
);

CREATE INDEX IF NOT EXISTS idx_loan_product_purpose_purpose_id
ON loan_product_purpose(loan_purpose_id);

CREATE INDEX IF NOT EXISTS idx_loan_product_vehicle_type_vehicle_type_id
ON loan_product_vehicle_type(vehicle_type_id);

CREATE INDEX IF NOT EXISTS idx_loan_product_term_loan_term_id
ON loan_product_term(loan_term_id);

CREATE INDEX IF NOT EXISTS idx_loan_product_score_grade_score_grade_id
ON loan_product_score_grade(score_grade_id);

INSERT INTO score_grade (
    code,
    name,
    description,
    is_active,
    sort_order
)
VALUES
    ('A', 'Hạng A', 'Hạng điểm tốt nhất.', TRUE, 10),
    ('B', 'Hạng B', 'Hạng điểm tốt.', TRUE, 20),
    ('C', 'Hạng C', 'Hạng điểm trung bình.', TRUE, 30),
    ('D', 'Hạng D', 'Hạng điểm rủi ro cao.', TRUE, 40)
ON CONFLICT (code) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    is_active = EXCLUDED.is_active,
    sort_order = EXCLUDED.sort_order;

INSERT INTO loan_product (
    product_code,
    product_name,
    applies_to_all_loan_purposes,
    min_loan_amount,
    max_loan_amount,
    max_ltv_percent,
    monthly_interest_rate_percent,
    is_active,
    sort_order
)
VALUES
    ('XM_STANDARD', 'Xe máy tiêu chuẩn', TRUE, 3000000, 15000000, 60.00, 3.20, TRUE, 10),
    ('XM_FLEX', 'Xe máy linh hoạt', FALSE, 5000000, 20000000, 70.00, 2.90, TRUE, 20),
    ('XM_PREFER', 'Xe máy ưu đãi', FALSE, 7000000, 30000000, 75.00, 2.80, TRUE, 30),
    ('XM_SHORT', 'Xe máy ngắn hạn', FALSE, 3000000, 9000000, 50.00, 2.70, TRUE, 40),
    ('XM_BUSINESS', 'Xe máy kinh doanh', FALSE, 8000000, 25000000, 80.00, 3.40, TRUE, 50),
    ('XM_HIGH_LIMIT', 'Xe máy hạn mức cao', FALSE, 15000000, 40000000, 85.00, 4.20, TRUE, 60),
    ('CAR_STANDARD', 'Ô tô tiêu chuẩn', FALSE, 20000000, 100000000, 70.00, 2.50, TRUE, 70)
ON CONFLICT (product_code) DO UPDATE
SET
    product_name = EXCLUDED.product_name,
    applies_to_all_loan_purposes = EXCLUDED.applies_to_all_loan_purposes,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount,
    max_ltv_percent = EXCLUDED.max_ltv_percent,
    monthly_interest_rate_percent = EXCLUDED.monthly_interest_rate_percent,
    is_active = EXCLUDED.is_active,
    sort_order = EXCLUDED.sort_order;

DELETE FROM loan_product_purpose lpp
USING loan_product lp
WHERE lpp.loan_product_id = lp.id
  AND lp.product_code IN (
      'XM_STANDARD', 'XM_FLEX', 'XM_PREFER', 'XM_SHORT',
      'XM_BUSINESS', 'XM_HIGH_LIMIT', 'CAR_STANDARD'
  );

DELETE FROM loan_product_vehicle_type lpvt
USING loan_product lp
WHERE lpvt.loan_product_id = lp.id
  AND lp.product_code IN (
      'XM_STANDARD', 'XM_FLEX', 'XM_PREFER', 'XM_SHORT',
      'XM_BUSINESS', 'XM_HIGH_LIMIT', 'CAR_STANDARD'
  );

DELETE FROM loan_product_term lpt
USING loan_product lp
WHERE lpt.loan_product_id = lp.id
  AND lp.product_code IN (
      'XM_STANDARD', 'XM_FLEX', 'XM_PREFER', 'XM_SHORT',
      'XM_BUSINESS', 'XM_HIGH_LIMIT', 'CAR_STANDARD'
  );

DELETE FROM loan_product_score_grade lpsg
USING loan_product lp
WHERE lpsg.loan_product_id = lp.id
  AND lp.product_code IN (
      'XM_STANDARD', 'XM_FLEX', 'XM_PREFER', 'XM_SHORT',
      'XM_BUSINESS', 'XM_HIGH_LIMIT', 'CAR_STANDARD'
  );

INSERT INTO loan_product_purpose (
    loan_product_id,
    loan_purpose_id
)
SELECT lp.id, lpur.id
FROM (
    VALUES
        ('XM_FLEX', 'PERSONAL_CONSUMPTION'),
        ('XM_PREFER', 'PERSONAL_CONSUMPTION'),
        ('XM_SHORT', 'PERSONAL_CONSUMPTION'),
        ('XM_BUSINESS', 'BUSINESS'),
        ('XM_HIGH_LIMIT', 'PERSONAL_CONSUMPTION'),
        ('CAR_STANDARD', 'PERSONAL_CONSUMPTION')
) AS m(product_code, loan_purpose_code)
JOIN loan_product lp
    ON lp.product_code = m.product_code
JOIN loan_purpose lpur
    ON lpur.code = m.loan_purpose_code
ON CONFLICT DO NOTHING;

INSERT INTO loan_product_vehicle_type (
    loan_product_id,
    vehicle_type_id
)
SELECT lp.id, vt.id
FROM (
    VALUES
        ('XM_STANDARD', 'MOTORBIKE'),
        ('XM_FLEX', 'MOTORBIKE'),
        ('XM_PREFER', 'MOTORBIKE'),
        ('XM_SHORT', 'MOTORBIKE'),
        ('XM_BUSINESS', 'MOTORBIKE'),
        ('XM_HIGH_LIMIT', 'MOTORBIKE'),
        ('CAR_STANDARD', 'CAR')
) AS m(product_code, vehicle_type_code)
JOIN loan_product lp
    ON lp.product_code = m.product_code
JOIN vehicle_type vt
    ON vt.code = m.vehicle_type_code
ON CONFLICT DO NOTHING;

INSERT INTO loan_product_term (
    loan_product_id,
    loan_term_id
)
SELECT lp.id, lt.id
FROM (
    VALUES
        ('XM_STANDARD', 6),
        ('XM_STANDARD', 12),
        ('XM_STANDARD', 18),
        ('XM_STANDARD', 24),
        ('XM_FLEX', 12),
        ('XM_FLEX', 24),
        ('XM_PREFER', 6),
        ('XM_PREFER', 12),
        ('XM_SHORT', 6),
        ('XM_BUSINESS', 6),
        ('XM_BUSINESS', 12),
        ('XM_BUSINESS', 18),
        ('XM_HIGH_LIMIT', 12),
        ('XM_HIGH_LIMIT', 18),
        ('XM_HIGH_LIMIT', 24),
        ('CAR_STANDARD', 12),
        ('CAR_STANDARD', 24)
) AS m(product_code, term_months)
JOIN loan_product lp
    ON lp.product_code = m.product_code
JOIN loan_term lt
    ON lt.term_months = m.term_months
ON CONFLICT DO NOTHING;

INSERT INTO loan_product_score_grade (
    loan_product_id,
    score_grade_id
)
SELECT lp.id, sg.id
FROM (
    VALUES
        ('XM_STANDARD', 'A'),
        ('XM_STANDARD', 'B'),
        ('XM_STANDARD', 'C'),
        ('XM_FLEX', 'A'),
        ('XM_FLEX', 'B'),
        ('XM_PREFER', 'A'),
        ('XM_SHORT', 'A'),
        ('XM_SHORT', 'B'),
        ('XM_SHORT', 'C'),
        ('XM_BUSINESS', 'A'),
        ('XM_BUSINESS', 'B'),
        ('XM_HIGH_LIMIT', 'A'),
        ('XM_HIGH_LIMIT', 'B'),
        ('CAR_STANDARD', 'A'),
        ('CAR_STANDARD', 'B'),
        ('CAR_STANDARD', 'C')
) AS m(product_code, score_grade_code)
JOIN loan_product lp
    ON lp.product_code = m.product_code
JOIN score_grade sg
    ON sg.code = m.score_grade_code
ON CONFLICT DO NOTHING;
