-- Customer Loan Onboarding - Add Additional Document Types
-- PostgreSQL dialect
-- Version: V17 seed
--
-- Scope:
-- - Add additional document_type records for customer identity verification
--   and supporting proof documents.
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
    ('INCOME_PROOF', 'Chứng từ chứng minh thu nhập', 'Chứng từ bổ sung dùng để chứng minh thu nhập của khách hàng.', FALSE, TRUE, 140),
    ('OCCUPATION_PROOF_DOCUMENT', 'Chứng từ chứng minh nghề nghiệp', 'Chứng từ bổ sung dùng để chứng minh nghề nghiệp/công việc hiện tại của khách hàng.', FALSE, TRUE, 150),
    ('RESIDENCE_PROOF_DOCUMENT', 'Chứng từ chứng minh nơi cư trú', 'Chứng từ bổ sung dùng để chứng minh nơi cư trú hoặc địa chỉ sinh sống của khách hàng.', FALSE, TRUE, 160),
    ('DEPENDENT_PROOF_DOCUMENT', 'Chứng từ chứng minh người phụ thuộc', 'Chứng từ bổ sung dùng để chứng minh thông tin người phụ thuộc của khách hàng.', FALSE, TRUE, 170),
    ('SIGNED_CUSTOMER_CONTRACT', 'Hợp đồng có chữ ký khách hàng', 'Hợp đồng/hồ sơ có chữ ký của khách hàng.', FALSE, TRUE, 180),
    ('REFERENCE_VERIFICATION_FORM', 'Phiếu xác minh người tham chiếu', 'Phiếu xác minh thông tin người tham chiếu.', FALSE, TRUE, 190)
ON CONFLICT (code) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    is_required = EXCLUDED.is_required,
    is_active = EXCLUDED.is_active,
    sort_order = EXCLUDED.sort_order;

DELETE FROM document_type dt
WHERE dt.code = 'SIGNED_LABOR_CONTRACT_IMAGE'
  AND NOT EXISTS (
      SELECT 1
      FROM loan_application_document lad
      WHERE lad.document_type_id = dt.id
  );

UPDATE document_type
SET
    name = 'Ảnh hợp đồng lao động có chữ ký khách hàng',
    description = 'Không còn dùng. Thay bằng INCOME_PROOF.',
    is_required = FALSE,
    is_active = FALSE,
    sort_order = 998
WHERE code = 'SIGNED_LABOR_CONTRACT_IMAGE';
