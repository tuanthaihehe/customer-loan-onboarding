-- Customer Loan Onboarding - Move draft step flow to loan_application
-- PostgreSQL dialect
-- Version: V11
--
-- Goal:
-- - loan_application is created at the beginning of the onboarding flow.
-- - loan_application.current_state_id owns the real lifecycle state.
-- - step payload is stored against loan_application, not a separate draft entity.
-- - old loan_application_draft tables are removed to avoid duplicate workflow storage.

-- =========================================================
-- 1. Align loan application states with onboarding state diagram
-- =========================================================

UPDATE loan_application_state
SET is_initial = FALSE
WHERE is_initial = TRUE;

INSERT INTO loan_application_state
(id, code, name, description, is_initial, is_terminal, sort_order)
VALUES
    ('00000000-0000-0000-0000-000000000121', 'APP_CREATED', 'Hồ sơ được khởi tạo', 'Hồ sơ vay đã được tạo ban đầu.', TRUE, FALSE, 1),
    ('00000000-0000-0000-0000-000000000122', 'APP_IN_PROGRESS', 'Hồ sơ đang được hoàn thiện', 'Hồ sơ đang được nhập thông tin và chứng từ.', FALSE, FALSE, 2),
    ('00000000-0000-0000-0000-000000000123', 'APP_COMPLETED', 'Đã hoàn thành', 'Hồ sơ đã hoàn thiện thông tin và sẵn sàng nộp.', FALSE, FALSE, 3),
    ('00000000-0000-0000-0000-000000000124', 'APP_EXPIRED', 'Hồ sơ hết hạn', 'Hồ sơ quá hạn do không thao tác trong thời gian quy định.', FALSE, TRUE, 5)
ON CONFLICT (code) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    is_initial = EXCLUDED.is_initial,
    is_terminal = EXCLUDED.is_terminal,
    sort_order = EXCLUDED.sort_order;

UPDATE loan_application_state
SET
    name = 'Đã nộp hồ sơ',
    description = 'Hồ sơ đã được nộp sang bước thẩm định.',
    is_initial = FALSE,
    is_terminal = FALSE,
    sort_order = 4
WHERE code = 'APP_SUBMITTED';

UPDATE loan_application_state
SET
    name = 'Hồ sơ bị hủy',
    description = 'Hồ sơ bị hủy và không tiếp tục xử lý.',
    is_initial = FALSE,
    is_terminal = TRUE,
    sort_order = 6
WHERE code = 'APP_CANCELLED';

DELETE FROM loan_application_state_transition
WHERE action_code IN (
    'IDENTIFY_COMPLETE',
    'COMPLETE_APPLICATION',
    'SUBMIT',
    'EXPIRE',
    'CANCEL',
    'REOPEN'
);

INSERT INTO loan_application_state_transition
(id, from_state_id, to_state_id, action_code, action_name, description)
SELECT gen_random_uuid(), from_state.id, to_state.id, transition.action_code, transition.action_name, transition.description
FROM (
    VALUES
        ('APP_CREATED', 'APP_IN_PROGRESS', 'IDENTIFY_COMPLETE', 'Định danh xong', 'Chuyển hồ sơ từ khởi tạo sang đang hoàn thiện.'),
        ('APP_IN_PROGRESS', 'APP_COMPLETED', 'COMPLETE_APPLICATION', 'Hoàn thiện hồ sơ', 'Hoàn thành nhập thông tin và chứng từ.'),
        ('APP_COMPLETED', 'APP_SUBMITTED', 'SUBMIT', 'Nộp hồ sơ', 'Nộp hồ sơ sang bước thẩm định.'),
        ('APP_IN_PROGRESS', 'APP_EXPIRED', 'EXPIRE', 'Hết hạn hồ sơ', 'Hồ sơ đang hoàn thiện bị hết hạn.'),
        ('APP_COMPLETED', 'APP_EXPIRED', 'EXPIRE', 'Hết hạn hồ sơ', 'Hồ sơ đã hoàn thiện nhưng chưa nộp bị hết hạn.'),
        ('APP_CREATED', 'APP_CANCELLED', 'CANCEL', 'Hủy hồ sơ', 'Hủy hồ sơ mới khởi tạo.'),
        ('APP_IN_PROGRESS', 'APP_CANCELLED', 'CANCEL', 'Hủy hồ sơ', 'Hủy hồ sơ đang hoàn thiện.'),
        ('APP_COMPLETED', 'APP_CANCELLED', 'CANCEL', 'Hủy hồ sơ', 'Hủy hồ sơ đã hoàn thiện nhưng chưa nộp.'),
        ('APP_COMPLETED', 'APP_IN_PROGRESS', 'REOPEN', 'Chỉnh sửa lại hồ sơ', 'Quay lại chỉnh sửa thông tin hoặc chứng từ.')
) AS transition(from_code, to_code, action_code, action_name, description)
JOIN loan_application_state from_state ON from_state.code = transition.from_code
JOIN loan_application_state to_state ON to_state.code = transition.to_code
ON CONFLICT (from_state_id, to_state_id, action_code) DO NOTHING;

