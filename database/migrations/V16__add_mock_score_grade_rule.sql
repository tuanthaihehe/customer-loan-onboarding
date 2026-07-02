-- Customer Loan Onboarding - Add Mock Score Grade Rule
-- PostgreSQL dialect
-- Version: V16
--
-- Scope:
-- - Create mock_score_grade_rule table to simulate a customer scoring service.
-- - The table stores configurable rule ranges and maps each matching rule to score_grade.
--
-- Design notes:
-- - score_grade table must already exist.
-- - A rule matches when all non-null min/max conditions are satisfied.
-- - sort_order controls rule priority. The first matching rule should be selected.
-- - This table is for mock/demo scoring, not a production scoring engine.

CREATE TABLE mock_score_grade_rule (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    rule_code VARCHAR(50) NOT NULL,
    rule_name VARCHAR(255) NOT NULL,
    description TEXT,

    min_monthly_income_amount NUMERIC(18, 2),
    max_monthly_income_amount NUMERIC(18, 2),

    min_requested_amount NUMERIC(18, 2),
    max_requested_amount NUMERIC(18, 2),

    min_ltv_percent NUMERIC(5, 2),
    max_ltv_percent NUMERIC(5, 2),

    score_grade_id UUID NOT NULL REFERENCES score_grade(id),

    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,

    CONSTRAINT uq_mock_score_grade_rule_code
        UNIQUE (rule_code),

    CONSTRAINT chk_mock_score_income_range
        CHECK (
            min_monthly_income_amount IS NULL
            OR max_monthly_income_amount IS NULL
            OR min_monthly_income_amount <= max_monthly_income_amount
        ),

    CONSTRAINT chk_mock_score_requested_amount_range
        CHECK (
            min_requested_amount IS NULL
            OR max_requested_amount IS NULL
            OR min_requested_amount <= max_requested_amount
        ),

    CONSTRAINT chk_mock_score_ltv_range
        CHECK (
            min_ltv_percent IS NULL
            OR max_ltv_percent IS NULL
            OR min_ltv_percent <= max_ltv_percent
        )
);

CREATE INDEX idx_mock_score_grade_rule_score_grade_id
ON mock_score_grade_rule(score_grade_id);

CREATE INDEX idx_mock_score_grade_rule_active_sort
ON mock_score_grade_rule(is_active, sort_order);
