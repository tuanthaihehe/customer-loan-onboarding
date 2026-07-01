-- Customer Loan Onboarding - Seed Document Types
-- PostgreSQL dialect
-- Version: V10 seed
--
-- Scope:
-- - Seed document_type catalog for loan application documents/images.
--
-- This file is idempotent and can be run multiple times safely.
-- It must be run after V17__add_loan_application_document.sql.

INSERT INTO document_type (
    code,
    name,
    description,
    is_required,
    is_active,
    sort_order
)
VALUES
    ('CITIZEN_ID_FRONT', 'CCCD mặt trước', 'Ảnh mặt trước của căn cước công dân/chứng minh nhân dân của người vay.', TRUE, TRUE, 10),
    ('CITIZEN_ID_BACK', 'CCCD mặt sau', 'Ảnh mặt sau của căn cước công dân/chứng minh nhân dân của người vay.', TRUE, TRUE, 20),

    ('VEHICLE_REGISTRATION_FRONT', 'Cà vẹt xe mặt trước', 'Ảnh mặt trước của giấy đăng ký xe/cà vẹt xe.', TRUE, TRUE, 30),
    ('VEHICLE_REGISTRATION_BACK', 'Cà vẹt xe mặt sau', 'Ảnh mặt sau của giấy đăng ký xe/cà vẹt xe.', TRUE, TRUE, 40),

    ('ASSET_FRONT_IMAGE', 'Ảnh tài sản góc trước', 'Ảnh chụp tài sản/xe từ góc phía trước.', TRUE, TRUE, 50),
    ('ASSET_BACK_IMAGE', 'Ảnh tài sản góc sau', 'Ảnh chụp tài sản/xe từ góc phía sau.', TRUE, TRUE, 60),
    ('ASSET_LEFT_IMAGE', 'Ảnh tài sản góc trái', 'Ảnh chụp tài sản/xe từ phía bên trái.', TRUE, TRUE, 70),
    ('ASSET_RIGHT_IMAGE', 'Ảnh tài sản góc phải', 'Ảnh chụp tài sản/xe từ phía bên phải.', TRUE, TRUE, 80),
    ('ASSET_FRAME_NUMBER_IMAGE', 'Ảnh số khung', 'Ảnh chụp số khung của xe/tài sản.', TRUE, TRUE, 90),
    ('ASSET_ENGINE_NUMBER_IMAGE', 'Ảnh số máy', 'Ảnh chụp số máy của xe/tài sản.', TRUE, TRUE, 100),
    ('ASSET_ODOMETER_IMAGE', 'Ảnh đồng hồ xe', 'Ảnh chụp đồng hồ/odo của xe để ghi nhận số km hoặc tình trạng hiển thị.', TRUE, TRUE, 110),

    ('BORROWER_PORTRAIT_IMAGE', 'Ảnh chân dung người vay', 'Ảnh chân dung của người vay.', TRUE, TRUE, 120),

    ('INCOME_PROOF', 'Chứng minh thu nhập', 'Ảnh hoặc file chứng minh thu nhập của người vay.', FALSE, TRUE, 130),

    ('OTHER_DOCUMENT', 'Chứng từ khác', 'Các chứng từ bổ sung khác không thuộc các loại đã định nghĩa.', FALSE, TRUE, 999)
ON CONFLICT (code) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    is_required = EXCLUDED.is_required,
    is_active = EXCLUDED.is_active,
    sort_order = EXCLUDED.sort_order;
