-- Customer Loan Onboarding - Drop Mock Score Grade Rule
-- PostgreSQL dialect
-- Version: V26
--
-- Scope:
-- - Remove the old mock scoring rule table.
-- - Credit scoring bands are now managed by the credit_scoring_band schema.

DROP TABLE IF EXISTS mock_score_grade_rule;
