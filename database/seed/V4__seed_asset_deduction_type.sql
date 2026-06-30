-- Customer Loan Onboarding - Seed Asset Deduction Types
-- PostgreSQL dialect
-- Version: V4 seed
--
-- Scope:
-- - Seed master data for asset deduction factors.
--
-- This file is idempotent. It can be run multiple times safely.

INSERT INTO asset_deduction_type (
    code,
    name,
    description,
    is_active,
    sort_order
)
VALUES
    (
        'OLD_VEHICLE',
        'Xe cũ',
        'Giảm trừ do xe đã cũ hoặc đời xe thấp hơn mặt bằng định giá.',
        TRUE,
        10
    ),
    (
        'PHYSICAL_DAMAGE',
        'Hư hỏng ngoại thất',
        'Giảm trừ do trầy xước, móp méo, va chạm hoặc hư hỏng bên ngoài.',
        TRUE,
        20
    ),
    (
        'ENGINE_ISSUE',
        'Lỗi động cơ',
        'Giảm trừ do động cơ yếu, đã sửa chữa lớn hoặc có dấu hiệu hư hỏng.',
        TRUE,
        30
    ),
    (
        'MISSING_DOCUMENT',
        'Thiếu giấy tờ',
        'Giảm trừ do thiếu đăng ký, đăng kiểm hoặc giấy tờ liên quan.',
        TRUE,
        40
    ),
    (
        'HIGH_MILEAGE',
        'Số km cao',
        'Giảm trừ do xe đã sử dụng nhiều, số km cao hơn mức thông thường.',
        TRUE,
        50
    ),
    (
        'LOW_LIQUIDITY_COLOR',
        'Màu khó thanh khoản',
        'Giảm trừ do màu xe khó bán lại hoặc ít được thị trường ưa chuộng.',
        TRUE,
        60
    ),
    (
        'MODIFIED_VEHICLE',
        'Xe đã độ/chỉnh sửa',
        'Giảm trừ do xe đã thay đổi kết cấu, độ máy, độ ngoại hình hoặc thay đổi phụ kiện quan trọng.',
        TRUE,
        70
    ),
    (
        'OTHER',
        'Khác',
        'Yếu tố giảm trừ khác không thuộc các nhóm trên.',
        TRUE,
        999
    )
ON CONFLICT (code) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    is_active = EXCLUDED.is_active,
    sort_order = EXCLUDED.sort_order;
