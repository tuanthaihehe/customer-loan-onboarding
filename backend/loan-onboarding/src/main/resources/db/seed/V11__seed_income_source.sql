-- Customer Loan Onboarding - Seed Income Source
-- PostgreSQL dialect
-- Version: V11 seed
--
-- Scope:
-- - Seed income_source catalog.
--
-- This file is idempotent and can be run multiple times safely.
-- It must be run after V19__update_loan_application_product_and_income_source.sql.

INSERT INTO income_source (
    code,
    name,
    description,
    is_active,
    sort_order
)
VALUES
    ('SALARY', 'Lương', 'Thu nhập từ lương cố định, hợp đồng lao động hoặc bảng lương hàng tháng.', TRUE, 10),
    ('BUSINESS', 'Kinh doanh', 'Thu nhập từ hoạt động kinh doanh, cửa hàng, hộ kinh doanh hoặc doanh nghiệp nhỏ.', TRUE, 20),
    ('SELF_EMPLOYED', 'Tự kinh doanh / làm tự do', 'Thu nhập từ công việc tự do, làm dịch vụ cá nhân hoặc tự tạo việc làm.', TRUE, 30),
    ('COMMISSION', 'Hoa hồng / doanh số', 'Thu nhập từ hoa hồng, doanh số bán hàng hoặc thưởng theo hiệu quả.', TRUE, 40),
    ('DRIVER_INCOME', 'Thu nhập tài xế', 'Thu nhập từ chạy xe công nghệ, taxi, giao hàng hoặc vận tải.', TRUE, 50),
    ('RENTAL', 'Cho thuê tài sản', 'Thu nhập từ cho thuê nhà, phòng trọ, xe hoặc tài sản khác.', TRUE, 60),
    ('FAMILY_SUPPORT', 'Hỗ trợ từ gia đình', 'Nguồn tiền hỗ trợ thường xuyên từ người thân hoặc gia đình.', TRUE, 70),
    ('PENSION', 'Lương hưu', 'Thu nhập từ lương hưu hoặc trợ cấp hưu trí.', TRUE, 80),
    ('AGRICULTURE', 'Nông nghiệp', 'Thu nhập từ trồng trọt, chăn nuôi, nuôi trồng thủy sản hoặc sản xuất nông nghiệp.', TRUE, 90),
    ('OTHER', 'Khác', 'Nguồn thu nhập khác không thuộc các nhóm đã định nghĩa.', TRUE, 999)
ON CONFLICT (code) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    is_active = EXCLUDED.is_active,
    sort_order = EXCLUDED.sort_order;
