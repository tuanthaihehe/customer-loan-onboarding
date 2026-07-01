-- Customer Loan Onboarding - Add loan term and loan purpose enum
-- PostgreSQL dialect
--
-- Scope:
-- - Add loan term fields to loan_application.
-- - Convert loan_purpose from free text to controlled enum-like values using CHECK constraint.
--
-- Design notes:
-- - loan_term_months is nullable because a draft application may be created before the loan term is known.
-- - loan_purpose remains a column on loan_application because loan purpose belongs to the application context.
-- - PostgreSQL CHECK constraint is used instead of CREATE TYPE enum to keep future changes easier during early design phase.
-- - Existing free-text loan_purpose values that do not match the new enum values are set to NULL before applying the constraint.

-- =========================================================
-- 1. Add loan term fields
-- =========================================================

ALTER TABLE loan_application
ADD COLUMN loan_term_months INT;

ALTER TABLE loan_application
ADD CONSTRAINT chk_loan_application_loan_term_months
CHECK (loan_term_months IS NULL OR loan_term_months > 0);

COMMENT ON COLUMN loan_application.loan_term_months IS
'Loan term in months. Nullable while the application is still a draft.';

-- =========================================================
-- 2. Normalize loan_purpose into enum-like values
-- =========================================================

-- Allowed loan_purpose values:
-- BUSINESS              : Business or working capital purpose
-- PERSONAL_CONSUMPTION  : Personal consumption purpose
-- VEHICLE_REPAIR        : Vehicle repair or maintenance purpose
-- MEDICAL               : Medical expense purpose
-- EDUCATION             : Education expense purpose
-- HOME_REPAIR           : Home repair or renovation purpose
-- DEBT_REPAYMENT        : Debt repayment or refinancing purpose
-- OTHER                 : Other purpose not covered by the predefined values

-- Existing V1/V2 demo data may contain free-text Vietnamese descriptions.
-- Those values are not valid enum values, so they are cleared before adding the constraint.
UPDATE loan_application
SET loan_purpose = NULL
WHERE loan_purpose IS NOT NULL
  AND loan_purpose NOT IN (
    'BUSINESS',
    'PERSONAL_CONSUMPTION',
    'VEHICLE_REPAIR',
    'MEDICAL',
    'EDUCATION',
    'HOME_REPAIR',
    'DEBT_REPAYMENT',
    'OTHER'
  );

ALTER TABLE loan_application
ALTER COLUMN loan_purpose TYPE VARCHAR(50);

ALTER TABLE loan_application
ADD CONSTRAINT chk_loan_application_loan_purpose
CHECK (
    loan_purpose IS NULL
    OR loan_purpose IN (
        'BUSINESS',
        'PERSONAL_CONSUMPTION',
        'VEHICLE_REPAIR',
        'MEDICAL',
        'EDUCATION',
        'HOME_REPAIR',
        'DEBT_REPAYMENT',
        'OTHER'
    )
);

COMMENT ON COLUMN loan_application.loan_purpose IS
'Controlled loan purpose code. Nullable while the application is still a draft.';

-- =========================================================
-- 3. Optional index for filtering/reporting by purpose and term
-- =========================================================

CREATE INDEX idx_loan_application_loan_purpose
ON loan_application(loan_purpose);

CREATE INDEX idx_loan_application_loan_term_months
ON loan_application(loan_term_months);
