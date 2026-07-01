ALTER TABLE customer
ADD COLUMN IF NOT EXISTS permanent_address TEXT;

ALTER TABLE loan_application
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