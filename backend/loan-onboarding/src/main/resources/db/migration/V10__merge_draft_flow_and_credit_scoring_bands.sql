-- Customer Loan Onboarding - Merge Draft Flow and Credit Scoring Bands
-- PostgreSQL dialect
-- Version: V10
--
-- Sources:
-- - database/seed/V12__seed_loan_application_step.sql
-- - database/seed/V13__seed_loan_application_draft_flow.sql
-- - database/seed/V14__seed_loan_application_draft_review_flow.sql
-- - database/migrations/V25__create_credit_scoring_bands.sql
-- - database/seed/V15_seed_credit_scoring_bands.sql
--
-- Notes:
-- - V13 seed is intentionally a no-op in the database folder because the old
--   6-step draft flow was superseded.
-- - The current draft flow has 4 steps:
--   CUSTOMER_IDENTIFY -> PRELIMINARY_INFO -> CUSTOMER_ASSET_LOAN_PROPOSAL -> UPLOAD_COMPLETE.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =========================================================
-- 1. Current loan application draft step flow
-- =========================================================

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

-- V13 draft flow seed is intentionally superseded by the merged 4-step V14 seed.

INSERT INTO loan_application_draft (
    id,
    draft_code,
    customer_id,
    current_step_code,
    status,
    converted_loan_application_id,
    expired_at,
    created_at,
    updated_at
)
VALUES
    (
        '70000000-0000-0000-0000-000000000001',
        'DRF-MERGED-000001',
        '10000000-0000-0000-0000-000000000001',
        'PRELIMINARY_INFO',
        'DRAFT',
        NULL,
        TIMESTAMP '2026-08-31 23:59:59',
        TIMESTAMP '2026-07-06 09:00:00',
        TIMESTAMP '2026-07-06 09:12:00'
    ),
    (
        '70000000-0000-0000-0000-000000000002',
        'DRF-MERGED-000002',
        '10000000-0000-0000-0000-000000000002',
        'CUSTOMER_ASSET_LOAN_PROPOSAL',
        'DRAFT',
        NULL,
        TIMESTAMP '2026-08-31 23:59:59',
        TIMESTAMP '2026-07-06 10:00:00',
        TIMESTAMP '2026-07-06 10:35:00'
    ),
    (
        '70000000-0000-0000-0000-000000000003',
        'DRF-MERGED-000003',
        '10000000-0000-0000-0000-000000000003',
        'CUSTOMER_ASSET_LOAN_PROPOSAL',
        'DRAFT',
        NULL,
        TIMESTAMP '2026-08-31 23:59:59',
        TIMESTAMP '2026-07-06 11:00:00',
        TIMESTAMP '2026-07-06 11:55:00'
    ),
    (
        '70000000-0000-0000-0000-000000000004',
        'DRF-MERGED-000004',
        '10000000-0000-0000-0000-000000000005',
        'UPLOAD_COMPLETE',
        'CONVERTED',
        '30000000-0000-0000-0000-000000000001',
        TIMESTAMP '2026-08-31 23:59:59',
        TIMESTAMP '2026-07-06 13:00:00',
        TIMESTAMP '2026-07-06 13:55:00'
    );

