-- Customer Loan Onboarding - Refine Loan Application Draft Review Flow
-- PostgreSQL dialect
-- Version: V24
--
-- Scope:
-- - Represent stale downstream step payloads with requires_review instead of
--   special step statuses.
-- - Keep downstream payload data intact when an earlier step changes.
-- - Restrict draft step status to workflow progress only:
--   NOT_STARTED, IN_PROGRESS, COMPLETED.
-- - Add history metadata for audit details such as changed step and affected step.

ALTER TABLE loan_application_draft_step_data
ADD COLUMN IF NOT EXISTS requires_review BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS invalidated_by_step_code VARCHAR(50),
ADD COLUMN IF NOT EXISTS invalidated_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS reviewed_at TIMESTAMP;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_loan_application_draft_step_data_invalidated_by_step'
          AND conrelid = 'loan_application_draft_step_data'::regclass
    ) THEN
        ALTER TABLE loan_application_draft_step_data
        ADD CONSTRAINT fk_loan_application_draft_step_data_invalidated_by_step
        FOREIGN KEY (invalidated_by_step_code)
        REFERENCES loan_application_step(code);
    END IF;
END $$;

UPDATE loan_application_draft_step_data
SET
    status = 'NOT_STARTED',
    updated_at = CURRENT_TIMESTAMP
WHERE status = 'LOCKED';

UPDATE loan_application_draft_step_data
SET
    status = 'COMPLETED',
    requires_review = TRUE,
    invalidated_at = COALESCE(invalidated_at, CURRENT_TIMESTAMP),
    updated_at = CURRENT_TIMESTAMP
WHERE status = 'INVALIDATED';

ALTER TABLE loan_application_draft_step_data
DROP CONSTRAINT IF EXISTS chk_loan_application_draft_step_data_status;

ALTER TABLE loan_application_draft_step_data
ADD CONSTRAINT chk_loan_application_draft_step_data_status
CHECK (status IN (
    'NOT_STARTED',
    'IN_PROGRESS',
    'COMPLETED'
));

CREATE INDEX IF NOT EXISTS idx_loan_application_draft_step_data_requires_review
ON loan_application_draft_step_data(draft_id, requires_review);

CREATE INDEX IF NOT EXISTS idx_loan_application_draft_step_data_invalidated_by
ON loan_application_draft_step_data(invalidated_by_step_code);

ALTER TABLE loan_application_draft_history
ADD COLUMN IF NOT EXISTS metadata JSONB NOT NULL DEFAULT '{}'::jsonb;
