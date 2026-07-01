-- Customer Loan Onboarding - Add Loan Term Catalog
-- PostgreSQL dialect
-- Version: V9
--
-- Scope:
-- - Create loan_term master data table.
-- - Add loan_application.loan_term_id referencing loan_term(id).
-- - Keep loan_application.loan_term_months as a snapshot value.
--
-- Current loan_application has no records, so this migration is safe.
--
-- Design notes:
-- - loan_term is reference data used by frontend dropdown.
-- - loan_application.loan_term_id stores the selected term option.
-- - loan_application.loan_term_months stores the actual selected number of months
--   as a snapshot on the loan application.

CREATE TABLE loan_term (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    code VARCHAR(50) NOT NULL,
    term_months INT NOT NULL,

    name VARCHAR(100) NOT NULL,
    description TEXT,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,

    CONSTRAINT uq_loan_term_code
        UNIQUE (code),

    CONSTRAINT uq_loan_term_months
        UNIQUE (term_months),

    CONSTRAINT chk_loan_term_months
        CHECK (term_months > 0)
);

ALTER TABLE loan_application
ADD COLUMN IF NOT EXISTS loan_term_id UUID REFERENCES loan_term(id);

CREATE INDEX IF NOT EXISTS idx_loan_application_loan_term_id
ON loan_application(loan_term_id);

-- Replace the old loose validation with the currently allowed terms.
-- loan_term_months is still nullable because a draft application may be created
-- before the loan term is selected.

ALTER TABLE loan_application
DROP CONSTRAINT IF EXISTS chk_loan_application_loan_term_months;

ALTER TABLE loan_application
ADD CONSTRAINT chk_loan_application_loan_term_months
CHECK (
    loan_term_months IS NULL
    OR loan_term_months IN (3, 6, 9, 12, 18, 24)
);