INSERT INTO loan_application_draft_step_data (
    id,
    draft_id,
    step_code,
    status,
    payload,
    requires_review,
    invalidated_by_step_code,
    invalidated_at,
    reviewed_at,
    completed_at,
    invalidated_reason,
    created_at,
    updated_at
)
VALUES
    (
        '71000000-0000-0000-0000-000000000001',
        '70000000-0000-0000-0000-000000000001',
        'CUSTOMER_IDENTIFY',
        'COMPLETED',
        '{"initial_identification": {"full_name": "Nguyen Van An", "phone_number": "0912345678", "identity_number": "001203000001", "matched_existing_customer": true}}'::jsonb,
        FALSE,
        NULL,
        NULL,
        NULL,
        TIMESTAMP '2026-07-06 09:05:00',
        NULL,
        TIMESTAMP '2026-07-06 09:00:00',
        TIMESTAMP '2026-07-06 09:05:00'
    ),
    (
        '71000000-0000-0000-0000-000000000002',
        '70000000-0000-0000-0000-000000000001',
        'PRELIMINARY_INFO',
        'IN_PROGRESS',
        '{"preliminary_customer": {"full_name": "Nguyen Van An", "phone_number": "0912345678", "identity_number": "001203000001"}, "preliminary_collateral": {"collateral_type": "MOTORBIKE", "brand": "Honda", "model": "SH", "estimated_value": 85000000}, "preliminary_deductions": [{"code": "SCRATCH", "name": "Trầy xước", "amount": 500000}], "total_preliminary_deduction_amount": 500000, "preliminary_loan_package": {"requested_amount": 50000000, "loan_term_months": 12}}'::jsonb,
        FALSE,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        TIMESTAMP '2026-07-06 09:06:00',
        TIMESTAMP '2026-07-06 09:12:00'
    ),
    (
        '71000000-0000-0000-0000-000000000003',
        '70000000-0000-0000-0000-000000000001',
        'CUSTOMER_ASSET_LOAN_PROPOSAL',
        'NOT_STARTED',
        '{}'::jsonb,
        FALSE,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        TIMESTAMP '2026-07-06 09:00:00',
        TIMESTAMP '2026-07-06 09:00:00'
    ),
    (
        '71000000-0000-0000-0000-000000000004',
        '70000000-0000-0000-0000-000000000001',
        'UPLOAD_COMPLETE',
        'NOT_STARTED',
        '{}'::jsonb,
        FALSE,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        TIMESTAMP '2026-07-06 09:00:00',
        TIMESTAMP '2026-07-06 09:00:00'
    ),
    (
        '71000000-0000-0000-0000-000000000005',
        '70000000-0000-0000-0000-000000000002',
        'CUSTOMER_IDENTIFY',
        'COMPLETED',
        '{"initial_identification": {"full_name": "Tran Thi Binh", "phone_number": "0987654321", "identity_number": "001203000002", "matched_existing_customer": true}}'::jsonb,
        FALSE,
        NULL,
        NULL,
        NULL,
        TIMESTAMP '2026-07-06 10:05:00',
        NULL,
        TIMESTAMP '2026-07-06 10:00:00',
        TIMESTAMP '2026-07-06 10:05:00'
    ),
    (
        '71000000-0000-0000-0000-000000000006',
        '70000000-0000-0000-0000-000000000002',
        'PRELIMINARY_INFO',
        'COMPLETED',
        '{"preliminary_customer": {"full_name": "Tran Thi Binh", "phone_number": "0987654321", "identity_number": "001203000002"}, "preliminary_collateral": {"collateral_type": "MOTORBIKE", "brand": "Honda", "model": "Vision", "estimated_value": 45000000}, "preliminary_deductions": [{"code": "SCRATCH", "name": "Trầy xước", "amount": 300000}, {"code": "MISSING_DOCUMENT", "name": "Thiếu giấy tờ", "amount": 700000}], "total_preliminary_deduction_amount": 1000000, "preliminary_loan_package": {"requested_amount": 25000000, "loan_term_months": 12}}'::jsonb,
        FALSE,
        NULL,
        NULL,
        NULL,
        TIMESTAMP '2026-07-06 10:12:00',
        NULL,
        TIMESTAMP '2026-07-06 10:06:00',
        TIMESTAMP '2026-07-06 10:12:00'
    ),
    (
        '71000000-0000-0000-0000-000000000007',
        '70000000-0000-0000-0000-000000000002',
        'CUSTOMER_ASSET_LOAN_PROPOSAL',
        'IN_PROGRESS',
        '{"customer_detail": {"full_name": "Tran Thi Binh", "phone_number": "0987654321", "identity_number": "001203000002", "date_of_birth": "1995-05-20", "address": "Ha Noi", "employment_type": "SALARIED", "monthly_income": 15000000}, "reference_persons": [{"full_name": "Tran Van C", "phone_number": "0900000001", "relationship": "FATHER"}], "collateral_detail": {"collateral_type": "MOTORBIKE", "brand": "Honda", "model": "Vision", "license_plate": "29A1-12345", "frame_number": "RLHJF123456789001", "engine_number": "JF45E1234567", "manufacture_year": 2022}, "confirmed_deductions": [{"code": "SCRATCH", "name": "Trầy xước", "amount": 300000}, {"code": "MISSING_DOCUMENT", "name": "Thiếu giấy tờ", "amount": 700000}], "confirmed_total_deduction_amount": 1000000, "selected_loan_offer": {"loan_product_code": "XM_PREFER", "product_name": "Xe máy ưu đãi", "final_requested_amount": 25000000, "final_loan_term_months": 12, "interest_rate": 1.5, "estimated_monthly_payment": 2350000}, "payment_info": {"payment_method": "BANK_TRANSFER", "payment_day": 15}}'::jsonb,
        FALSE,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        TIMESTAMP '2026-07-06 10:13:00',
        TIMESTAMP '2026-07-06 10:35:00'
    ),
    (
        '71000000-0000-0000-0000-000000000008',
        '70000000-0000-0000-0000-000000000002',
        'UPLOAD_COMPLETE',
        'NOT_STARTED',
        '{}'::jsonb,
        FALSE,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        TIMESTAMP '2026-07-06 10:00:00',
        TIMESTAMP '2026-07-06 10:00:00'
    ),
    (
        '71000000-0000-0000-0000-000000000009',
        '70000000-0000-0000-0000-000000000003',
        'CUSTOMER_IDENTIFY',
        'COMPLETED',
        '{"initial_identification": {"full_name": "Pham Van Cuong", "phone_number": "0977000001", "identity_number": "001203000003", "matched_existing_customer": true}}'::jsonb,
        FALSE,
        NULL,
        NULL,
        NULL,
        TIMESTAMP '2026-07-06 11:05:00',
        NULL,
        TIMESTAMP '2026-07-06 11:00:00',
        TIMESTAMP '2026-07-06 11:05:00'
    ),
    (
        '71000000-0000-0000-0000-000000000010',
        '70000000-0000-0000-0000-000000000003',
        'PRELIMINARY_INFO',
        'COMPLETED',
        '{"preliminary_customer": {"full_name": "Pham Van Cuong", "phone_number": "0977000001", "identity_number": "001203000003"}, "preliminary_collateral": {"collateral_type": "MOTORBIKE", "brand": "Yamaha", "model": "Exciter", "estimated_value": 50000000}, "preliminary_deductions": [{"code": "SCRATCH", "name": "Trầy xước", "amount": 800000}], "total_preliminary_deduction_amount": 800000, "preliminary_loan_package": {"requested_amount": 30000000, "loan_term_months": 12}}'::jsonb,
        FALSE,
        NULL,
        NULL,
        NULL,
        TIMESTAMP '2026-07-06 11:44:00',
        NULL,
        TIMESTAMP '2026-07-06 11:06:00',
        TIMESTAMP '2026-07-06 11:44:00'
    ),
    (
        '71000000-0000-0000-0000-000000000011',
        '70000000-0000-0000-0000-000000000003',
        'CUSTOMER_ASSET_LOAN_PROPOSAL',
        'COMPLETED',
        '{"customer_detail": {"full_name": "Pham Van Cuong", "phone_number": "0977000001", "identity_number": "001203000003", "date_of_birth": "1991-09-12", "address": "Da Nang", "employment_type": "SELF_EMPLOYED", "monthly_income": 18000000}, "collateral_detail": {"collateral_type": "MOTORBIKE", "brand": "Yamaha", "model": "Exciter", "license_plate": "30B1-88888", "manufacture_year": 2021}, "confirmed_deductions": [{"code": "SCRATCH", "name": "Trầy xước", "amount": 500000}, {"code": "MISSING_DOCUMENT", "name": "Thiếu giấy tờ", "amount": 1000000}], "confirmed_total_deduction_amount": 1500000, "selected_loan_offer": {"loan_product_code": "XM_HIGH_LIMIT", "product_name": "Xe máy hạn mức cao", "final_requested_amount": 30000000, "final_loan_term_months": 12, "interest_rate": 1.6}}'::jsonb,
        TRUE,
        'PRELIMINARY_INFO',
        TIMESTAMP '2026-07-06 11:45:00',
        NULL,
        TIMESTAMP '2026-07-06 11:38:00',
        'Preliminary deduction list changed, merged customer/asset/loan proposal step requires review.',
        TIMESTAMP '2026-07-06 11:13:00',
        TIMESTAMP '2026-07-06 11:45:00'
    ),
    (
        '71000000-0000-0000-0000-000000000012',
        '70000000-0000-0000-0000-000000000003',
        'UPLOAD_COMPLETE',
        'NOT_STARTED',
        '{}'::jsonb,
        FALSE,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        TIMESTAMP '2026-07-06 11:00:00',
        TIMESTAMP '2026-07-06 11:00:00'
    ),
    (
        '71000000-0000-0000-0000-000000000013',
        '70000000-0000-0000-0000-000000000004',
        'CUSTOMER_IDENTIFY',
        'COMPLETED',
        '{"initial_identification": {"full_name": "Hoang Duc Huy", "phone_number": "0955000005", "identity_number": "001203000005", "matched_existing_customer": true}}'::jsonb,
        FALSE,
        NULL,
        NULL,
        NULL,
        TIMESTAMP '2026-07-06 13:05:00',
        NULL,
        TIMESTAMP '2026-07-06 13:00:00',
        TIMESTAMP '2026-07-06 13:05:00'
    ),
    (
        '71000000-0000-0000-0000-000000000014',
        '70000000-0000-0000-0000-000000000004',
        'PRELIMINARY_INFO',
        'COMPLETED',
        '{"preliminary_customer": {"full_name": "Hoang Duc Huy", "phone_number": "0955000005", "identity_number": "001203000005"}, "preliminary_collateral": {"collateral_type": "MOTORBIKE", "brand": "Honda", "model": "SH", "estimated_value": 90000000}, "preliminary_deductions": [{"code": "SCRATCH", "name": "Trầy xước", "amount": 400000}], "total_preliminary_deduction_amount": 400000, "preliminary_loan_package": {"requested_amount": 45000000, "loan_term_months": 12}}'::jsonb,
        FALSE,
        NULL,
        NULL,
        NULL,
        TIMESTAMP '2026-07-06 13:12:00',
        NULL,
        TIMESTAMP '2026-07-06 13:06:00',
        TIMESTAMP '2026-07-06 13:12:00'
    ),
    (
        '71000000-0000-0000-0000-000000000015',
        '70000000-0000-0000-0000-000000000004',
        'CUSTOMER_ASSET_LOAN_PROPOSAL',
        'COMPLETED',
        '{"customer_detail": {"full_name": "Hoang Duc Huy", "phone_number": "0955000005", "identity_number": "001203000005", "date_of_birth": "1990-10-25", "address": "Ha Noi", "employment_type": "SALARIED", "monthly_income": 25000000}, "collateral_detail": {"collateral_type": "MOTORBIKE", "brand": "Honda", "model": "SH", "license_plate": "29H1-55555", "manufacture_year": 2023}, "confirmed_deductions": [{"code": "SCRATCH", "name": "Trầy xước", "amount": 400000}], "selected_loan_offer": {"loan_product_code": "XM_HIGH_LIMIT", "product_name": "Xe máy hạn mức cao", "final_requested_amount": 40000000, "final_loan_term_months": 12, "interest_rate": 1.5}}'::jsonb,
        FALSE,
        NULL,
        NULL,
        NULL,
        TIMESTAMP '2026-07-06 13:40:00',
        NULL,
        TIMESTAMP '2026-07-06 13:13:00',
        TIMESTAMP '2026-07-06 13:40:00'
    ),
    (
        '71000000-0000-0000-0000-000000000016',
        '70000000-0000-0000-0000-000000000004',
        'UPLOAD_COMPLETE',
        'COMPLETED',
        '{"required_documents": [{"document_type": "IDENTITY_CARD", "required": true, "status": "UPLOADED"}, {"document_type": "VEHICLE_REGISTRATION", "required": true, "status": "UPLOADED"}], "uploaded_documents": [{"document_type": "IDENTITY_CARD", "file_name": "huy_identity.jpg", "status": "VALIDATED"}, {"document_type": "VEHICLE_REGISTRATION", "file_name": "huy_vehicle_registration.jpg", "status": "VALIDATED"}]}'::jsonb,
        FALSE,
        NULL,
        NULL,
        NULL,
        TIMESTAMP '2026-07-06 13:50:00',
        NULL,
        TIMESTAMP '2026-07-06 13:41:00',
        TIMESTAMP '2026-07-06 13:50:00'
    );

