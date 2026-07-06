-- Add customer profile fields and loan application bank/occupation reference fields
-- PostgreSQL dialect

-- =========================================================
-- 1. Add customer profile fields
-- =========================================================

ALTER TABLE customer
ADD COLUMN gender VARCHAR(20),
ADD COLUMN email VARCHAR(255),
ADD COLUMN marital_status VARCHAR(30);

ALTER TABLE customer
ADD CONSTRAINT chk_customer_gender
CHECK (
    gender IS NULL
    OR gender IN ('MALE', 'FEMALE')
);

ALTER TABLE customer
ADD CONSTRAINT chk_customer_marital_status
CHECK (
    marital_status IS NULL
    OR marital_status IN ('SINGLE', 'MARRIED')
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_customer_email
ON customer(email)
WHERE email IS NOT NULL;

-- =========================================================
-- 2. Create bank catalog
-- =========================================================

CREATE TABLE bank (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    short_name VARCHAR(100),

    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,

    CONSTRAINT uq_bank_code UNIQUE (code)
);

-- =========================================================
-- 3. Create occupation catalog
-- =========================================================

CREATE TABLE occupation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,

    CONSTRAINT uq_occupation_code UNIQUE (code)
);

-- =========================================================
-- 4. Add loan application reference fields
-- =========================================================
-- These fields belong to loan_application because they are collected
-- for a specific loan application and may differ between applications.

ALTER TABLE loan_application
ADD COLUMN occupation_id UUID REFERENCES occupation(id),
ADD COLUMN disbursement_bank_id UUID REFERENCES bank(id),
ADD COLUMN disbursement_account_number VARCHAR(50),
ADD COLUMN disbursement_account_name VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_loan_application_occupation_id
ON loan_application(occupation_id);

CREATE INDEX IF NOT EXISTS idx_loan_application_disbursement_bank_id
ON loan_application(disbursement_bank_id);