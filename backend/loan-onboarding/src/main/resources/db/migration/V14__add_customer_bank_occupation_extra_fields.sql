-- Customer Loan Onboarding - Add Customer, Bank, Occupation and Extra Application Fields
-- PostgreSQL dialect
-- Version: V14
--
-- Scope:
-- - Normalize DA/BA files V13_add_additional_customer_info_and_additional_loan_info.sql
--   and V14_add_extra_fields_customer_and_loan_application.sql into a valid Flyway version.
-- - Add customer profile fields.
-- - Create bank and occupation catalogs.
-- - Add loan application bank, occupation, address, workplace, and income fields.

-- =========================================================
-- 1. Add customer profile fields
-- =========================================================

ALTER TABLE customer
ADD COLUMN IF NOT EXISTS gender VARCHAR(20),
ADD COLUMN IF NOT EXISTS email VARCHAR(255),
ADD COLUMN IF NOT EXISTS marital_status VARCHAR(30),
ADD COLUMN IF NOT EXISTS permanent_address TEXT;

ALTER TABLE customer
DROP CONSTRAINT IF EXISTS chk_customer_gender;

ALTER TABLE customer
ADD CONSTRAINT chk_customer_gender
CHECK (
    gender IS NULL
    OR gender IN ('MALE', 'FEMALE')
);

ALTER TABLE customer
DROP CONSTRAINT IF EXISTS chk_customer_marital_status;

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

CREATE TABLE IF NOT EXISTS bank (
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

CREATE TABLE IF NOT EXISTS occupation (
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

ALTER TABLE loan_application
ADD COLUMN IF NOT EXISTS occupation_id UUID REFERENCES occupation(id),
ADD COLUMN IF NOT EXISTS disbursement_bank_id UUID REFERENCES bank(id),
ADD COLUMN IF NOT EXISTS disbursement_account_number VARCHAR(50),
ADD COLUMN IF NOT EXISTS disbursement_account_name VARCHAR(255),
ADD COLUMN IF NOT EXISTS current_address TEXT,
ADD COLUMN IF NOT EXISTS workplace_name VARCHAR(255),
ADD COLUMN IF NOT EXISTS workplace_address TEXT,
ADD COLUMN IF NOT EXISTS monthly_income_amount NUMERIC(18, 2);

ALTER TABLE loan_application
DROP CONSTRAINT IF EXISTS chk_loan_application_monthly_income_amount;

ALTER TABLE loan_application
ADD CONSTRAINT chk_loan_application_monthly_income_amount
CHECK (
    monthly_income_amount IS NULL
    OR monthly_income_amount >= 0
);

CREATE INDEX IF NOT EXISTS idx_loan_application_occupation_id
ON loan_application(occupation_id);

CREATE INDEX IF NOT EXISTS idx_loan_application_disbursement_bank_id
ON loan_application(disbursement_bank_id);
