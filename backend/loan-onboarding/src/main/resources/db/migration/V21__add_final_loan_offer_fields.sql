-- Customer Loan Onboarding - Add Final Loan Offer Fields
-- PostgreSQL dialect
-- Version: V21
--
-- Scope:
-- - Store the final selected loan offer for step 5.
-- - Keep the selected loan_product_id from V19 and add the final amount,
--   final tenor, payment method, payment day, estimated monthly payment,
--   score grade snapshot, and selected timestamp.

ALTER TABLE loan_application
ADD COLUMN IF NOT EXISTS final_requested_amount NUMERIC(18, 2),
ADD COLUMN IF NOT EXISTS final_loan_term_months INT,
ADD COLUMN IF NOT EXISTS final_payment_method VARCHAR(50),
ADD COLUMN IF NOT EXISTS final_monthly_payment_day INT,
ADD COLUMN IF NOT EXISTS final_selected_amount NUMERIC(18, 2),
ADD COLUMN IF NOT EXISTS final_estimated_monthly_payment NUMERIC(18, 2),
ADD COLUMN IF NOT EXISTS final_score_grade VARCHAR(10),
ADD COLUMN IF NOT EXISTS final_selected_at TIMESTAMP;

ALTER TABLE loan_application
DROP CONSTRAINT IF EXISTS chk_loan_application_final_requested_amount;

ALTER TABLE loan_application
ADD CONSTRAINT chk_loan_application_final_requested_amount
CHECK (
    final_requested_amount IS NULL
    OR final_requested_amount > 0
);

ALTER TABLE loan_application
DROP CONSTRAINT IF EXISTS chk_loan_application_final_loan_term_months;

ALTER TABLE loan_application
ADD CONSTRAINT chk_loan_application_final_loan_term_months
CHECK (
    final_loan_term_months IS NULL
    OR final_loan_term_months > 0
);

ALTER TABLE loan_application
DROP CONSTRAINT IF EXISTS chk_loan_application_final_monthly_payment_day;

ALTER TABLE loan_application
ADD CONSTRAINT chk_loan_application_final_monthly_payment_day
CHECK (
    final_monthly_payment_day IS NULL
    OR (final_monthly_payment_day >= 1 AND final_monthly_payment_day <= 28)
);

ALTER TABLE loan_application
DROP CONSTRAINT IF EXISTS chk_loan_application_final_selected_amount;

ALTER TABLE loan_application
ADD CONSTRAINT chk_loan_application_final_selected_amount
CHECK (
    final_selected_amount IS NULL
    OR final_selected_amount > 0
);

ALTER TABLE loan_application
DROP CONSTRAINT IF EXISTS chk_loan_application_final_estimated_monthly_payment;

ALTER TABLE loan_application
ADD CONSTRAINT chk_loan_application_final_estimated_monthly_payment
CHECK (
    final_estimated_monthly_payment IS NULL
    OR final_estimated_monthly_payment >= 0
);
