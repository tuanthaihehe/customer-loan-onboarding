-- Customer Loan Onboarding - Add Loan Purpose Catalog
-- PostgreSQL dialect
-- Version: V8
--
-- Scope:
-- - Create loan_purpose master data table.
-- - Replace loan_application.loan_purpose text/varchar field with loan_purpose_id.
--
-- Assumption:
-- - loan_application currently has no records, so dropping loan_purpose is safe.
--
-- Design notes:
-- - loan_purpose is master/reference data used by frontend dropdown.
-- - loan_application.loan_purpose_id is nullable because a draft application
--   may be created before the loan purpose is selected.

CREATE TABLE loan_purpose (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,

    CONSTRAINT uq_loan_purpose_code
        UNIQUE (code)
);

ALTER TABLE loan_application
DROP COLUMN IF EXISTS loan_purpose;

ALTER TABLE loan_application
ADD COLUMN IF NOT EXISTS loan_purpose_id UUID REFERENCES loan_purpose(id);

CREATE INDEX IF NOT EXISTS idx_loan_application_loan_purpose_id
ON loan_application(loan_purpose_id);