-- =========================================================
-- 2. Add application-level workflow columns
-- =========================================================

ALTER TABLE loan_application
ADD COLUMN IF NOT EXISTS current_step_code VARCHAR(50) REFERENCES loan_application_step(code),
ADD COLUMN IF NOT EXISTS expired_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_loan_application_current_step_code
ON loan_application(current_step_code);

CREATE INDEX IF NOT EXISTS idx_loan_application_updated_at
ON loan_application(updated_at);

UPDATE loan_application
SET current_state_id = (
    SELECT id
    FROM loan_application_state
    WHERE code = 'APP_CREATED'
)
WHERE current_state_id = (
    SELECT id
    FROM loan_application_state
    WHERE code = 'APP_DRAFT'
);

UPDATE loan_application
SET current_step_code = 'CUSTOMER_IDENTIFY'
WHERE current_step_code IS NULL
  AND EXISTS (
      SELECT 1
      FROM loan_application_step
      WHERE code = 'CUSTOMER_IDENTIFY'
  );

UPDATE loan_application_state_history
SET from_state_id = (
    SELECT id
    FROM loan_application_state
    WHERE code = 'APP_CREATED'
)
WHERE from_state_id = (
    SELECT id
    FROM loan_application_state
    WHERE code = 'APP_DRAFT'
);

UPDATE loan_application_state_history
SET to_state_id = (
    SELECT id
    FROM loan_application_state
    WHERE code = 'APP_CREATED'
)
WHERE to_state_id = (
    SELECT id
    FROM loan_application_state
    WHERE code = 'APP_DRAFT'
);

DELETE FROM loan_application_state_transition
WHERE from_state_id = (
    SELECT id
    FROM loan_application_state
    WHERE code = 'APP_DRAFT'
)
   OR to_state_id = (
       SELECT id
       FROM loan_application_state
       WHERE code = 'APP_DRAFT'
   );

DELETE FROM loan_application_state
WHERE code = 'APP_DRAFT';

-- =========================================================
-- 3. Application step payload and history
-- =========================================================

CREATE TABLE IF NOT EXISTS loan_application_step_data (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    loan_application_id UUID NOT NULL REFERENCES loan_application(id) ON DELETE CASCADE,
    step_code VARCHAR(50) NOT NULL REFERENCES loan_application_step(code),
    status VARCHAR(30) NOT NULL DEFAULT 'NOT_STARTED',
    payload JSONB NOT NULL DEFAULT '{}'::jsonb,
    requires_review BOOLEAN NOT NULL DEFAULT FALSE,
    invalidated_by_step_code VARCHAR(50) REFERENCES loan_application_step(code),
    invalidated_at TIMESTAMP,
    invalidated_reason TEXT,
    reviewed_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_loan_application_step_data
        UNIQUE (loan_application_id, step_code),
    CONSTRAINT chk_loan_application_step_data_status
        CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED'))
);

CREATE INDEX IF NOT EXISTS idx_loan_application_step_data_application_id
ON loan_application_step_data(loan_application_id);

CREATE INDEX IF NOT EXISTS idx_loan_application_step_data_step_code
ON loan_application_step_data(step_code);

CREATE INDEX IF NOT EXISTS idx_loan_application_step_data_requires_review
ON loan_application_step_data(loan_application_id, requires_review);

CREATE TABLE IF NOT EXISTS loan_application_step_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    loan_application_id UUID NOT NULL REFERENCES loan_application(id) ON DELETE CASCADE,
    step_code VARCHAR(50) REFERENCES loan_application_step(code),
    action VARCHAR(50) NOT NULL,
    old_status VARCHAR(30),
    new_status VARCHAR(30),
    note TEXT,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    CONSTRAINT chk_loan_application_step_history_action
        CHECK (action IN (
            'CREATE_APPLICATION',
            'START_STEP',
            'SAVE_STEP',
            'COMPLETE_STEP',
            'REOPEN_STEP',
            'INVALIDATE_STEP',
            'CANCEL_APPLICATION',
            'EXPIRE_APPLICATION',
            'COMPLETE_APPLICATION',
            'SUBMIT_APPLICATION'
        ))
);

CREATE INDEX IF NOT EXISTS idx_loan_application_step_history_application_id
ON loan_application_step_history(loan_application_id);

CREATE INDEX IF NOT EXISTS idx_loan_application_step_history_changed_at
ON loan_application_step_history(changed_at);

-- =========================================================
-- 4. Remove obsolete draft tables
-- =========================================================

DROP TABLE IF EXISTS loan_application_draft_history;
DROP TABLE IF EXISTS loan_application_draft_step_data;
DROP TABLE IF EXISTS loan_application_draft;
