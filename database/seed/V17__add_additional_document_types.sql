-- Customer Loan Onboarding - Add Additional Document Types
-- PostgreSQL dialect
-- Version: V17 seed
--
-- Scope:
-- - Add additional document_type records for customer identity verification
--   and income proof documents.
--
-- This file is idempotent and can be run multiple times safely.
-- It must be run after V10__seed_document_type.sql.

INSERT INTO document_type (
    code,
    name,
    description,
    is_required,
    is_active,
    sort_order
)
VALUES
    ('BORROWER_HOLDING_CITIZEN_ID_IMAGE', 'Ảnh khách hàng cầm CCCD', 'Ảnh chân dung khách hàng cầm căn cước công dân/chứng minh nhân dân.', TRUE, TRUE, 25),
    ('BORROWER_PORTRAIT_VIDEO', 'Video chân dung người vay', 'Video chân dung của người vay dùng để xác thực khách hàng.', TRUE, TRUE, 125),
    ('SIGNED_LABOR_CONTRACT_IMAGE', 'Ảnh hợp đồng lao động có chữ ký khách hàng', 'Ảnh hợp đồng lao động có chữ ký của khách hàng dùng để bổ sung hồ sơ chứng minh thu nhập.', FALSE, TRUE, 140)
ON CONFLICT (code) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    is_required = EXCLUDED.is_required,
    is_active = EXCLUDED.is_active,
    sort_order = EXCLUDED.sort_order;
