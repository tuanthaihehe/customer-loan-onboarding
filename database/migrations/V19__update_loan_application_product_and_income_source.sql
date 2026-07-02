-- Customer Loan Onboarding - Update Loan Application Product and Income Source
-- PostgreSQL dialect
-- Version: V19
--
-- Scope:
-- - Remove duplicated applicant snapshot fields from loan_application.
-- - Add selected loan product reference to loan_application.
-- - Create income_source catalog.
-- - Add income_source_id reference to loan_application.
--
-- Design notes:
-- - Customer stable information should stay in customer.
-- - Application-specific financial/work information stays in loan_application.
-- - applicant_occupation and applicant_monthly_income are removed because they overlap
--   with occupation_id and monthly_income_amount.
-- - loan_product_id stores which loan product was selected for the application.
-- - income_source_id stores the applicant's declared source of income using a catalog.

ALTER TABLE loan_application
DROP COLUMN IF EXISTS applicant_full_name,
DROP COLUMN IF EXISTS applicant_identity_number,
DROP COLUMN IF EXISTS applicant_phone_number,
DROP COLUMN IF EXISTS applicant_date_of_birth,
DROP COLUMN IF EXISTS applicant_gender,
DROP COLUMN IF EXISTS applicant_occupation,
DROP COLUMN IF EXISTS applicant_monthly_income;

ALTER TABLE loan_application
ADD COLUMN IF NOT EXISTS loan_product_id UUID REFERENCES loan_product(id);

CREATE INDEX IF NOT EXISTS idx_loan_application_loan_product_id
ON loan_application(loan_product_id);

CREATE TABLE IF NOT EXISTS income_source (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,

    CONSTRAINT uq_income_source_code
        UNIQUE (code)
);

ALTER TABLE loan_application
ADD COLUMN IF NOT EXISTS income_source_id UUID REFERENCES income_source(id);

CREATE INDEX IF NOT EXISTS idx_loan_application_income_source_id
ON loan_application(income_source_id);
