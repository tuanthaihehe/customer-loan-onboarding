-- Customer Loan Onboarding - Unify Loan Application Draft Flow
-- PostgreSQL dialect
-- Version: V28
--
-- Scope:
-- - Move pre-submission onboarding from loan_application_draft into loan_application.
-- - Keep step payloads, but make them reference loan_application directly.
-- - Replace APP_DRAFT with explicit pre-submission states:
--   APP_CREATED, APP_IN_PROGRESS, APP_COMPLETED.

ALTER TABLE loan_application
ADD COLUMN IF NOT EXISTS current_step_code VARCHAR(50) REFERENCES loan_application_step(code),
ADD COLUMN IF NOT EXISTS expired_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_loan_application_current_step_code
ON loan_application(current_step_code);

DO $$
DECLARE
    draft_state_id UUID;
    created_state_id UUID;
BEGIN
    SELECT id
    INTO draft_state_id
    FROM loan_application_state
    WHERE code = 'APP_DRAFT';

    SELECT id
    INTO created_state_id
    FROM loan_application_state
    WHERE code = 'APP_CREATED';

    IF draft_state_id IS NOT NULL AND created_state_id IS NULL THEN
        UPDATE loan_application_state
        SET
            code = 'APP_CREATED',
            name = 'Đã tạo',
            description = 'Hồ sơ vay đã được tạo và chưa bắt đầu hoàn thiện thông tin.',
            is_initial = TRUE,
            is_terminal = FALSE,
            sort_order = 1
        WHERE code = 'APP_DRAFT';
    ELSIF draft_state_id IS NOT NULL AND created_state_id IS NOT NULL THEN
        UPDATE loan_application
        SET current_state_id = created_state_id
        WHERE current_state_id = draft_state_id;

        UPDATE loan_application_state_history
        SET to_state_id = created_state_id
        WHERE to_state_id = draft_state_id;

        UPDATE loan_application_state_history
        SET from_state_id = created_state_id
        WHERE from_state_id = draft_state_id;

        DELETE FROM loan_application_state_transition
        WHERE from_state_id = draft_state_id
           OR to_state_id = draft_state_id;

        DELETE FROM loan_application_state
        WHERE id = draft_state_id;
    END IF;
END $$;

UPDATE loan_application_state
SET is_initial = FALSE
WHERE is_initial = TRUE
  AND code <> 'APP_CREATED';

INSERT INTO loan_application_state
(id, code, name, description, is_initial, is_terminal, sort_order)
VALUES
    (
        '00000000-0000-0000-0000-000000000101',
        'APP_CREATED',
        'Đã tạo',
        'Hồ sơ vay đã được tạo và chưa bắt đầu hoàn thiện thông tin.',
        TRUE,
        FALSE,
        1
    ),
    (
        '00000000-0000-0000-0000-000000000108',
        'APP_IN_PROGRESS',
        'Đang hoàn thiện',
        'Hồ sơ vay đang được bổ sung và hoàn thiện thông tin theo các bước onboarding.',
        FALSE,
        FALSE,
        2
    ),
    (
        '00000000-0000-0000-0000-000000000109',
        'APP_COMPLETED',
        'Hoàn thành',
        'Hồ sơ vay đã hoàn thành thông tin onboarding và sẵn sàng nộp vào luồng xử lý.',
        FALSE,
        FALSE,
        3
    ),
    (
        '00000000-0000-0000-0000-000000000110',
        'APP_EXPIRED',
        'Hồ sơ hết hạn',
        'Hồ sơ vay hết hạn hoàn thiện và không tiếp tục xử lý.',
        FALSE,
        TRUE,
        10
    )
ON CONFLICT (code) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    is_initial = EXCLUDED.is_initial,
    is_terminal = EXCLUDED.is_terminal,
    sort_order = EXCLUDED.sort_order;

UPDATE loan_application_state
SET
    is_initial = FALSE,
    is_terminal = FALSE,
    sort_order = CASE code
        WHEN 'APP_SUBMITTED' THEN 4
        WHEN 'APP_NEEDS_SUPPLEMENT' THEN 5
        WHEN 'APP_IN_REVIEW' THEN 6
        WHEN 'APP_READY_FOR_CONTRACT' THEN 7
        WHEN 'APP_CONTRACTED' THEN 8
        WHEN 'APP_CANCELLED' THEN 9
        WHEN 'APP_EXPIRED' THEN 10
        ELSE sort_order
    END,
    description = CASE code
        WHEN 'APP_SUBMITTED' THEN 'Hồ sơ đã được gửi vào luồng xử lý.'
        ELSE description
    END
WHERE code IN (
    'APP_SUBMITTED',
    'APP_NEEDS_SUPPLEMENT',
    'APP_IN_REVIEW',
    'APP_READY_FOR_CONTRACT',
    'APP_CONTRACTED',
    'APP_CANCELLED',
    'APP_EXPIRED'
);

UPDATE loan_application_state
SET is_terminal = TRUE
WHERE code IN ('APP_CONTRACTED', 'APP_CANCELLED', 'APP_EXPIRED');

DROP TABLE IF EXISTS v28_draft_application_map;

CREATE TEMP TABLE v28_draft_application_map AS
SELECT
    d.id AS draft_id,
    COALESCE(d.converted_loan_application_id, d.id) AS loan_application_id,
    d.draft_code,
    d.customer_id,
    d.current_step_code,
    d.status,
    d.expired_at,
    d.created_at,
    d.updated_at
FROM loan_application_draft d;