INSERT INTO loan_application_draft_history (
    id,
    draft_id,
    step_code,
    action,
    old_status,
    new_status,
    note,
    changed_at,
    metadata
)
VALUES
    ('72000000-0000-0000-0000-000000000001', '70000000-0000-0000-0000-000000000001', NULL, 'CREATE_DRAFT', NULL, 'DRAFT', 'Tạo draft mới.', TIMESTAMP '2026-07-06 09:00:00', '{}'::jsonb),
    ('72000000-0000-0000-0000-000000000002', '70000000-0000-0000-0000-000000000001', 'CUSTOMER_IDENTIFY', 'COMPLETE_STEP', 'IN_PROGRESS', 'COMPLETED', 'Hoàn tất định danh.', TIMESTAMP '2026-07-06 09:05:00', '{}'::jsonb),
    ('72000000-0000-0000-0000-000000000003', '70000000-0000-0000-0000-000000000001', 'PRELIMINARY_INFO', 'SAVE_STEP', 'NOT_STARTED', 'IN_PROGRESS', 'Lưu dở thông tin sơ bộ.', TIMESTAMP '2026-07-06 09:12:00', '{}'::jsonb),
    ('72000000-0000-0000-0000-000000000004', '70000000-0000-0000-0000-000000000002', NULL, 'CREATE_DRAFT', NULL, 'DRAFT', 'Tạo draft đang nhập step gộp.', TIMESTAMP '2026-07-06 10:00:00', '{}'::jsonb),
    ('72000000-0000-0000-0000-000000000005', '70000000-0000-0000-0000-000000000002', 'PRELIMINARY_INFO', 'COMPLETE_STEP', 'IN_PROGRESS', 'COMPLETED', 'Hoàn tất thông tin sơ bộ.', TIMESTAMP '2026-07-06 10:12:00', '{}'::jsonb),
    ('72000000-0000-0000-0000-000000000006', '70000000-0000-0000-0000-000000000002', 'CUSTOMER_ASSET_LOAN_PROPOSAL', 'SAVE_STEP', 'NOT_STARTED', 'IN_PROGRESS', 'Lưu dở step thông tin khách hàng, tài sản và gói vay.', TIMESTAMP '2026-07-06 10:35:00', '{}'::jsonb),
    ('72000000-0000-0000-0000-000000000007', '70000000-0000-0000-0000-000000000003', NULL, 'CREATE_DRAFT', NULL, 'DRAFT', 'Tạo draft review step gộp.', TIMESTAMP '2026-07-06 11:00:00', '{}'::jsonb),
    ('72000000-0000-0000-0000-000000000008', '70000000-0000-0000-0000-000000000003', 'PRELIMINARY_INFO', 'REOPEN_STEP', 'COMPLETED', 'IN_PROGRESS', 'User quay lại sửa giảm trừ sơ bộ.', TIMESTAMP '2026-07-06 11:42:00', '{"changed_step_code": "PRELIMINARY_INFO"}'::jsonb),
    ('72000000-0000-0000-0000-000000000009', '70000000-0000-0000-0000-000000000003', 'PRELIMINARY_INFO', 'COMPLETE_STEP', 'IN_PROGRESS', 'COMPLETED', 'Step 2 completed lại với deduction mới.', TIMESTAMP '2026-07-06 11:44:00', '{"changed_step_code": "PRELIMINARY_INFO", "new_total_preliminary_deduction_amount": 800000}'::jsonb),
    ('72000000-0000-0000-0000-000000000010', '70000000-0000-0000-0000-000000000003', 'CUSTOMER_ASSET_LOAN_PROPOSAL', 'INVALIDATE_STEP', 'COMPLETED', 'COMPLETED', 'Step gộp cần review do Step 2 thay đổi deduction.', TIMESTAMP '2026-07-06 11:45:00', '{"changed_step_code": "PRELIMINARY_INFO", "affected_step_code": "CUSTOMER_ASSET_LOAN_PROPOSAL"}'::jsonb),
    ('72000000-0000-0000-0000-000000000011', '70000000-0000-0000-0000-000000000004', NULL, 'CREATE_DRAFT', NULL, 'DRAFT', 'Tạo draft đã convert.', TIMESTAMP '2026-07-06 13:00:00', '{}'::jsonb),
    ('72000000-0000-0000-0000-000000000012', '70000000-0000-0000-0000-000000000004', 'UPLOAD_COMPLETE', 'COMPLETE_STEP', 'IN_PROGRESS', 'COMPLETED', 'Hoàn tất upload chứng từ.', TIMESTAMP '2026-07-06 13:50:00', '{}'::jsonb),
    ('72000000-0000-0000-0000-000000000013', '70000000-0000-0000-0000-000000000004', NULL, 'COMPLETE_DRAFT', 'DRAFT', 'COMPLETED', 'Draft hoàn thành trước khi convert.', TIMESTAMP '2026-07-06 13:52:00', '{}'::jsonb),
    ('72000000-0000-0000-0000-000000000014', '70000000-0000-0000-0000-000000000004', NULL, 'CONVERT_DRAFT', 'COMPLETED', 'CONVERTED', 'Draft đã convert sang loan application.', TIMESTAMP '2026-07-06 13:55:00', '{"converted_loan_application_id": "30000000-0000-0000-0000-000000000001"}'::jsonb);

