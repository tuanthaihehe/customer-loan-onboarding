-- Customer Loan Onboarding - Superseded Draft Flow Seed
-- PostgreSQL dialect
-- Version: V13 seed
--
-- This seed is intentionally left as a no-op.
--
-- The old V13 demo data used the previous 6-step draft flow:
-- CUSTOMER_IDENTIFY -> PRELIMINARY_INFO -> CUSTOMER_DETAIL -> ASSET_DETAIL
-- -> FINAL_LOAN_PROPOSAL -> UPLOAD_COMPLETE.
--
-- The current draft flow merges the old steps 3, 4 and 5 into:
-- CUSTOMER_ASSET_LOAN_PROPOSAL.
--
-- Run V14__seed_loan_application_draft_review_flow.sql for the current
-- 4-step demo draft data.

SELECT 'V13 draft flow seed superseded by V14 merged-step seed.' AS seed_note;
