-- Customer Loan Onboarding - Add Loan Product Catalog
-- PostgreSQL dialect
-- Version: V10
--
-- Scope:
-- - Create score_grade catalog.
-- - Create loan_product catalog.
-- - Create mapping tables between loan_product and:
--   loan_purpose, vehicle_type, loan_term, score_grade.
--
-- Notes:
-- - score_grade is created here because it does not exist yet.
-- - loan_application is not changed in this migration.
--   This migration supports product matching/suggestion.
--   If the system later needs to store the selected product on an application,
--   add loan_application.loan_product_id in a later migration.

CREATE TABLE score_grade (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    code VARCHAR(10) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,

    CONSTRAINT uq_score_grade_code
        UNIQUE (code)
);

CREATE TABLE loan_product (
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

    CONSTRAINT uq_loan_product_code
        UNIQUE (product_code),

    CONSTRAINT chk_loan_product_amount
        CHECK (min_loan_amount > 0 AND max_loan_amount >= min_loan_amount),

    CONSTRAINT chk_loan_product_ltv
        CHECK (max_ltv_percent > 0 AND max_ltv_percent <= 100),

    CONSTRAINT chk_loan_product_interest_rate
        CHECK (monthly_interest_rate_percent > 0)
);

CREATE TABLE loan_product_purpose (
    loan_product_id UUID NOT NULL REFERENCES loan_product(id) ON DELETE CASCADE,
    loan_purpose_id UUID NOT NULL REFERENCES loan_purpose(id),

    PRIMARY KEY (loan_product_id, loan_purpose_id)
);

CREATE TABLE loan_product_vehicle_type (
    loan_product_id UUID NOT NULL REFERENCES loan_product(id) ON DELETE CASCADE,
    vehicle_type_id UUID NOT NULL REFERENCES vehicle_type(id),

    PRIMARY KEY (loan_product_id, vehicle_type_id)
);

CREATE TABLE loan_product_term (
    loan_product_id UUID NOT NULL REFERENCES loan_product(id) ON DELETE CASCADE,
    loan_term_id UUID NOT NULL REFERENCES loan_term(id),

    PRIMARY KEY (loan_product_id, loan_term_id)
);

CREATE TABLE loan_product_score_grade (
    loan_product_id UUID NOT NULL REFERENCES loan_product(id) ON DELETE CASCADE,
    score_grade_id UUID NOT NULL REFERENCES score_grade(id),

    PRIMARY KEY (loan_product_id, score_grade_id)
);

CREATE INDEX idx_loan_product_purpose_purpose_id
ON loan_product_purpose(loan_purpose_id);

CREATE INDEX idx_loan_product_vehicle_type_vehicle_type_id
ON loan_product_vehicle_type(vehicle_type_id);

CREATE INDEX idx_loan_product_term_loan_term_id
ON loan_product_term(loan_term_id);

CREATE INDEX idx_loan_product_score_grade_score_grade_id
ON loan_product_score_grade(score_grade_id);