INSERT INTO loan_application (
    id,
    loan_application_code,
    customer_id,
    current_state_id,
    requested_amount,
    current_step_code,
    expired_at,
    created_at,
    updated_at
)
SELECT
    m.loan_application_id,
    m.draft_code,
    m.customer_id,
    s.id,
    NULL,
    m.current_step_code,
    m.expired_at,
    m.created_at,
    m.updated_at
FROM v28_draft_application_map m
JOIN loan_application_state s
    ON s.code = CASE
        WHEN m.status = 'COMPLETED' THEN 'APP_COMPLETED'
        WHEN m.status IN ('CONVERTED') THEN 'APP_SUBMITTED'
        WHEN m.status = 'CANCELLED' THEN 'APP_CANCELLED'
        WHEN m.status = 'EXPIRED' THEN 'APP_EXPIRED'
        WHEN EXISTS (
            SELECT 1
            FROM loan_application_draft_step_data sd
            WHERE sd.draft_id = m.draft_id
              AND sd.status IN ('IN_PROGRESS', 'COMPLETED')
        ) THEN 'APP_IN_PROGRESS'
        ELSE 'APP_CREATED'
    END
WHERE NOT EXISTS (
    SELECT 1
    FROM loan_application la
    WHERE la.id = m.loan_application_id
       OR la.loan_application_code = m.draft_code
);

UPDATE loan_application la
SET
    current_step_code = COALESCE(la.current_step_code, m.current_step_code),
    expired_at = COALESCE(la.expired_at, m.expired_at),
    updated_at = GREATEST(la.updated_at, m.updated_at)
FROM v28_draft_application_map m
WHERE la.id = m.loan_application_id;

ALTER TABLE loan_application_draft_step_data
DROP CONSTRAINT IF EXISTS loan_application_draft_step_data_draft_id_fkey,
DROP CONSTRAINT IF EXISTS uq_loan_application_draft_step_data;

ALTER TABLE loan_application_draft_step_data
RENAME COLUMN draft_id TO loan_application_id;

UPDATE loan_application_draft_step_data sd
SET loan_application_id = m.loan_application_id
FROM v28_draft_application_map m
WHERE sd.loan_application_id = m.draft_id;

WITH ranked_step_data AS (
    SELECT
        id,
        ROW_NUMBER() OVER (
            PARTITION BY loan_application_id, step_code
            ORDER BY updated_at DESC, created_at DESC, id
        ) AS row_number
    FROM loan_application_draft_step_data
)
DELETE FROM loan_application_draft_step_data sd
USING ranked_step_data r
WHERE sd.id = r.id
  AND r.row_number > 1;

ALTER TABLE loan_application_draft_step_data
ADD CONSTRAINT fk_loan_application_step_data_application
    FOREIGN KEY (loan_application_id)
    REFERENCES loan_application(id)
    ON DELETE CASCADE,
ADD CONSTRAINT uq_loan_application_step_data
    UNIQUE (loan_application_id, step_code);

ALTER TABLE loan_application_draft_step_data
RENAME TO loan_application_step_data;

ALTER INDEX IF EXISTS idx_loan_application_draft_step_data_draft_id
RENAME TO idx_loan_application_step_data_application_id;

ALTER INDEX IF EXISTS idx_loan_application_draft_step_data_step_code
RENAME TO idx_loan_application_step_data_step_code;

ALTER INDEX IF EXISTS idx_loan_application_draft_step_data_status
RENAME TO idx_loan_application_step_data_status;

ALTER INDEX IF EXISTS idx_loan_application_draft_step_data_requires_review
RENAME TO idx_loan_application_step_data_requires_review;

ALTER INDEX IF EXISTS idx_loan_application_draft_step_data_invalidated_by
RENAME TO idx_loan_application_step_data_invalidated_by;

ALTER TABLE loan_application_step_data
RENAME CONSTRAINT chk_loan_application_draft_step_data_status
TO chk_loan_application_step_data_status;

ALTER TABLE loan_application_step_data
RENAME CONSTRAINT fk_loan_application_draft_step_data_invalidated_by_step
TO fk_loan_application_step_data_invalidated_by_step;

ALTER TABLE loan_application_draft_history
DROP CONSTRAINT IF EXISTS loan_application_draft_history_draft_id_fkey;

ALTER TABLE loan_application_draft_history
RENAME COLUMN draft_id TO loan_application_id;

UPDATE loan_application_draft_history h
SET loan_application_id = m.loan_application_id
FROM v28_draft_application_map m
WHERE h.loan_application_id = m.draft_id;

ALTER TABLE loan_application_draft_history
ADD CONSTRAINT fk_loan_application_step_history_application
    FOREIGN KEY (loan_application_id)
    REFERENCES loan_application(id)
    ON DELETE CASCADE;

ALTER TABLE loan_application_draft_history
RENAME TO loan_application_step_history;

ALTER INDEX IF EXISTS idx_loan_application_draft_history_draft_id
RENAME TO idx_loan_application_step_history_application_id;

ALTER INDEX IF EXISTS idx_loan_application_draft_history_step_code
RENAME TO idx_loan_application_step_history_step_code;

ALTER INDEX IF EXISTS idx_loan_application_draft_history_changed_at
RENAME TO idx_loan_application_step_history_changed_at;

ALTER TABLE loan_application_step_history
RENAME CONSTRAINT chk_loan_application_draft_history_action
TO chk_loan_application_step_history_action;

DROP TABLE IF EXISTS loan_application_draft CASCADE;

DROP TABLE IF EXISTS v28_draft_application_map;
