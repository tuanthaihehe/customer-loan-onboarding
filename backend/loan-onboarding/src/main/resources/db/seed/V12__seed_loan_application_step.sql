-- Customer Loan Onboarding - Seed Loan Application Draft Steps
-- PostgreSQL dialect
-- Version: V12 seed
--
-- Scope:
-- - Reset and seed the ordered step catalog used by loan_application_draft_step_data.
-- - Current draft flow has 4 steps:
--   1. Customer identification
--   2. Preliminary information
--   3. Customer detail + asset detail + selected loan proposal
--   4. Documents/upload completion
--
-- This file is idempotent for dev/demo data.
-- It must be run after V21__add_loan_application_draft_step_flow.sql.

TRUNCATE TABLE
    loan_application_draft_history,
    loan_application_draft_step_data,
    loan_application_draft
RESTART IDENTITY CASCADE;

DELETE FROM loan_application_step;

INSERT INTO loan_application_step (
    code,
    name,
    step_order,
    description,
    is_active
)
VALUES
    (
        'CUSTOMER_IDENTIFY',
        'Định danh khách hàng',
        1,
        'Tra cứu hoặc tạo khách hàng và xác định khách hàng đứng tên hồ sơ vay.',
        TRUE
    ),
    (
        'PRELIMINARY_INFO',
        'Thông tin sơ bộ',
        2,
        'Ghi nhận thông tin khách hàng sơ bộ, tài sản sơ bộ, giảm trừ sơ bộ và nhu cầu vay ban đầu.',
        TRUE
    ),
    (
        'CUSTOMER_ASSET_LOAN_PROPOSAL',
        'Thông tin khách hàng, tài sản và gói vay',
        3,
        'Bổ sung thông tin khách hàng chi tiết, thông tin tài sản chi tiết, xác nhận giảm trừ và chọn gói vay.',
        TRUE
    ),
    (
        'UPLOAD_COMPLETE',
        'Chứng từ và hoàn tất',
        4,
        'Upload chứng từ, kiểm tra dữ liệu toàn bộ draft và đánh dấu hồ sơ sẵn sàng convert.',
        TRUE
    );
