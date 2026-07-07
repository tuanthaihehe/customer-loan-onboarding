-- Customer Loan Onboarding - Remove APP_DRAFT Loan Application State
-- PostgreSQL dialect
-- Version: V16 seed
--
-- Scope:
-- - Remove APP_DRAFT from loan_application_state because draft data is now
--   represented by loan_application_draft.
-- - Keep real loan_application lifecycle starting at APP_SUBMITTED.
-- - Clean demo/reference rows that still point to APP_DRAFT before deleting it.
--
-- This file is idempotent and can be run multiple times safely.

DO $$
DECLARE
    draft_state_id UUID;
    submitted_state_id UUID;
BEGIN
    SELECT id
    INTO draft_state_id
    FROM loan_application_state
    WHERE code = 'APP_DRAFT';

    SELECT id
    INTO submitted_state_id
    FROM loan_application_state
    WHERE code = 'APP_SUBMITTED';

    IF draft_state_id IS NULL THEN
        UPDATE loan_application_state
        SET is_initial = FALSE
        WHERE is_initial = TRUE;

        UPDATE loan_application_state
        SET
            is_initial = TRUE,
            sort_order = 1
        WHERE code = 'APP_SUBMITTED';

        RETURN;
    END IF;

    IF submitted_state_id IS NULL THEN
        RAISE EXCEPTION 'Cannot remove APP_DRAFT because APP_SUBMITTED does not exist.';
    END IF;

    UPDATE loan_application
    SET current_state_id = submitted_state_id
    WHERE current_state_id = draft_state_id;

    DELETE FROM loan_application_state_history
    WHERE from_state_id = draft_state_id
      AND to_state_id = submitted_state_id
      AND action_code = 'SUBMIT';

    UPDATE loan_application_state_history
    SET
        to_state_id = submitted_state_id,
        action_code = CASE
            WHEN action_code = 'CREATE' THEN 'CREATE_APPLICATION'
            ELSE action_code
        END,
        note = CASE
            WHEN note IS NULL THEN 'Tạo hồ sơ vay thật từ loan_application_draft.'
            ELSE replace(replace(note, 'nháp', 'thật'), 'Nháp', 'Thật')
        END
    WHERE to_state_id = draft_state_id;

    UPDATE loan_application_state_history
    SET from_state_id = submitted_state_id
    WHERE from_state_id = draft_state_id;

    DELETE FROM loan_application_state_transition
    WHERE from_state_id = draft_state_id
       OR to_state_id = draft_state_id;

    UPDATE loan_application_state
    SET is_initial = FALSE
    WHERE is_initial = TRUE;

    UPDATE loan_application_state
    SET
        is_initial = TRUE,
        sort_order = 1,
        description = 'Hồ sơ vay thật đã được tạo từ loan_application_draft và đi vào luồng xử lý.'
    WHERE id = submitted_state_id;

    UPDATE loan_application_state
    SET sort_order = sort_order - 1
    WHERE sort_order > 1;

    DELETE FROM loan_application_state
    WHERE id = draft_state_id;
END $$;
