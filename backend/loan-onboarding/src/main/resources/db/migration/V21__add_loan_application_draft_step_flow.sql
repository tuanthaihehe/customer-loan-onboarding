-- Customer Loan Onboarding - Add Loan Application Draft Step Flow
-- PostgreSQL dialect
-- Version: V21
--
-- Scope:
-- - Add a separate draft container for step-by-step loan onboarding.
-- - Keep real loan_application creation until the draft is completed and converted.
-- - Store each step payload separately as JSONB.
-- - Add draft history because the existing schema already tracks loan application lifecycle history.
--
-- Design notes:
-- - The existing customer.id and loan_application.id primary keys are UUID.
-- - Status values follow the current project style: VARCHAR with CHECK constraints.
-- - TIMESTAMP is used consistently with existing business tables.
-- - No staff/user table exists in the schema, so draft history does not include changed_by.
-- - No deleted_at column is added because soft delete is not a consistent convention in current tables.

CREATE TABLE IF NOT EXISTS loan_application_step (
    code VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    step_order INT NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_loan_application_step_order
        UNIQUE (step_order)
);

CREATE INDEX IF NOT EXISTS idx_loan_application_step_active_order
ON loan_application_step(is_active, step_order);

CREATE TABLE IF NOT EXISTS loan_application_draft (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    draft_code VARCHAR(50) NOT NULL,
    customer_id UUID NOT NULL REFERENCES customer(id),
    current_step_code VARCHAR(50) NOT NULL REFERENCES loan_application_step(code),

    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    converted_loan_application_id UUID REFERENCES loan_application(id),

    expired_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    converted_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_loan_application_draft_code
        UNIQUE (draft_code),

    CONSTRAINT chk_loan_application_draft_status
        CHECK (status IN (
            'DRAFT',
            'COMPLETED',
            'CONVERTED',
            'CANCELLED',
            'EXPIRED'
        ))
);

CREATE INDEX IF NOT EXISTS idx_loan_application_draft_customer_id
ON loan_application_draft(customer_id);

CREATE INDEX IF NOT EXISTS idx_loan_application_draft_status
ON loan_application_draft(status);

CREATE INDEX IF NOT EXISTS idx_loan_application_draft_current_step_code
ON loan_application_draft(current_step_code);

CREATE INDEX IF NOT EXISTS idx_loan_application_draft_converted_application_id
ON loan_application_draft(converted_loan_application_id);

CREATE TABLE IF NOT EXISTS loan_application_draft_step_data (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    draft_id UUID NOT NULL REFERENCES loan_application_draft(id) ON DELETE CASCADE,
    step_code VARCHAR(50) NOT NULL REFERENCES loan_application_step(code),

    status VARCHAR(30) NOT NULL DEFAULT 'NOT_STARTED',
    payload JSONB NOT NULL DEFAULT '{}'::jsonb,

    completed_at TIMESTAMP,
    invalidated_at TIMESTAMP,
    invalidated_reason TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_loan_application_draft_step_data
        UNIQUE (draft_id, step_code),

    CONSTRAINT chk_loan_application_draft_step_data_status
        CHECK (status IN (
            'NOT_STARTED',
            'IN_PROGRESS',
            'COMPLETED',
            'INVALIDATED',
            'LOCKED'
        ))
);

CREATE INDEX IF NOT EXISTS idx_loan_application_draft_step_data_draft_id
ON loan_application_draft_step_data(draft_id);

CREATE INDEX IF NOT EXISTS idx_loan_application_draft_step_data_step_code
ON loan_application_draft_step_data(step_code);

CREATE INDEX IF NOT EXISTS idx_loan_application_draft_step_data_status
ON loan_application_draft_step_data(status);

CREATE TABLE IF NOT EXISTS loan_application_draft_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    draft_id UUID NOT NULL REFERENCES loan_application_draft(id) ON DELETE CASCADE,
    step_code VARCHAR(50) REFERENCES loan_application_step(code),

    action VARCHAR(50) NOT NULL,
    old_status VARCHAR(30),
    new_status VARCHAR(30),
    note TEXT,

    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_loan_application_draft_history_action
        CHECK (action IN (
            'CREATE_DRAFT',
            'START_STEP',
            'SAVE_STEP',
            'COMPLETE_STEP',
            'REOPEN_STEP',
            'INVALIDATE_STEP',
            'CANCEL_DRAFT',
            'EXPIRE_DRAFT',
            'COMPLETE_DRAFT',
            'CONVERT_DRAFT'
        ))
);

CREATE INDEX IF NOT EXISTS idx_loan_application_draft_history_draft_id
ON loan_application_draft_history(draft_id);

CREATE INDEX IF NOT EXISTS idx_loan_application_draft_history_step_code
ON loan_application_draft_history(step_code);

CREATE INDEX IF NOT EXISTS idx_loan_application_draft_history_changed_at
ON loan_application_draft_history(changed_at);
