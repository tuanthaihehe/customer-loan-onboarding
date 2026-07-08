-- Customer Loan Onboarding - Add upload screen document types

INSERT INTO document_type (
    code,
    name,
    description,
    is_required,
    is_active,
    sort_order
)
VALUES
    ('CUSTOMER_SIGNED_CONTRACT', 'Hợp đồng có chữ ký khách hàng', 'File hợp đồng/hồ sơ có chữ ký của khách hàng.', FALSE, TRUE, 180),
    ('REFERENCE_VERIFICATION_FORM', 'Phiếu xác minh người tham chiếu', 'Phiếu hoặc file xác minh thông tin người tham chiếu.', FALSE, TRUE, 190)
ON CONFLICT (code) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    is_required = EXCLUDED.is_required,
    is_active = EXCLUDED.is_active,
    sort_order = EXCLUDED.sort_order;
