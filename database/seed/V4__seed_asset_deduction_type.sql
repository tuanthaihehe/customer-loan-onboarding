INSERT INTO asset_deduction_type (
    code,
    name,
    description,
    deduction_amount,
    is_active,
    sort_order
)
VALUES
    (
        'OLD_VEHICLE',
        'Xe cũ',
        'Giảm trừ do xe đã cũ hoặc đời xe thấp hơn mặt bằng định giá.',
        3000000,
        TRUE,
        10
    ),
    (
        'PHYSICAL_DAMAGE',
        'Hư hỏng ngoại thất',
        'Giảm trừ do trầy xước, móp méo, va chạm hoặc hư hỏng bên ngoài.',
        5000000,
        TRUE,
        20
    ),
    (
        'ENGINE_ISSUE',
        'Lỗi động cơ',
        'Giảm trừ do động cơ yếu, đã sửa chữa lớn hoặc có dấu hiệu hư hỏng.',
        8000000,
        TRUE,
        30
    ),
    (
        'HIGH_MILEAGE',
        'Số km cao',
        'Giảm trừ do xe đã sử dụng nhiều, số km cao hơn mức thông thường.',
        3000000,
        TRUE,
        50
    ),
    (
        'LOW_LIQUIDITY_COLOR',
        'Màu khó thanh khoản',
        'Giảm trừ do màu xe khó bán lại hoặc ít được thị trường ưa chuộng.',
        1000000,
        TRUE,
        60
    ),
    (
        'MODIFIED_VEHICLE',
        'Xe đã độ/chỉnh sửa',
        'Giảm trừ do xe đã thay đổi kết cấu, độ máy, độ ngoại hình hoặc phụ kiện quan trọng.',
        4000000,
        TRUE,
        70
    ),
    (
        'OTHER',
        'Khác',
        'Yếu tố giảm trừ khác không thuộc các nhóm trên.',
        0,
        TRUE,
        999
    )
ON CONFLICT (code) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    deduction_amount = EXCLUDED.deduction_amount,
    is_active = EXCLUDED.is_active,
    sort_order = EXCLUDED.sort_order;