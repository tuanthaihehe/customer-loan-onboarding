-- Customer Loan Onboarding - Simplify Loan Application Draft Step Flow
-- PostgreSQL dialect
-- Version: V22
--
-- Scope:
-- - Remove persisted LOCKED step status because it can be derived from step order
--   and prerequisite step states in API responses.
-- - Remove duplicate draft terminal timestamps; audit timing is represented in
--   loan_application_draft_history.changed_at.
-- - Remove loan_application_draft_history.created_at because history rows already
--   store changed_at.

UPDATE loan_application_draft_step_data
SET
    status = 'NOT_STARTED',
    updated_at = CURRENT_TIMESTAMP
WHERE status = 'LOCKED';

ALTER TABLE loan_application_draft_step_data
DROP CONSTRAINT IF EXISTS chk_loan_application_draft_step_data_status;

ALTER TABLE loan_application_draft_step_data
ADD CONSTRAINT chk_loan_application_draft_step_data_status
CHECK (status IN (
    'NOT_STARTED',
    'IN_PROGRESS',
    'COMPLETED',
    'INVALIDATED'
));

ALTER TABLE loan_application_draft
DROP COLUMN IF EXISTS cancelled_at,
DROP COLUMN IF EXISTS converted_at;

ALTER TABLE loan_application_draft_history
DROP COLUMN IF EXISTS created_at;
