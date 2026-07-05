-- Customer Loan Onboarding - Seed Loan Application Draft Review Flow
-- PostgreSQL dialect
-- Version: V13 seed
--
-- Scope:
-- - Reset demo draft data only.
-- - Seed 5 draft cases for the 6-step loan application draft flow.
-- - Use requires_review for stale downstream payloads.
-- - Keep step statuses limited to NOT_STARTED, IN_PROGRESS, COMPLETED.

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
        '60000000-0000-0000-0000-000000000001',
        'DRF-REVIEW-000001',
        '10000000-0000-0000-0000-000000000001',
        'PRELIMINARY_INFO',
        'DRAFT',
        NULL,
        TIMESTAMP '2026-08-31 23:59:59',
        TIMESTAMP '2026-07-05 09:00:00',
        TIMESTAMP '2026-07-05 09:12:00'
    ),
    (
        '60000000-0000-0000-0000-000000000002',
        'DRF-REVIEW-000002',
        '10000000-0000-0000-0000-000000000002',
        'UPLOAD_COMPLETE',
        'COMPLETED',
        NULL,
        TIMESTAMP '2026-08-31 23:59:59',
        TIMESTAMP '2026-07-05 10:00:00',
        TIMESTAMP '2026-07-05 10:50:00'
    ),
    (
        '60000000-0000-0000-0000-000000000003',
        'DRF-REVIEW-000003',
        '10000000-0000-0000-0000-000000000003',
        'ASSET_DETAIL',
        'DRAFT',
        NULL,
        TIMESTAMP '2026-08-31 23:59:59',
        TIMESTAMP '2026-07-05 11:00:00',
        TIMESTAMP '2026-07-05 11:55:00'
    ),
    (
        '60000000-0000-0000-0000-000000000004',
        'DRF-REVIEW-000004',
        '10000000-0000-0000-0000-000000000004',
        'FINAL_LOAN_PROPOSAL',
        'DRAFT',
        NULL,
        TIMESTAMP '2026-08-31 23:59:59',
        TIMESTAMP '2026-07-05 13:00:00',
        TIMESTAMP '2026-07-05 13:45:00'
    ),
    (
        '60000000-0000-0000-0000-000000000005',
        'DRF-REVIEW-000005',
        '10000000-0000-0000-0000-000000000005',
        'UPLOAD_COMPLETE',
        'CONVERTED',
        '30000000-0000-0000-0000-000000000001',
        TIMESTAMP '2026-08-31 23:59:59',
        TIMESTAMP '2026-07-05 14:00:00',
        TIMESTAMP '2026-07-05 14:55:00'
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
    -- Case 1: Draft newly started, doing Step 2.
    ('61000000-0000-0000-0000-000000000001', '60000000-0000-0000-0000-000000000001', 'CUSTOMER_IDENTIFY', 'COMPLETED',
        '{"initial_identification": {"full_name": "Nguyen Van An", "phone_number": "0912345678", "identity_number": "001203000001", "matched_existing_customer": true}}'::jsonb,
        FALSE, NULL, NULL, NULL, TIMESTAMP '2026-07-05 09:05:00', NULL, TIMESTAMP '2026-07-05 09:00:00', TIMESTAMP '2026-07-05 09:05:00'),
    ('61000000-0000-0000-0000-000000000002', '60000000-0000-0000-0000-000000000001', 'PRELIMINARY_INFO', 'IN_PROGRESS',
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
        FALSE, NULL, NULL, NULL, NULL, NULL, TIMESTAMP '2026-07-05 09:06:00', TIMESTAMP '2026-07-05 09:12:00'),
    ('61000000-0000-0000-0000-000000000003', '60000000-0000-0000-0000-000000000001', 'CUSTOMER_DETAIL', 'NOT_STARTED', '{}'::jsonb,
        FALSE, NULL, NULL, NULL, NULL, NULL, TIMESTAMP '2026-07-05 09:00:00', TIMESTAMP '2026-07-05 09:00:00'),
    ('61000000-0000-0000-0000-000000000004', '60000000-0000-0000-0000-000000000001', 'ASSET_DETAIL', 'NOT_STARTED', '{}'::jsonb,
        FALSE, NULL, NULL, NULL, NULL, NULL, TIMESTAMP '2026-07-05 09:00:00', TIMESTAMP '2026-07-05 09:00:00'),
    ('61000000-0000-0000-0000-000000000005', '60000000-0000-0000-0000-000000000001', 'FINAL_LOAN_PROPOSAL', 'NOT_STARTED', '{}'::jsonb,
        FALSE, NULL, NULL, NULL, NULL, NULL, TIMESTAMP '2026-07-05 09:00:00', TIMESTAMP '2026-07-05 09:00:00'),
    ('61000000-0000-0000-0000-000000000006', '60000000-0000-0000-0000-000000000001', 'UPLOAD_COMPLETE', 'NOT_STARTED', '{}'::jsonb,
        FALSE, NULL, NULL, NULL, NULL, NULL, TIMESTAMP '2026-07-05 09:00:00', TIMESTAMP '2026-07-05 09:00:00'),

    -- Case 2: All 6 steps completed, not converted yet.
    ('61000000-0000-0000-0000-000000000007', '60000000-0000-0000-0000-000000000002', 'CUSTOMER_IDENTIFY', 'COMPLETED',
        '{"initial_identification": {"full_name": "Tran Thi Binh", "phone_number": "0987654321", "identity_number": "001203000002", "matched_existing_customer": true}}'::jsonb,
        FALSE, NULL, NULL, NULL, TIMESTAMP '2026-07-05 10:05:00', NULL, TIMESTAMP '2026-07-05 10:00:00', TIMESTAMP '2026-07-05 10:05:00'),
    ('61000000-0000-0000-0000-000000000008', '60000000-0000-0000-0000-000000000002', 'PRELIMINARY_INFO', 'COMPLETED',
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
        FALSE, NULL, NULL, NULL, TIMESTAMP '2026-07-05 10:12:00', NULL, TIMESTAMP '2026-07-05 10:06:00', TIMESTAMP '2026-07-05 10:12:00'),
    ('61000000-0000-0000-0000-000000000009', '60000000-0000-0000-0000-000000000002', 'CUSTOMER_DETAIL', 'COMPLETED',
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
                },
                {
                    "full_name": "Le Thi D",
                    "phone_number": "0900000002",
                    "relationship": "FRIEND"
                }
            ]
        }'::jsonb,
        FALSE, NULL, NULL, NULL, TIMESTAMP '2026-07-05 10:20:00', NULL, TIMESTAMP '2026-07-05 10:13:00', TIMESTAMP '2026-07-05 10:20:00'),
    ('61000000-0000-0000-0000-000000000010', '60000000-0000-0000-0000-000000000002', 'ASSET_DETAIL', 'COMPLETED',
        '{
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
            "confirmed_total_deduction_amount": 1000000
        }'::jsonb,
        FALSE, NULL, NULL, NULL, TIMESTAMP '2026-07-05 10:30:00', NULL, TIMESTAMP '2026-07-05 10:21:00', TIMESTAMP '2026-07-05 10:30:00'),
    ('61000000-0000-0000-0000-000000000011', '60000000-0000-0000-0000-000000000002', 'FINAL_LOAN_PROPOSAL', 'COMPLETED',
        '{
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
        FALSE, NULL, NULL, NULL, TIMESTAMP '2026-07-05 10:40:00', NULL, TIMESTAMP '2026-07-05 10:31:00', TIMESTAMP '2026-07-05 10:40:00'),
    ('61000000-0000-0000-0000-000000000012', '60000000-0000-0000-0000-000000000002', 'UPLOAD_COMPLETE', 'COMPLETED',
        '{
            "required_documents": [
                {
                    "document_type": "IDENTITY_CARD",
                    "required": true,
                    "status": "UPLOADED"
                },
                {
                    "document_type": "VEHICLE_REGISTRATION",
                    "required": true,
                    "status": "UPLOADED"
                }
            ],
            "uploaded_documents": [
                {
                    "document_type": "IDENTITY_CARD",
                    "file_name": "identity_card_front.jpg",
                    "status": "VALIDATED"
                },
                {
                    "document_type": "VEHICLE_REGISTRATION",
                    "file_name": "vehicle_registration.jpg",
                    "status": "VALIDATED"
                }
            ]
        }'::jsonb,
        FALSE, NULL, NULL, NULL, TIMESTAMP '2026-07-05 10:50:00', NULL, TIMESTAMP '2026-07-05 10:41:00', TIMESTAMP '2026-07-05 10:50:00'),

    -- Case 3: Step 2 changed deductions; Step 4 and Step 5 keep old payload and require review.
    ('61000000-0000-0000-0000-000000000013', '60000000-0000-0000-0000-000000000003', 'CUSTOMER_IDENTIFY', 'COMPLETED',
        '{"initial_identification": {"full_name": "Pham Van Cuong", "phone_number": "0977000001", "identity_number": "001203000003", "matched_existing_customer": true}}'::jsonb,
        FALSE, NULL, NULL, NULL, TIMESTAMP '2026-07-05 11:05:00', NULL, TIMESTAMP '2026-07-05 11:00:00', TIMESTAMP '2026-07-05 11:05:00'),
    ('61000000-0000-0000-0000-000000000014', '60000000-0000-0000-0000-000000000003', 'PRELIMINARY_INFO', 'COMPLETED',
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
        FALSE, NULL, NULL, NULL, TIMESTAMP '2026-07-05 11:44:00', NULL, TIMESTAMP '2026-07-05 11:06:00', TIMESTAMP '2026-07-05 11:44:00'),
    ('61000000-0000-0000-0000-000000000015', '60000000-0000-0000-0000-000000000003', 'CUSTOMER_DETAIL', 'COMPLETED',
        '{"customer_detail": {"full_name": "Pham Van Cuong", "phone_number": "0977000001", "identity_number": "001203000003", "date_of_birth": "1991-09-12", "address": "Da Nang", "employment_type": "SELF_EMPLOYED", "monthly_income": 18000000}, "reference_persons": [{"full_name": "Pham Thi H", "phone_number": "0900000003", "relationship": "SISTER"}]}'::jsonb,
        FALSE, NULL, NULL, NULL, TIMESTAMP '2026-07-05 11:20:00', NULL, TIMESTAMP '2026-07-05 11:13:00', TIMESTAMP '2026-07-05 11:20:00'),
    ('61000000-0000-0000-0000-000000000016', '60000000-0000-0000-0000-000000000003', 'ASSET_DETAIL', 'COMPLETED',
        '{
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
            "confirmed_total_deduction_amount": 1500000
        }'::jsonb,
        TRUE, 'PRELIMINARY_INFO', TIMESTAMP '2026-07-05 11:45:00', NULL, TIMESTAMP '2026-07-05 11:30:00', 'Preliminary deduction list changed, collateral detail requires manual review.', TIMESTAMP '2026-07-05 11:21:00', TIMESTAMP '2026-07-05 11:45:00'),
    ('61000000-0000-0000-0000-000000000017', '60000000-0000-0000-0000-000000000003', 'FINAL_LOAN_PROPOSAL', 'COMPLETED',
        '{
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
        TRUE, 'PRELIMINARY_INFO', TIMESTAMP '2026-07-05 11:45:00', NULL, TIMESTAMP '2026-07-05 11:38:00', 'Preliminary deduction list changed, final loan offer may be affected.', TIMESTAMP '2026-07-05 11:31:00', TIMESTAMP '2026-07-05 11:45:00'),
    ('61000000-0000-0000-0000-000000000018', '60000000-0000-0000-0000-000000000003', 'UPLOAD_COMPLETE', 'COMPLETED',
        '{"required_documents": [{"document_type": "IDENTITY_CARD", "required": true, "status": "UPLOADED"}, {"document_type": "VEHICLE_REGISTRATION", "required": true, "status": "UPLOADED"}], "uploaded_documents": [{"document_type": "IDENTITY_CARD", "file_name": "cuong_identity.jpg", "status": "VALIDATED"}, {"document_type": "VEHICLE_REGISTRATION", "file_name": "cuong_vehicle_registration.jpg", "status": "VALIDATED"}]}'::jsonb,
        FALSE, NULL, NULL, NULL, TIMESTAMP '2026-07-05 11:40:00', NULL, TIMESTAMP '2026-07-05 11:39:00', TIMESTAMP '2026-07-05 11:40:00'),

    -- Case 4: Step 4 reviewed after Step 2 changed; Step 5 still requires review.
    ('61000000-0000-0000-0000-000000000019', '60000000-0000-0000-0000-000000000004', 'CUSTOMER_IDENTIFY', 'COMPLETED',
        '{"initial_identification": {"full_name": "Le Thi Dung", "phone_number": "0966000004", "identity_number": "001203000004", "matched_existing_customer": true}}'::jsonb,
        FALSE, NULL, NULL, NULL, TIMESTAMP '2026-07-05 13:05:00', NULL, TIMESTAMP '2026-07-05 13:00:00', TIMESTAMP '2026-07-05 13:05:00'),
    ('61000000-0000-0000-0000-000000000020', '60000000-0000-0000-0000-000000000004', 'PRELIMINARY_INFO', 'COMPLETED',
        '{"preliminary_customer": {"full_name": "Le Thi Dung", "phone_number": "0966000004", "identity_number": "001203000004"}, "preliminary_collateral": {"collateral_type": "MOTORBIKE", "brand": "Yamaha", "model": "Exciter", "estimated_value": 50000000}, "preliminary_deductions": [{"code": "SCRATCH", "name": "Trầy xước", "amount": 800000}], "total_preliminary_deduction_amount": 800000, "preliminary_loan_package": {"requested_amount": 30000000, "loan_term_months": 12}}'::jsonb,
        FALSE, NULL, NULL, NULL, TIMESTAMP '2026-07-05 13:15:00', NULL, TIMESTAMP '2026-07-05 13:06:00', TIMESTAMP '2026-07-05 13:15:00'),
    ('61000000-0000-0000-0000-000000000021', '60000000-0000-0000-0000-000000000004', 'CUSTOMER_DETAIL', 'COMPLETED',
        '{"customer_detail": {"full_name": "Le Thi Dung", "phone_number": "0966000004", "identity_number": "001203000004", "date_of_birth": "1994-04-22", "address": "Ho Chi Minh City", "employment_type": "SALARIED", "monthly_income": 21000000}, "reference_persons": [{"full_name": "Le Van E", "phone_number": "0900000004", "relationship": "BROTHER"}]}'::jsonb,
        FALSE, NULL, NULL, NULL, TIMESTAMP '2026-07-05 13:22:00', NULL, TIMESTAMP '2026-07-05 13:16:00', TIMESTAMP '2026-07-05 13:22:00'),
    ('61000000-0000-0000-0000-000000000022', '60000000-0000-0000-0000-000000000004', 'ASSET_DETAIL', 'COMPLETED',
        '{"collateral_detail": {"collateral_type": "MOTORBIKE", "brand": "Yamaha", "model": "Exciter", "license_plate": "30B1-99999", "frame_number": "RLCUG0610NY999999", "engine_number": "G3D4E9999999", "manufacture_year": 2021}, "confirmed_deductions": [{"code": "SCRATCH", "name": "Trầy xước", "amount": 800000}], "confirmed_total_deduction_amount": 800000}'::jsonb,
        FALSE, 'PRELIMINARY_INFO', TIMESTAMP '2026-07-05 13:30:00', TIMESTAMP '2026-07-05 13:38:00', TIMESTAMP '2026-07-05 13:38:00', NULL, TIMESTAMP '2026-07-05 13:23:00', TIMESTAMP '2026-07-05 13:38:00'),
    ('61000000-0000-0000-0000-000000000023', '60000000-0000-0000-0000-000000000004', 'FINAL_LOAN_PROPOSAL', 'COMPLETED',
        '{"selected_loan_offer": {"loan_product_id": "PRODUCT_MOTORBIKE_STANDARD", "product_name": "Vay cầm cố xe máy tiêu chuẩn", "final_requested_amount": 30000000, "final_loan_term_months": 12, "interest_rate": 1.6, "estimated_monthly_payment": 2850000}, "payment_info": {"payment_method": "CASH", "payment_day": 10}}'::jsonb,
        TRUE, 'PRELIMINARY_INFO', TIMESTAMP '2026-07-05 13:30:00', NULL, TIMESTAMP '2026-07-05 13:40:00', 'Step 4 was reviewed, but final loan offer still requires review.', TIMESTAMP '2026-07-05 13:31:00', TIMESTAMP '2026-07-05 13:45:00'),
    ('61000000-0000-0000-0000-000000000024', '60000000-0000-0000-0000-000000000004', 'UPLOAD_COMPLETE', 'COMPLETED',
        '{"required_documents": [{"document_type": "IDENTITY_CARD", "required": true, "status": "UPLOADED"}, {"document_type": "VEHICLE_REGISTRATION", "required": true, "status": "UPLOADED"}], "uploaded_documents": [{"document_type": "IDENTITY_CARD", "file_name": "dung_identity.jpg", "status": "VALIDATED"}, {"document_type": "VEHICLE_REGISTRATION", "file_name": "dung_vehicle_registration.jpg", "status": "VALIDATED"}]}'::jsonb,
        FALSE, NULL, NULL, NULL, TIMESTAMP '2026-07-05 13:42:00', NULL, TIMESTAMP '2026-07-05 13:41:00', TIMESTAMP '2026-07-05 13:42:00'),

    -- Case 5: Draft completed and converted.
    ('61000000-0000-0000-0000-000000000025', '60000000-0000-0000-0000-000000000005', 'CUSTOMER_IDENTIFY', 'COMPLETED',
        '{"initial_identification": {"full_name": "Hoang Duc Huy", "phone_number": "0955000005", "identity_number": "001203000005", "matched_existing_customer": true}}'::jsonb,
        FALSE, NULL, NULL, NULL, TIMESTAMP '2026-07-05 14:05:00', NULL, TIMESTAMP '2026-07-05 14:00:00', TIMESTAMP '2026-07-05 14:05:00'),
    ('61000000-0000-0000-0000-000000000026', '60000000-0000-0000-0000-000000000005', 'PRELIMINARY_INFO', 'COMPLETED',
        '{"preliminary_customer": {"full_name": "Hoang Duc Huy", "phone_number": "0955000005", "identity_number": "001203000005"}, "preliminary_collateral": {"collateral_type": "MOTORBIKE", "brand": "Honda", "model": "SH", "estimated_value": 90000000}, "preliminary_deductions": [{"code": "SCRATCH", "name": "Trầy xước", "amount": 400000}], "total_preliminary_deduction_amount": 400000, "preliminary_loan_package": {"requested_amount": 45000000, "loan_term_months": 12}}'::jsonb,
        FALSE, NULL, NULL, NULL, TIMESTAMP '2026-07-05 14:12:00', NULL, TIMESTAMP '2026-07-05 14:06:00', TIMESTAMP '2026-07-05 14:12:00'),
    ('61000000-0000-0000-0000-000000000027', '60000000-0000-0000-0000-000000000005', 'CUSTOMER_DETAIL', 'COMPLETED',
        '{"customer_detail": {"full_name": "Hoang Duc Huy", "phone_number": "0955000005", "identity_number": "001203000005", "date_of_birth": "1990-10-25", "address": "Ha Noi", "employment_type": "SALARIED", "monthly_income": 25000000}, "reference_persons": [{"full_name": "Hoang Thi K", "phone_number": "0900000005", "relationship": "MOTHER"}]}'::jsonb,
        FALSE, NULL, NULL, NULL, TIMESTAMP '2026-07-05 14:20:00', NULL, TIMESTAMP '2026-07-05 14:13:00', TIMESTAMP '2026-07-05 14:20:00'),
    ('61000000-0000-0000-0000-000000000028', '60000000-0000-0000-0000-000000000005', 'ASSET_DETAIL', 'COMPLETED',
        '{"collateral_detail": {"collateral_type": "MOTORBIKE", "brand": "Honda", "model": "SH", "license_plate": "29H1-55555", "frame_number": "RLHSH1502023ABCDE", "engine_number": "SH150E2023ABCDE", "manufacture_year": 2023}, "confirmed_deductions": [{"code": "SCRATCH", "name": "Trầy xước", "amount": 400000}], "confirmed_total_deduction_amount": 400000}'::jsonb,
        FALSE, NULL, NULL, NULL, TIMESTAMP '2026-07-05 14:30:00', NULL, TIMESTAMP '2026-07-05 14:21:00', TIMESTAMP '2026-07-05 14:30:00'),
    ('61000000-0000-0000-0000-000000000029', '60000000-0000-0000-0000-000000000005', 'FINAL_LOAN_PROPOSAL', 'COMPLETED',
        '{"selected_loan_offer": {"loan_product_id": "PRODUCT_MOTORBIKE_STANDARD", "product_name": "Vay cầm cố xe máy tiêu chuẩn", "final_requested_amount": 45000000, "final_loan_term_months": 12, "interest_rate": 1.5, "estimated_monthly_payment": 4200000}, "payment_info": {"payment_method": "BANK_TRANSFER", "payment_day": 20}}'::jsonb,
        FALSE, NULL, NULL, NULL, TIMESTAMP '2026-07-05 14:40:00', NULL, TIMESTAMP '2026-07-05 14:31:00', TIMESTAMP '2026-07-05 14:40:00'),
    ('61000000-0000-0000-0000-000000000030', '60000000-0000-0000-0000-000000000005', 'UPLOAD_COMPLETE', 'COMPLETED',
        '{"required_documents": [{"document_type": "IDENTITY_CARD", "required": true, "status": "UPLOADED"}, {"document_type": "VEHICLE_REGISTRATION", "required": true, "status": "UPLOADED"}], "uploaded_documents": [{"document_type": "IDENTITY_CARD", "file_name": "huy_identity.jpg", "status": "VALIDATED"}, {"document_type": "VEHICLE_REGISTRATION", "file_name": "huy_vehicle_registration.jpg", "status": "VALIDATED"}]}'::jsonb,
        FALSE, NULL, NULL, NULL, TIMESTAMP '2026-07-05 14:50:00', NULL, TIMESTAMP '2026-07-05 14:41:00', TIMESTAMP '2026-07-05 14:50:00');

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
    ('62000000-0000-0000-0000-000000000001', '60000000-0000-0000-0000-000000000001', NULL, 'CREATE_DRAFT', NULL, 'DRAFT', 'Draft mới bắt đầu.', TIMESTAMP '2026-07-05 09:00:00', '{}'::jsonb),
    ('62000000-0000-0000-0000-000000000002', '60000000-0000-0000-0000-000000000001', 'CUSTOMER_IDENTIFY', 'COMPLETE_STEP', 'IN_PROGRESS', 'COMPLETED', 'Hoàn tất định danh ban đầu.', TIMESTAMP '2026-07-05 09:05:00', '{}'::jsonb),
    ('62000000-0000-0000-0000-000000000003', '60000000-0000-0000-0000-000000000001', 'PRELIMINARY_INFO', 'SAVE_STEP', 'NOT_STARTED', 'IN_PROGRESS', 'Lưu dở thông tin sơ bộ.', TIMESTAMP '2026-07-05 09:12:00', '{}'::jsonb),

    ('62000000-0000-0000-0000-000000000004', '60000000-0000-0000-0000-000000000002', NULL, 'CREATE_DRAFT', NULL, 'DRAFT', 'Tạo draft hoàn thành đủ 6 bước.', TIMESTAMP '2026-07-05 10:00:00', '{}'::jsonb),
    ('62000000-0000-0000-0000-000000000005', '60000000-0000-0000-0000-000000000002', 'UPLOAD_COMPLETE', 'COMPLETE_STEP', 'IN_PROGRESS', 'COMPLETED', 'Hoàn tất upload chứng từ.', TIMESTAMP '2026-07-05 10:50:00', '{}'::jsonb),
    ('62000000-0000-0000-0000-000000000006', '60000000-0000-0000-0000-000000000002', NULL, 'COMPLETE_DRAFT', 'DRAFT', 'COMPLETED', 'Draft đủ dữ liệu, chưa convert.', TIMESTAMP '2026-07-05 10:51:00', '{}'::jsonb),

    ('62000000-0000-0000-0000-000000000007', '60000000-0000-0000-0000-000000000003', NULL, 'CREATE_DRAFT', NULL, 'DRAFT', 'Tạo draft case review downstream.', TIMESTAMP '2026-07-05 11:00:00', '{}'::jsonb),
    ('62000000-0000-0000-0000-000000000008', '60000000-0000-0000-0000-000000000003', 'PRELIMINARY_INFO', 'REOPEN_STEP', 'COMPLETED', 'IN_PROGRESS', 'User quay lại sửa danh sách giảm trừ sơ bộ.', TIMESTAMP '2026-07-05 11:42:00', '{"changed_step_code": "PRELIMINARY_INFO"}'::jsonb),
    ('62000000-0000-0000-0000-000000000009', '60000000-0000-0000-0000-000000000003', 'PRELIMINARY_INFO', 'COMPLETE_STEP', 'IN_PROGRESS', 'COMPLETED', 'Step 2 completed lại với deduction mới.', TIMESTAMP '2026-07-05 11:44:00', '{"changed_step_code": "PRELIMINARY_INFO", "new_total_preliminary_deduction_amount": 800000}'::jsonb),
    ('62000000-0000-0000-0000-000000000010', '60000000-0000-0000-0000-000000000003', 'ASSET_DETAIL', 'INVALIDATE_STEP', 'COMPLETED', 'COMPLETED', 'Step 4 cần review do Step 2 thay đổi deduction.', TIMESTAMP '2026-07-05 11:45:00', '{"changed_step_code": "PRELIMINARY_INFO", "affected_step_code": "ASSET_DETAIL", "reason": "Preliminary deduction list changed, collateral detail requires manual review"}'::jsonb),
    ('62000000-0000-0000-0000-000000000011', '60000000-0000-0000-0000-000000000003', 'FINAL_LOAN_PROPOSAL', 'INVALIDATE_STEP', 'COMPLETED', 'COMPLETED', 'Step 5 cần review do giá trị tài sản/khoản vay có thể bị ảnh hưởng.', TIMESTAMP '2026-07-05 11:45:00', '{"changed_step_code": "PRELIMINARY_INFO", "affected_step_code": "FINAL_LOAN_PROPOSAL", "reason": "Preliminary deduction list changed, final loan offer may be affected"}'::jsonb),

    ('62000000-0000-0000-0000-000000000012', '60000000-0000-0000-0000-000000000004', NULL, 'CREATE_DRAFT', NULL, 'DRAFT', 'Tạo draft case Step 4 đã review, Step 5 chưa review.', TIMESTAMP '2026-07-05 13:00:00', '{}'::jsonb),
    ('62000000-0000-0000-0000-000000000013', '60000000-0000-0000-0000-000000000004', 'ASSET_DETAIL', 'SAVE_STEP', 'COMPLETED', 'COMPLETED', 'User đã kiểm tra lại Step 4 sau thay đổi Step 2.', TIMESTAMP '2026-07-05 13:38:00', '{"changed_step_code": "PRELIMINARY_INFO", "reviewed_step_code": "ASSET_DETAIL"}'::jsonb),
    ('62000000-0000-0000-0000-000000000014', '60000000-0000-0000-0000-000000000004', 'FINAL_LOAN_PROPOSAL', 'INVALIDATE_STEP', 'COMPLETED', 'COMPLETED', 'Step 5 vẫn cần review.', TIMESTAMP '2026-07-05 13:45:00', '{"changed_step_code": "PRELIMINARY_INFO", "affected_step_code": "FINAL_LOAN_PROPOSAL", "reason": "Step 4 reviewed but final loan offer has not been reviewed yet"}'::jsonb),

    ('62000000-0000-0000-0000-000000000015', '60000000-0000-0000-0000-000000000005', NULL, 'CREATE_DRAFT', NULL, 'DRAFT', 'Tạo draft đã convert.', TIMESTAMP '2026-07-05 14:00:00', '{}'::jsonb),
    ('62000000-0000-0000-0000-000000000016', '60000000-0000-0000-0000-000000000005', 'UPLOAD_COMPLETE', 'COMPLETE_STEP', 'IN_PROGRESS', 'COMPLETED', 'Hoàn tất chứng từ.', TIMESTAMP '2026-07-05 14:50:00', '{}'::jsonb),
    ('62000000-0000-0000-0000-000000000017', '60000000-0000-0000-0000-000000000005', NULL, 'COMPLETE_DRAFT', 'DRAFT', 'COMPLETED', 'Draft hoàn thành trước khi convert.', TIMESTAMP '2026-07-05 14:52:00', '{}'::jsonb),
    ('62000000-0000-0000-0000-000000000018', '60000000-0000-0000-0000-000000000005', NULL, 'CONVERT_DRAFT', 'COMPLETED', 'CONVERTED', 'Draft đã convert sang loan application.', TIMESTAMP '2026-07-05 14:55:00', '{"converted_loan_application_id": "30000000-0000-0000-0000-000000000001"}'::jsonb);
