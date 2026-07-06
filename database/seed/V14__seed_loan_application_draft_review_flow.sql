-- Customer Loan Onboarding - Seed Loan Application Draft Review Flow
-- PostgreSQL dialect
-- Version: V14 seed
--
-- Scope:
-- - Reset demo draft data.
-- - Seed demo data for the current 4-step draft flow.
--
-- Current draft steps:
-- 1. CUSTOMER_IDENTIFY
-- 2. PRELIMINARY_INFO
-- 3. CUSTOMER_ASSET_LOAN_PROPOSAL
-- 4. UPLOAD_COMPLETE

TRUNCATE TABLE
    loan_application_draft_history,
    loan_application_draft_step_data,
    loan_application_draft
RESTART IDENTITY CASCADE;

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
    -- Case 1: Draft mới bắt đầu, đang làm Step 2.
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
        '{
            "preliminary_customer": {
                "full_name": "Nguyen Van An",
                "phone_number": "0912345678",
                "identity_number": "001203000001"
            },
            "preliminary_collateral": {
                "collateral_type": "MOTORBIKE",
                "brand": "Honda",
                "model": "SH",
                "estimated_value": 85000000
            },
            "preliminary_deductions": [
                {
                    "code": "SCRATCH",
                    "name": "Trầy xước",
                    "amount": 500000
                }
            ],
            "total_preliminary_deduction_amount": 500000,
            "preliminary_loan_package": {
                "requested_amount": 50000000,
                "loan_term_months": 12
            }
        }'::jsonb,
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

    -- Case 2: Đang làm step gộp thông tin khách hàng + tài sản + chọn gói vay.
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
        '{
            "preliminary_customer": {
                "full_name": "Tran Thi Binh",
                "phone_number": "0987654321",
                "identity_number": "001203000002"
            },
            "preliminary_collateral": {
                "collateral_type": "MOTORBIKE",
                "brand": "Honda",
                "model": "Vision",
                "estimated_value": 45000000
            },
            "preliminary_deductions": [
                {
                    "code": "SCRATCH",
                    "name": "Trầy xước",
                    "amount": 300000
                },
                {
                    "code": "MISSING_DOCUMENT",
                    "name": "Thiếu giấy tờ",
                    "amount": 700000
                }
            ],
            "total_preliminary_deduction_amount": 1000000,
            "preliminary_loan_package": {
                "requested_amount": 25000000,
                "loan_term_months": 12
            }
        }'::jsonb,
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
        '{
            "customer_detail": {
                "full_name": "Tran Thi Binh",
                "phone_number": "0987654321",
                "identity_number": "001203000002",
                "date_of_birth": "1995-05-20",
                "address": "Ha Noi",
                "employment_type": "SALARIED",
                "monthly_income": 15000000
            },
            "reference_persons": [
                {
                    "full_name": "Tran Van C",
                    "phone_number": "0900000001",
                    "relationship": "FATHER"
                }
            ],
            "collateral_detail": {
                "collateral_type": "MOTORBIKE",
                "brand": "Honda",
                "model": "Vision",
                "license_plate": "29A1-12345",
                "frame_number": "RLHJF123456789001",
                "engine_number": "JF45E1234567",
                "manufacture_year": 2022
            },
            "confirmed_deductions": [
                {
                    "code": "SCRATCH",
                    "name": "Trầy xước",
                    "amount": 300000
                },
                {
                    "code": "MISSING_DOCUMENT",
                    "name": "Thiếu giấy tờ",
                    "amount": 700000
                }
            ],
            "confirmed_total_deduction_amount": 1000000,
            "selected_loan_offer": {
                "loan_product_id": "PRODUCT_MOTORBIKE_STANDARD",
                "product_name": "Vay cầm cố xe máy tiêu chuẩn",
                "final_requested_amount": 25000000,
                "final_loan_term_months": 12,
                "interest_rate": 1.5,
                "estimated_monthly_payment": 2350000
            },
            "payment_info": {
                "payment_method": "BANK_TRANSFER",
                "payment_day": 15
            }
        }'::jsonb,
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

    -- Case 3: Step 2 đổi giảm trừ, step gộp giữ payload cũ và cần review.
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
        '{
            "preliminary_customer": {
                "full_name": "Pham Van Cuong",
                "phone_number": "0977000001",
                "identity_number": "001203000003"
            },
            "preliminary_collateral": {
                "collateral_type": "MOTORBIKE",
                "brand": "Yamaha",
                "model": "Exciter",
                "estimated_value": 50000000
            },
            "preliminary_deductions": [
                {
                    "code": "SCRATCH",
                    "name": "Trầy xước",
                    "amount": 800000
                }
            ],
            "total_preliminary_deduction_amount": 800000,
            "preliminary_loan_package": {
                "requested_amount": 30000000,
                "loan_term_months": 12
            }
        }'::jsonb,
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
        '{
            "customer_detail": {
                "full_name": "Pham Van Cuong",
                "phone_number": "0977000001",
                "identity_number": "001203000003",
                "date_of_birth": "1991-09-12",
                "address": "Da Nang",
                "employment_type": "SELF_EMPLOYED",
                "monthly_income": 18000000
            },
            "reference_persons": [
                {
                    "full_name": "Pham Thi H",
                    "phone_number": "0900000003",
                    "relationship": "SISTER"
                }
            ],
            "collateral_detail": {
                "collateral_type": "MOTORBIKE",
                "brand": "Yamaha",
                "model": "Exciter",
                "license_plate": "30B1-88888",
                "frame_number": "RLCUG0610NY123456",
                "engine_number": "G3D4E5678901",
                "manufacture_year": 2021
            },
            "confirmed_deductions": [
                {
                    "code": "SCRATCH",
                    "name": "Trầy xước",
                    "amount": 500000
                },
                {
                    "code": "MISSING_DOCUMENT",
                    "name": "Thiếu giấy tờ",
                    "amount": 1000000
                }
            ],
            "confirmed_total_deduction_amount": 1500000,
            "selected_loan_offer": {
                "loan_product_id": "PRODUCT_MOTORBIKE_STANDARD",
                "product_name": "Vay cầm cố xe máy tiêu chuẩn",
                "final_requested_amount": 30000000,
                "final_loan_term_months": 12,
                "interest_rate": 1.6,
                "estimated_monthly_payment": 2850000
            },
            "payment_info": {
                "payment_method": "CASH",
                "payment_day": 10
            }
        }'::jsonb,
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

    -- Case 4: Draft đã hoàn tất và convert.
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
        '{"customer_detail": {"full_name": "Hoang Duc Huy", "phone_number": "0955000005", "identity_number": "001203000005", "date_of_birth": "1990-10-25", "address": "Ha Noi", "employment_type": "SALARIED", "monthly_income": 25000000}, "reference_persons": [{"full_name": "Hoang Thi K", "phone_number": "0900000005", "relationship": "MOTHER"}], "collateral_detail": {"collateral_type": "MOTORBIKE", "brand": "Honda", "model": "SH", "license_plate": "29H1-55555", "frame_number": "RLHSH1502023ABCDE", "engine_number": "SH150E2023ABCDE", "manufacture_year": 2023}, "confirmed_deductions": [{"code": "SCRATCH", "name": "Trầy xước", "amount": 400000}], "confirmed_total_deduction_amount": 400000, "selected_loan_offer": {"loan_product_id": "PRODUCT_MOTORBIKE_STANDARD", "product_name": "Vay cầm cố xe máy tiêu chuẩn", "final_requested_amount": 45000000, "final_loan_term_months": 12, "interest_rate": 1.5, "estimated_monthly_payment": 4200000}, "payment_info": {"payment_method": "BANK_TRANSFER", "payment_day": 20}}'::jsonb,
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
    ('72000000-0000-0000-0000-000000000010', '70000000-0000-0000-0000-000000000003', 'CUSTOMER_ASSET_LOAN_PROPOSAL', 'INVALIDATE_STEP', 'COMPLETED', 'COMPLETED', 'Step gộp cần review do Step 2 thay đổi deduction.', TIMESTAMP '2026-07-06 11:45:00', '{"changed_step_code": "PRELIMINARY_INFO", "affected_step_code": "CUSTOMER_ASSET_LOAN_PROPOSAL", "reason": "Preliminary deduction list changed, customer/asset/loan proposal step requires manual review"}'::jsonb),

    ('72000000-0000-0000-0000-000000000011', '70000000-0000-0000-0000-000000000004', NULL, 'CREATE_DRAFT', NULL, 'DRAFT', 'Tạo draft đã convert.', TIMESTAMP '2026-07-06 13:00:00', '{}'::jsonb),
    ('72000000-0000-0000-0000-000000000012', '70000000-0000-0000-0000-000000000004', 'UPLOAD_COMPLETE', 'COMPLETE_STEP', 'IN_PROGRESS', 'COMPLETED', 'Hoàn tất upload chứng từ.', TIMESTAMP '2026-07-06 13:50:00', '{}'::jsonb),
    ('72000000-0000-0000-0000-000000000013', '70000000-0000-0000-0000-000000000004', NULL, 'COMPLETE_DRAFT', 'DRAFT', 'COMPLETED', 'Draft hoàn thành trước khi convert.', TIMESTAMP '2026-07-06 13:52:00', '{}'::jsonb),
    ('72000000-0000-0000-0000-000000000014', '70000000-0000-0000-0000-000000000004', NULL, 'CONVERT_DRAFT', 'COMPLETED', 'CONVERTED', 'Draft đã convert sang loan application.', TIMESTAMP '2026-07-06 13:55:00', '{"converted_loan_application_id": "30000000-0000-0000-0000-000000000001"}'::jsonb);