-- =========================================================
-- 2. Credit scoring band schema
-- =========================================================

CREATE TABLE IF NOT EXISTS income_score_band (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_set_code VARCHAR(50) NOT NULL DEFAULT 'BASIC_SCORING_V1',
    min_income_amount NUMERIC(18, 2) NOT NULL,
    max_income_amount NUMERIC(18, 2),
    score_value NUMERIC(10, 2) NOT NULL,
    weight NUMERIC(5, 4) NOT NULL DEFAULT 0.3000,
    display_label VARCHAR(255),
    priority INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    effective_to DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_income_score_band_range
        CHECK (max_income_amount IS NULL OR max_income_amount > min_income_amount),
    CONSTRAINT chk_income_score_band_score
        CHECK (score_value >= 0 AND score_value <= 100),
    CONSTRAINT chk_income_score_band_weight
        CHECK (weight >= 0 AND weight <= 1),
    CONSTRAINT chk_income_score_band_effective_range
        CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE TABLE IF NOT EXISTS age_score_band (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_set_code VARCHAR(50) NOT NULL DEFAULT 'BASIC_SCORING_V1',
    min_age INT NOT NULL,
    max_age INT,
    score_value NUMERIC(10, 2) NOT NULL,
    weight NUMERIC(5, 4) NOT NULL DEFAULT 0.4000,
    display_label VARCHAR(255),
    priority INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    effective_to DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_age_score_band_range
        CHECK (max_age IS NULL OR max_age > min_age),
    CONSTRAINT chk_age_score_band_score
        CHECK (score_value >= 0 AND score_value <= 100),
    CONSTRAINT chk_age_score_band_weight
        CHECK (weight >= 0 AND weight <= 1),
    CONSTRAINT chk_age_score_band_effective_range
        CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE TABLE IF NOT EXISTS dependent_score_band (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_set_code VARCHAR(50) NOT NULL DEFAULT 'BASIC_SCORING_V1',
    min_dependent_count INT NOT NULL,
    max_dependent_count INT,
    score_value NUMERIC(10, 2) NOT NULL,
    weight NUMERIC(5, 4) NOT NULL DEFAULT 0.3000,
    display_label VARCHAR(255),
    priority INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    effective_to DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_dependent_score_band_range
        CHECK (max_dependent_count IS NULL OR max_dependent_count > min_dependent_count),
    CONSTRAINT chk_dependent_score_band_score
        CHECK (score_value >= 0 AND score_value <= 100),
    CONSTRAINT chk_dependent_score_band_weight
        CHECK (weight >= 0 AND weight <= 1),
    CONSTRAINT chk_dependent_score_band_effective_range
        CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE TABLE IF NOT EXISTS overall_score_grade_band (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_set_code VARCHAR(50) NOT NULL DEFAULT 'BASIC_SCORING_V1',
    grade_code VARCHAR(10) NOT NULL,
    min_score NUMERIC(10, 2) NOT NULL,
    max_score NUMERIC(10, 2),
    display_label VARCHAR(255),
    priority INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    effective_to DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_overall_score_grade_band_range
        CHECK (max_score IS NULL OR max_score > min_score),
    CONSTRAINT chk_overall_score_grade_band_effective_range
        CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE INDEX IF NOT EXISTS idx_income_score_band_lookup
    ON income_score_band (rule_set_code, is_active, min_income_amount, max_income_amount);

CREATE INDEX IF NOT EXISTS idx_age_score_band_lookup
    ON age_score_band (rule_set_code, is_active, min_age, max_age);

CREATE INDEX IF NOT EXISTS idx_dependent_score_band_lookup
    ON dependent_score_band (rule_set_code, is_active, min_dependent_count, max_dependent_count);

CREATE INDEX IF NOT EXISTS idx_overall_score_grade_band_lookup
    ON overall_score_grade_band (rule_set_code, is_active, min_score, max_score);

COMMENT ON TABLE income_score_band IS 'Catalog table for mapping monthly income amount to income score.';
COMMENT ON TABLE age_score_band IS 'Catalog table for mapping customer age to age score.';
COMMENT ON TABLE dependent_score_band IS 'Catalog table for mapping number of dependents to dependent score.';
COMMENT ON TABLE overall_score_grade_band IS 'Catalog table for mapping overall score to score grade.';

-- =========================================================
-- 3. Credit scoring band seed
-- =========================================================

DELETE FROM overall_score_grade_band WHERE rule_set_code = 'BASIC_SCORING_V1';
DELETE FROM dependent_score_band WHERE rule_set_code = 'BASIC_SCORING_V1';
DELETE FROM age_score_band WHERE rule_set_code = 'BASIC_SCORING_V1';
DELETE FROM income_score_band WHERE rule_set_code = 'BASIC_SCORING_V1';

INSERT INTO income_score_band
(rule_set_code, min_income_amount, max_income_amount, score_value, weight, display_label, priority, effective_from)
VALUES
('BASIC_SCORING_V1', 0,        5000000,  10, 0.3000, 'Từ 0 đến dưới 5 triệu', 1, CURRENT_DATE),
('BASIC_SCORING_V1', 5000000,  10000000, 30, 0.3000, 'Từ 5 đến dưới 10 triệu', 2, CURRENT_DATE),
('BASIC_SCORING_V1', 10000000, 15000000, 50, 0.3000, 'Từ 10 đến dưới 15 triệu', 3, CURRENT_DATE),
('BASIC_SCORING_V1', 15000000, 20000000, 70, 0.3000, 'Từ 15 đến dưới 20 triệu', 4, CURRENT_DATE),
('BASIC_SCORING_V1', 20000000, NULL,     100, 0.3000, 'Từ 20 triệu trở lên', 5, CURRENT_DATE);

INSERT INTO age_score_band
(rule_set_code, min_age, max_age, score_value, weight, display_label, priority, effective_from)
VALUES
('BASIC_SCORING_V1', 18, 25,   40, 0.4000, 'Từ 18 đến dưới 25 tuổi', 1, CURRENT_DATE),
('BASIC_SCORING_V1', 25, 30,   60, 0.4000, 'Từ 25 đến dưới 30 tuổi', 2, CURRENT_DATE),
('BASIC_SCORING_V1', 30, 40,   80, 0.4000, 'Từ 30 đến dưới 40 tuổi', 3, CURRENT_DATE),
('BASIC_SCORING_V1', 40, NULL, 90, 0.4000, 'Từ 40 tuổi trở lên', 4, CURRENT_DATE);

INSERT INTO dependent_score_band
(rule_set_code, min_dependent_count, max_dependent_count, score_value, weight, display_label, priority, effective_from)
VALUES
('BASIC_SCORING_V1', 0, 1,    100, 0.3000, '0 người phụ thuộc', 1, CURRENT_DATE),
('BASIC_SCORING_V1', 1, 2,    80,  0.3000, '1 người phụ thuộc', 2, CURRENT_DATE),
('BASIC_SCORING_V1', 2, 3,    60,  0.3000, '2 người phụ thuộc', 3, CURRENT_DATE),
('BASIC_SCORING_V1', 3, 4,    40,  0.3000, '3 người phụ thuộc', 4, CURRENT_DATE),
('BASIC_SCORING_V1', 4, NULL, 20,  0.3000, 'Từ 4 người phụ thuộc trở lên', 5, CURRENT_DATE);

INSERT INTO overall_score_grade_band
(rule_set_code, grade_code, min_score, max_score, display_label, priority, effective_from)
VALUES
('BASIC_SCORING_V1', 'A', 80, NULL, 'Rất tốt', 1, CURRENT_DATE),
('BASIC_SCORING_V1', 'B', 60, 80,   'Tốt', 2, CURRENT_DATE),
('BASIC_SCORING_V1', 'C', 40, 60,   'Trung bình', 3, CURRENT_DATE),
('BASIC_SCORING_V1', 'D', 20, 40,   'Rủi ro cao', 4, CURRENT_DATE),
('BASIC_SCORING_V1', 'E', 0,  20,   'Rủi ro rất cao', 5, CURRENT_DATE);
