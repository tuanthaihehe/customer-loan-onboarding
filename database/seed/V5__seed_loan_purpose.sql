-- Customer Loan Onboarding - Seed Loan Purpose
-- PostgreSQL dialect
-- Version: V5 seed
--
-- Scope:
-- - Seed master data for loan purpose dropdown.
--
-- This file is idempotent. It can be run multiple times safely.
-- It must be run after V8__add_loan_purpose_catalog.sql.

INSERT INTO loan_purpose (
    code,
    name,
    description,
    is_active,
    sort_order
)
VALUES
    (
        'BUSINESS',
        'Kinh doanh',
        'Vay phục vụ hoạt động kinh doanh.',
        TRUE,
        10
    ),
    (
        'PERSONAL_CONSUMPTION',
        'Tiêu dùng cá nhân',
        'Vay phục vụ nhu cầu tiêu dùng cá nhân.',
        TRUE,
        20
    ),
    (
        'VEHICLE_REPAIR',
        'Sửa chữa xe',
        'Vay phục vụ sửa chữa, bảo dưỡng phương tiện.',
        TRUE,
        30
    ),
    (
        'MEDICAL',
        'Y tế',
        'Vay phục vụ chi phí khám chữa bệnh hoặc nhu cầu y tế.',
        TRUE,
        40
    ),
    (
        'EDUCATION',
        'Giáo dục',
        'Vay phục vụ chi phí học tập hoặc giáo dục.',
        TRUE,
        50
    ),
    (
        'HOME_REPAIR',
        'Sửa chữa nhà',
        'Vay phục vụ sửa chữa hoặc cải tạo nhà cửa.',
        TRUE,
        60
    ),
    (
        'DEBT_REPAYMENT',
        'Thanh toán nợ',
        'Vay để thanh toán khoản nợ khác.',
        TRUE,
        70
    ),
    (
        'OTHER',
        'Khác',
        'Mục đích vay khác.',
        TRUE,
        999
    )
ON CONFLICT (code) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    is_active = EXCLUDED.is_active,
    sort_order = EXCLUDED.sort_order;
