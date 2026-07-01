-- Customer Loan Onboarding - Add Preliminary Applicant Snapshot
-- PostgreSQL dialect
-- Version: V13
--
-- Scope:
-- - Store preliminary applicant information on loan_application.
-- - Identity fields are snapshotted from the identified customer at the time the draft is saved.
-- - Additional preliminary fields are entered by staff on the preliminary loan need screen.

ALTER TABLE loan_application
ADD COLUMN applicant_full_name VARCHAR(255),
ADD COLUMN applicant_identity_number VARCHAR(20),
ADD COLUMN applicant_phone_number VARCHAR(20),
ADD COLUMN applicant_date_of_birth DATE,
ADD COLUMN applicant_gender VARCHAR(30),
ADD COLUMN applicant_occupation VARCHAR(100),
ADD COLUMN applicant_monthly_income NUMERIC(18, 2);

ALTER TABLE loan_application
ADD CONSTRAINT chk_loan_application_applicant_gender
CHECK (
    applicant_gender IS NULL
    OR applicant_gender IN ('MALE', 'FEMALE', 'OTHER')
);

ALTER TABLE loan_application
ADD CONSTRAINT chk_loan_application_applicant_monthly_income
CHECK (
    applicant_monthly_income IS NULL
    OR applicant_monthly_income > 0
);
