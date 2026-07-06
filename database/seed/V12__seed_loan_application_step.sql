-- Customer Loan Onboarding - Seed Loan Application Draft Steps
-- PostgreSQL dialect
-- Version: V12 seed
--
-- Scope:
-- - Seed the ordered step catalog used by loan_application_draft_step_data.
--
-- This file is idempotent and can be run multiple times safely.
-- It must be run after V21__add_loan_application_draft_step_flow.sql.

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
        'Ghi nhận nhu cầu vay, thông tin khoản vay sơ bộ và các lựa chọn ban đầu.',
        TRUE
    ),
    (
        'CUSTOMER_DETAIL',
        'Chi tiết khách hàng',
        3,
        'Bổ sung thông tin cá nhân, nghề nghiệp, thu nhập, địa chỉ và thông tin giải ngân.',
        TRUE
    ),
    (
        'ASSET_DETAIL',
        'Chi tiết tài sản',
        4,
        'Ghi nhận thông tin tài sản, giấy đăng ký, định giá và các yếu tố giảm trừ.',
        TRUE
    ),
    (
        'FINAL_LOAN_PROPOSAL',
        'Đề xuất gói vay cuối cùng',
        5,
        'Chốt sản phẩm vay, số tiền, kỳ hạn, lãi suất và đề xuất cuối cùng trước khi hoàn tất.',
        TRUE
    ),
    (
        'UPLOAD_COMPLETE',
        'Upload hồ sơ và hoàn tất',
        6,
        'Upload chứng từ, kiểm tra dữ liệu toàn bộ draft và đánh dấu hồ sơ sẵn sàng convert.',
        TRUE
    )
ON CONFLICT (code) DO UPDATE
SET
    name = EXCLUDED.name,
    step_order = EXCLUDED.step_order,
    description = EXCLUDED.description,
    is_active = EXCLUDED.is_active,
    updated_at = CURRENT_TIMESTAMP;
