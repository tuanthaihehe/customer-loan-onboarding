-- Customer Loan Onboarding - Seed Loan Application Draft Flow Demo
-- PostgreSQL dialect
-- Version: V13 seed
--
-- Scope:
-- - Seed demo data for loan_application_draft.
-- - Seed one payload per step in loan_application_draft_step_data.
-- - Seed loan_application_draft_history to show how a draft moves through steps.
--
-- This file is idempotent and can be run multiple times safely.
-- It must be run after:
-- - V12__seed_loan_application_step.sql
-- - V2__seed_demo_business_data.sql
-- - V3__seed_vehicle_catalog_and_asset.sql
-- - V5/V6/V7/V8/V10/V11 catalog seeds.

-- =========================================================
-- 1. Demo draft containers
-- =========================================================
-- If a previous run failed halfway, child rows may still point to auto-generated
-- draft ids from older versions of this seed. Remove only this demo dataset first.

DELETE FROM loan_application_draft_history h
USING loan_application_draft d
WHERE h.draft_id = d.id
  AND d.draft_code IN (
      'DRF-2026-000001',
      'DRF-2026-000002',
      'DRF-2026-000003',
      'DRF-2026-000004',
      'DRF-2026-000005'
  );

DELETE FROM loan_application_draft_step_data sd
USING loan_application_draft d
WHERE sd.draft_id = d.id
  AND d.draft_code IN (
      'DRF-2026-000001',
      'DRF-2026-000002',
      'DRF-2026-000003',
      'DRF-2026-000004',
      'DRF-2026-000005'
  );

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
        '50000000-0000-0000-0000-000000000001',
        'DRF-2026-000001',
        '10000000-0000-0000-0000-000000000001',
        'PRELIMINARY_INFO',
        'DRAFT',
        NULL,
        TIMESTAMP '2026-07-31 23:59:59',
        TIMESTAMP '2026-07-01 09:00:00',
        TIMESTAMP '2026-07-01 09:18:00'
    ),
    (
        '50000000-0000-0000-0000-000000000002',
        'DRF-2026-000002',
        '10000000-0000-0000-0000-000000000002',
        'FINAL_LOAN_PROPOSAL',
        'DRAFT',
        NULL,
        TIMESTAMP '2026-07-31 23:59:59',
        TIMESTAMP '2026-07-01 10:00:00',
        TIMESTAMP '2026-07-01 11:15:00'
    ),
    (
        '50000000-0000-0000-0000-000000000003',
        'DRF-2026-000003',
        '10000000-0000-0000-0000-000000000005',
        'UPLOAD_COMPLETE',
        'COMPLETED',
        NULL,
        TIMESTAMP '2026-07-31 23:59:59',
        TIMESTAMP '2026-07-02 08:30:00',
        TIMESTAMP '2026-07-02 09:42:00'
    ),
    (
        '50000000-0000-0000-0000-000000000004',
        'DRF-2026-000004',
        '10000000-0000-0000-0000-000000000001',
        'UPLOAD_COMPLETE',
        'CONVERTED',
        '30000000-0000-0000-0000-000000000001',
        TIMESTAMP '2026-07-31 23:59:59',
        TIMESTAMP '2026-07-02 13:00:00',
        TIMESTAMP '2026-07-02 14:10:00'
    ),
    (
        '50000000-0000-0000-0000-000000000005',
        'DRF-2026-000005',
        '10000000-0000-0000-0000-000000000004',
        'CUSTOMER_DETAIL',
        'CANCELLED',
        NULL,
        TIMESTAMP '2026-07-31 23:59:59',
        TIMESTAMP '2026-07-02 15:00:00',
        TIMESTAMP '2026-07-02 16:20:00'
    )
ON CONFLICT (draft_code) DO UPDATE
SET
    id = EXCLUDED.id,
    customer_id = EXCLUDED.customer_id,
    current_step_code = EXCLUDED.current_step_code,
    status = EXCLUDED.status,
    converted_loan_application_id = EXCLUDED.converted_loan_application_id,
    expired_at = EXCLUDED.expired_at,
    updated_at = EXCLUDED.updated_at;

-- =========================================================
-- 2. Step payloads
-- =========================================================
-- Payload convention:
-- - Keep catalog codes because API payloads should be stable across environments.
-- - Keep display snapshots so frontend can render a clicked draft without joining every catalog.
-- - Keep validation/result blocks so tests can assert step status and invalidation behavior.

INSERT INTO loan_application_draft_step_data (
    id,
    draft_id,
    step_code,
    status,
    payload,
    completed_at,
    invalidated_at,
    invalidated_reason,
    created_at,
    updated_at
)
VALUES
    -- DRF-2026-000001: early draft, customer identified, preliminary info in progress.
    (
        '51000000-0000-0000-0000-000000000001',
        '50000000-0000-0000-0000-000000000001',
        'CUSTOMER_IDENTIFY',
        'COMPLETED',
        '{
            "customer": {
                "customerId": "10000000-0000-0000-0000-000000000001",
                "customerCode": "CUS-2026-000001",
                "fullName": "Nguyễn Văn An",
                "phoneNumber": "0901000001",
                "identityNumber": "001201000001",
                "dateOfBirth": "1995-01-15",
                "status": "ACTIVE"
            },
            "search": {
                "searchType": "PHONE_NUMBER",
                "keyword": "0901000001",
                "matchedExistingCustomer": true
            },
            "kyc": {
                "blacklistCheckResult": false,
                "phoneOtpVerificationResult": true,
                "checkedAt": "2026-07-01T09:05:00"
            }
        }'::jsonb,
        TIMESTAMP '2026-07-01 09:08:00',
        NULL,
        NULL,
        TIMESTAMP '2026-07-01 09:00:00',
        TIMESTAMP '2026-07-01 09:08:00'
    ),
    (
        '51000000-0000-0000-0000-000000000002',
        '50000000-0000-0000-0000-000000000001',
        'PRELIMINARY_INFO',
        'IN_PROGRESS',
        '{
            "loanRequest": {
                "loanPurposeCode": "PERSONAL_CONSUMPTION",
                "loanPurposeName": "Tiêu dùng cá nhân",
                "requestedAmount": 12000000,
                "termCode": "TERM_12M",
                "termMonths": 12,
                "expectedDisbursementDate": "2026-07-05"
            },
            "assetPreview": {
                "vehicleTypeCode": "MOTORBIKE",
                "vehicleBrandCode": "YAMAHA",
                "vehicleModelCode": "EXCITER_155",
                "licensePlate": "29A12345"
            },
            "progress": {
                "lastSavedField": "expectedDisbursementDate",
                "canContinue": true
            }
        }'::jsonb,
        NULL,
        NULL,
        NULL,
        TIMESTAMP '2026-07-01 09:10:00',
        TIMESTAMP '2026-07-01 09:18:00'
    ),
    (
        '51000000-0000-0000-0000-000000000003',
        '50000000-0000-0000-0000-000000000001',
        'CUSTOMER_DETAIL',
        'NOT_STARTED',
        '{}'::jsonb,
        NULL,
        NULL,
        NULL,
        TIMESTAMP '2026-07-01 09:00:00',
        TIMESTAMP '2026-07-01 09:00:00'
    ),
    (
        '51000000-0000-0000-0000-000000000004',
        '50000000-0000-0000-0000-000000000001',
        'ASSET_DETAIL',
        'NOT_STARTED',
        '{}'::jsonb,
        NULL,
        NULL,
        NULL,
        TIMESTAMP '2026-07-01 09:00:00',
        TIMESTAMP '2026-07-01 09:00:00'
    ),
    (
        '51000000-0000-0000-0000-000000000005',
        '50000000-0000-0000-0000-000000000001',
        'FINAL_LOAN_PROPOSAL',
        'NOT_STARTED',
        '{"lockedByStepCodes": ["CUSTOMER_DETAIL", "ASSET_DETAIL"]}'::jsonb,
        NULL,
        NULL,
        NULL,
        TIMESTAMP '2026-07-01 09:00:00',
        TIMESTAMP '2026-07-01 09:00:00'
    ),
    (
        '51000000-0000-0000-0000-000000000006',
        '50000000-0000-0000-0000-000000000001',
        'UPLOAD_COMPLETE',
        'NOT_STARTED',
        '{"lockedByStepCodes": ["FINAL_LOAN_PROPOSAL"]}'::jsonb,
        NULL,
        NULL,
        NULL,
        TIMESTAMP '2026-07-01 09:00:00',
        TIMESTAMP '2026-07-01 09:00:00'
    ),

    -- DRF-2026-000002: near final proposal, asset step invalidated after amount changed.
    (
        '51000000-0000-0000-0000-000000000007',
        '50000000-0000-0000-0000-000000000002',
        'CUSTOMER_IDENTIFY',
        'COMPLETED',
        '{
            "customer": {
                "customerId": "10000000-0000-0000-0000-000000000002",
                "customerCode": "CUS-2026-000002",
                "fullName": "Trần Thị Bình",
                "phoneNumber": "0901000002",
                "identityNumber": "001201000002",
                "dateOfBirth": "1992-08-20",
                "status": "ACTIVE"
            },
            "search": {
                "searchType": "IDENTITY_NUMBER",
                "keyword": "001201000002",
                "matchedExistingCustomer": true
            },
            "kyc": {
                "blacklistCheckResult": false,
                "phoneOtpVerificationResult": true,
                "faceMatchScore": 91.60,
                "livenessDetectionScore": 94.20,
                "faceAuthenticityScore": 96.10
            }
        }'::jsonb,
        TIMESTAMP '2026-07-01 10:06:00',
        NULL,
        NULL,
        TIMESTAMP '2026-07-01 10:00:00',
        TIMESTAMP '2026-07-01 10:06:00'
    ),
    (
        '51000000-0000-0000-0000-000000000008',
        '50000000-0000-0000-0000-000000000002',
        'PRELIMINARY_INFO',
        'COMPLETED',
        '{
            "loanRequest": {
                "loanPurposeCode": "PERSONAL_CONSUMPTION",
                "loanPurposeName": "Tiêu dùng cá nhân",
                "requestedAmount": 65000000,
                "termCode": "TERM_24M",
                "termMonths": 24
            },
            "selectedAssetIntent": {
                "vehicleTypeCode": "MOTORBIKE",
                "vehicleVariantCode": "HONDA_SH_150_ABS_2022_BLACK"
            },
            "changeNote": "Khách đổi số tiền vay từ 60 triệu lên 65 triệu."
        }'::jsonb,
        TIMESTAMP '2026-07-01 10:18:00',
        NULL,
        NULL,
        TIMESTAMP '2026-07-01 10:08:00',
        TIMESTAMP '2026-07-01 11:12:00'
    ),
    (
        '51000000-0000-0000-0000-000000000009',
        '50000000-0000-0000-0000-000000000002',
        'CUSTOMER_DETAIL',
        'COMPLETED',
        '{
            "customerProfile": {
                "gender": "FEMALE",
                "email": "binh.tran.demo@example.com",
                "maritalStatus": "MARRIED",
                "permanentAddress": "12 Nguyễn Trãi, Thanh Xuân, Hà Nội",
                "currentAddress": "12 Nguyễn Trãi, Thanh Xuân, Hà Nội"
            },
            "employment": {
                "occupationCode": "BUSINESS_OWNER",
                "occupationName": "Chủ kinh doanh",
                "incomeSourceCode": "BUSINESS",
                "monthlyIncomeAmount": 28000000,
                "workplaceName": "Cửa hàng Bình Minh",
                "workplaceAddress": "Cầu Giấy, Hà Nội"
            },
            "disbursement": {
                "bankCode": "TCB",
                "bankShortName": "Techcombank",
                "accountNumber": "1903666888999",
                "accountName": "TRAN THI BINH"
            }
        }'::jsonb,
        TIMESTAMP '2026-07-01 10:35:00',
        NULL,
        NULL,
        TIMESTAMP '2026-07-01 10:20:00',
        TIMESTAMP '2026-07-01 10:35:00'
    ),
    (
        '51000000-0000-0000-0000-000000000010',
        '50000000-0000-0000-0000-000000000002',
        'ASSET_DETAIL',
        'INVALIDATED',
        '{
            "asset": {
                "assetCode": "AST-2026-000002",
                "vehicleVariantCode": "HONDA_SH_150_ABS_2022_BLACK",
                "vehicleName": "Honda SH 150 ABS 2022 Đen",
                "licensePlate": "30B67890",
                "frameNumber": "RLHSH1502022DEMO01",
                "engineNumber": "SH150E2022DEMO01",
                "registrationCertificateNumber": "DKX-2022-000002",
                "registrationIssueDate": "2022-04-15"
            },
            "valuation": {
                "marketPriceAmount": 88000000,
                "deductions": [
                    {
                        "deductionTypeCode": "SCRATCHED_BODY",
                        "deductionAmount": 1500000
                    }
                ],
                "totalDeductionAmount": 1500000,
                "finalValueAmount": 86500000,
                "maxLtvPercent": 70.00,
                "maxEligibleAmount": 60550000
            },
            "validation": {
                "previousRequestedAmount": 60000000,
                "currentRequestedAmount": 65000000,
                "reason": "Số tiền vay thay đổi vượt hạn mức LTV đã tính."
            }
        }'::jsonb,
        TIMESTAMP '2026-07-01 10:55:00',
        TIMESTAMP '2026-07-01 11:13:00',
        'PRELIMINARY_INFO requestedAmount changed from 60000000 to 65000000.',
        TIMESTAMP '2026-07-01 10:38:00',
        TIMESTAMP '2026-07-01 11:13:00'
    ),
    (
        '51000000-0000-0000-0000-000000000011',
        '50000000-0000-0000-0000-000000000002',
        'FINAL_LOAN_PROPOSAL',
        'IN_PROGRESS',
        '{
            "proposal": {
                "selectedProductCode": "XM_FLEX",
                "selectedProductName": "Xe máy linh hoạt",
                "requestedAmount": 65000000,
                "termCode": "TERM_24M",
                "termMonths": 24,
                "monthlyInterestRatePercent": 2.90,
                "requiresAssetRevaluation": true
            },
            "availableProducts": [
                {
                    "productCode": "XM_FLEX",
                    "eligible": false,
                    "reason": "REQUESTED_AMOUNT_EXCEEDS_MAX_ELIGIBLE_AMOUNT"
                },
                {
                    "productCode": "XM_HIGH_LIMIT",
                    "eligible": true,
                    "maxLtvPercent": 85.00
                }
            ]
        }'::jsonb,
        NULL,
        NULL,
        NULL,
        TIMESTAMP '2026-07-01 11:14:00',
        TIMESTAMP '2026-07-01 11:15:00'
    ),
    (
        '51000000-0000-0000-0000-000000000012',
        '50000000-0000-0000-0000-000000000002',
        'UPLOAD_COMPLETE',
        'NOT_STARTED',
        '{"lockedByStepCodes": ["ASSET_DETAIL", "FINAL_LOAN_PROPOSAL"]}'::jsonb,
        NULL,
        NULL,
        NULL,
        TIMESTAMP '2026-07-01 10:00:00',
        TIMESTAMP '2026-07-01 11:15:00'
    ),

    -- DRF-2026-000003: completed draft, ready for conversion.
    (
        '51000000-0000-0000-0000-000000000013',
        '50000000-0000-0000-0000-000000000003',
        'CUSTOMER_IDENTIFY',
        'COMPLETED',
        '{"customer": {"customerId": "10000000-0000-0000-0000-000000000005", "customerCode": "CUS-2026-000005", "fullName": "Hoàng Đức Huy", "phoneNumber": "0901000005", "identityNumber": "001201000005", "status": "ACTIVE"}, "kyc": {"blacklistCheckResult": false, "phoneOtpVerificationResult": true, "faceMatchScore": 95.20}}'::jsonb,
        TIMESTAMP '2026-07-02 08:36:00',
        NULL,
        NULL,
        TIMESTAMP '2026-07-02 08:30:00',
        TIMESTAMP '2026-07-02 08:36:00'
    ),
    (
        '51000000-0000-0000-0000-000000000014',
        '50000000-0000-0000-0000-000000000003',
        'PRELIMINARY_INFO',
        'COMPLETED',
        '{"loanRequest": {"loanPurposeCode": "PERSONAL_CONSUMPTION", "requestedAmount": 18000000, "termCode": "TERM_18M", "termMonths": 18}, "selectedAssetIntent": {"vehicleTypeCode": "MOTORBIKE", "vehicleVariantCode": "YAMAHA_EXCITER_155_ABS_2023_BLUE"}}'::jsonb,
        TIMESTAMP '2026-07-02 08:45:00',
        NULL,
        NULL,
        TIMESTAMP '2026-07-02 08:38:00',
        TIMESTAMP '2026-07-02 08:45:00'
    ),
    (
        '51000000-0000-0000-0000-000000000015',
        '50000000-0000-0000-0000-000000000003',
        'CUSTOMER_DETAIL',
        'COMPLETED',
        '{"customerProfile": {"gender": "MALE", "email": "huy.hoang.demo@example.com", "maritalStatus": "SINGLE", "permanentAddress": "88 Lê Lợi, Quận 1, TP.HCM", "currentAddress": "88 Lê Lợi, Quận 1, TP.HCM"}, "employment": {"occupationCode": "OFFICE_WORKER", "incomeSourceCode": "SALARY", "monthlyIncomeAmount": 22000000, "workplaceName": "Công ty Demo Finance"}, "disbursement": {"bankCode": "VCB", "accountNumber": "0011000888999", "accountName": "HOANG DUC HUY"}}'::jsonb,
        TIMESTAMP '2026-07-02 09:00:00',
        NULL,
        NULL,
        TIMESTAMP '2026-07-02 08:46:00',
        TIMESTAMP '2026-07-02 09:00:00'
    ),
    (
        '51000000-0000-0000-0000-000000000016',
        '50000000-0000-0000-0000-000000000003',
        'ASSET_DETAIL',
        'COMPLETED',
        '{"asset": {"assetCode": "AST-DRAFT-000003", "vehicleVariantCode": "YAMAHA_EXCITER_155_ABS_2023_BLUE", "vehicleName": "Yamaha Exciter 155 ABS 2023 Xanh", "licensePlate": "59D112345", "frameNumber": "RLCUG0610PYDEMO03", "engineNumber": "G3D4EDEMO03", "registrationCertificateNumber": "DKX-2023-000003", "registrationIssueDate": "2023-05-22"}, "valuation": {"marketPriceAmount": 45000000, "totalDeductionAmount": 2500000, "finalValueAmount": 42500000, "maxLtvPercent": 75.00, "maxEligibleAmount": 31875000}}'::jsonb,
        TIMESTAMP '2026-07-02 09:18:00',
        NULL,
        NULL,
        TIMESTAMP '2026-07-02 09:03:00',
        TIMESTAMP '2026-07-02 09:18:00'
    ),
    (
        '51000000-0000-0000-0000-000000000017',
        '50000000-0000-0000-0000-000000000003',
        'FINAL_LOAN_PROPOSAL',
        'COMPLETED',
        '{"proposal": {"selectedProductCode": "XM_PREFER", "selectedProductName": "Xe máy ưu đãi", "approvedAmount": 18000000, "termCode": "TERM_18M", "termMonths": 18, "monthlyInterestRatePercent": 2.80, "ltvPercent": 42.35}, "repaymentPreview": {"estimatedMonthlyPayment": 1552000, "currencyCode": "VND"}}'::jsonb,
        TIMESTAMP '2026-07-02 09:30:00',
        NULL,
        NULL,
        TIMESTAMP '2026-07-02 09:20:00',
        TIMESTAMP '2026-07-02 09:30:00'
    ),
    (
        '51000000-0000-0000-0000-000000000018',
        '50000000-0000-0000-0000-000000000003',
        'UPLOAD_COMPLETE',
        'COMPLETED',
        '{"documents": [{"documentTypeCode": "CITIZEN_ID_FRONT", "fileName": "huy_cccd_front.jpg", "fileUrl": "https://demo.local/files/drf-2026-000003/huy_cccd_front.jpg"}, {"documentTypeCode": "CITIZEN_ID_BACK", "fileName": "huy_cccd_back.jpg", "fileUrl": "https://demo.local/files/drf-2026-000003/huy_cccd_back.jpg"}, {"documentTypeCode": "VEHICLE_REGISTRATION_FRONT", "fileName": "exciter_cavet_front.jpg", "fileUrl": "https://demo.local/files/drf-2026-000003/exciter_cavet_front.jpg"}, {"documentTypeCode": "ASSET_FRONT_IMAGE", "fileName": "exciter_front.jpg", "fileUrl": "https://demo.local/files/drf-2026-000003/exciter_front.jpg"}], "checklist": {"requiredDocumentCount": 12, "uploadedRequiredDocumentCount": 12, "canCompleteDraft": true}}'::jsonb,
        TIMESTAMP '2026-07-02 09:41:00',
        NULL,
        NULL,
        TIMESTAMP '2026-07-02 09:32:00',
        TIMESTAMP '2026-07-02 09:41:00'
    ),

    -- DRF-2026-000004: converted draft. Uses the already seeded loan_application APP-2026-000001.
    (
        '51000000-0000-0000-0000-000000000019',
        '50000000-0000-0000-0000-000000000004',
        'CUSTOMER_IDENTIFY',
        'COMPLETED',
        '{"customer": {"customerId": "10000000-0000-0000-0000-000000000001", "customerCode": "CUS-2026-000001", "fullName": "Nguyễn Văn An", "phoneNumber": "0901000001", "identityNumber": "001201000001", "status": "ACTIVE"}, "convertedLoanApplicationCode": "APP-2026-000001"}'::jsonb,
        TIMESTAMP '2026-07-02 13:05:00',
        NULL,
        NULL,
        TIMESTAMP '2026-07-02 13:00:00',
        TIMESTAMP '2026-07-02 13:05:00'
    ),
    (
        '51000000-0000-0000-0000-000000000020',
        '50000000-0000-0000-0000-000000000004',
        'PRELIMINARY_INFO',
        'COMPLETED',
        '{"loanRequest": {"loanPurposeCode": "PERSONAL_CONSUMPTION", "requestedAmount": 50000000, "termCode": "TERM_24M", "termMonths": 24}}'::jsonb,
        TIMESTAMP '2026-07-02 13:14:00',
        NULL,
        NULL,
        TIMESTAMP '2026-07-02 13:06:00',
        TIMESTAMP '2026-07-02 13:14:00'
    ),
    (
        '51000000-0000-0000-0000-000000000021',
        '50000000-0000-0000-0000-000000000004',
        'CUSTOMER_DETAIL',
        'COMPLETED',
        '{"customerProfile": {"gender": "MALE", "email": "an.nguyen.demo@example.com", "maritalStatus": "MARRIED", "currentAddress": "24 Trần Duy Hưng, Cầu Giấy, Hà Nội"}, "employment": {"occupationCode": "SALES_STAFF", "incomeSourceCode": "COMMISSION", "monthlyIncomeAmount": 30000000}, "disbursement": {"bankCode": "MB", "accountNumber": "0888999000111", "accountName": "NGUYEN VAN AN"}}'::jsonb,
        TIMESTAMP '2026-07-02 13:28:00',
        NULL,
        NULL,
        TIMESTAMP '2026-07-02 13:15:00',
        TIMESTAMP '2026-07-02 13:28:00'
    ),
    (
        '51000000-0000-0000-0000-000000000022',
        '50000000-0000-0000-0000-000000000004',
        'ASSET_DETAIL',
        'COMPLETED',
        '{"asset": {"assetCode": "AST-CONVERTED-000004", "vehicleVariantCode": "TOYOTA_VIOS_G_CVT_2020_WHITE", "vehicleName": "Toyota Vios G CVT 2020 Trắng", "licensePlate": "30A99999"}, "valuation": {"marketPriceAmount": 300000000, "totalDeductionAmount": 12000000, "finalValueAmount": 288000000, "maxLtvPercent": 70.00, "maxEligibleAmount": 201600000}}'::jsonb,
        TIMESTAMP '2026-07-02 13:42:00',
        NULL,
        NULL,
        TIMESTAMP '2026-07-02 13:30:00',
        TIMESTAMP '2026-07-02 13:42:00'
    ),
    (
        '51000000-0000-0000-0000-000000000023',
        '50000000-0000-0000-0000-000000000004',
        'FINAL_LOAN_PROPOSAL',
        'COMPLETED',
        '{"proposal": {"selectedProductCode": "CAR_STANDARD", "selectedProductName": "Ô tô tiêu chuẩn", "approvedAmount": 50000000, "termCode": "TERM_24M", "termMonths": 24, "monthlyInterestRatePercent": 2.50}}'::jsonb,
        TIMESTAMP '2026-07-02 13:55:00',
        NULL,
        NULL,
        TIMESTAMP '2026-07-02 13:44:00',
        TIMESTAMP '2026-07-02 13:55:00'
    ),
    (
        '51000000-0000-0000-0000-000000000024',
        '50000000-0000-0000-0000-000000000004',
        'UPLOAD_COMPLETE',
        'COMPLETED',
        '{"documents": [{"documentTypeCode": "CITIZEN_ID_FRONT", "fileName": "an_cccd_front.jpg", "fileUrl": "https://demo.local/files/drf-2026-000004/an_cccd_front.jpg"}, {"documentTypeCode": "VEHICLE_REGISTRATION_FRONT", "fileName": "vios_cavet_front.jpg", "fileUrl": "https://demo.local/files/drf-2026-000004/vios_cavet_front.jpg"}], "conversion": {"convertedLoanApplicationId": "30000000-0000-0000-0000-000000000001", "convertedLoanApplicationCode": "APP-2026-000001", "convertedAt": "2026-07-02T14:10:00"}}'::jsonb,
        TIMESTAMP '2026-07-02 14:04:00',
        NULL,
        NULL,
        TIMESTAMP '2026-07-02 13:56:00',
        TIMESTAMP '2026-07-02 14:04:00'
    ),

    -- DRF-2026-000005: cancelled draft.
    (
        '51000000-0000-0000-0000-000000000025',
        '50000000-0000-0000-0000-000000000005',
        'CUSTOMER_IDENTIFY',
        'COMPLETED',
        '{"customer": {"customerId": "10000000-0000-0000-0000-000000000004", "customerCode": "CUS-2026-000004", "fullName": "Phạm Thu Dung", "phoneNumber": "0901000004", "identityNumber": "001201000004", "status": "INACTIVE"}, "kyc": {"blacklistCheckResult": false, "phoneOtpVerificationResult": false}}'::jsonb,
        TIMESTAMP '2026-07-02 15:08:00',
        NULL,
        NULL,
        TIMESTAMP '2026-07-02 15:00:00',
        TIMESTAMP '2026-07-02 15:08:00'
    ),
    (
        '51000000-0000-0000-0000-000000000026',
        '50000000-0000-0000-0000-000000000005',
        'PRELIMINARY_INFO',
        'COMPLETED',
        '{"loanRequest": {"loanPurposeCode": "OTHER", "requestedAmount": 10000000, "termCode": "TERM_6M", "termMonths": 6}, "note": "Khách chưa chắc chắn nhu cầu vay."}'::jsonb,
        TIMESTAMP '2026-07-02 15:20:00',
        NULL,
        NULL,
        TIMESTAMP '2026-07-02 15:10:00',
        TIMESTAMP '2026-07-02 15:20:00'
    ),
    (
        '51000000-0000-0000-0000-000000000027',
        '50000000-0000-0000-0000-000000000005',
        'CUSTOMER_DETAIL',
        'IN_PROGRESS',
        '{"customerProfile": {"gender": "FEMALE", "maritalStatus": "SINGLE"}, "employment": {"occupationCode": "OTHER"}, "progress": {"lastSavedField": "occupationCode", "canContinue": false}}'::jsonb,
        NULL,
        NULL,
        NULL,
        TIMESTAMP '2026-07-02 15:25:00',
        TIMESTAMP '2026-07-02 15:40:00'
    ),
    (
        '51000000-0000-0000-0000-000000000028',
        '50000000-0000-0000-0000-000000000005',
        'ASSET_DETAIL',
        'NOT_STARTED',
        '{"lockedByStepCodes": ["CUSTOMER_DETAIL"]}'::jsonb,
        NULL,
        NULL,
        NULL,
        TIMESTAMP '2026-07-02 15:00:00',
        TIMESTAMP '2026-07-02 15:00:00'
    ),
    (
        '51000000-0000-0000-0000-000000000029',
        '50000000-0000-0000-0000-000000000005',
        'FINAL_LOAN_PROPOSAL',
        'NOT_STARTED',
        '{"lockedByStepCodes": ["ASSET_DETAIL"]}'::jsonb,
        NULL,
        NULL,
        NULL,
        TIMESTAMP '2026-07-02 15:00:00',
        TIMESTAMP '2026-07-02 15:00:00'
    ),
    (
        '51000000-0000-0000-0000-000000000030',
        '50000000-0000-0000-0000-000000000005',
        'UPLOAD_COMPLETE',
        'NOT_STARTED',
        '{"lockedByStepCodes": ["FINAL_LOAN_PROPOSAL"]}'::jsonb,
        NULL,
        NULL,
        NULL,
        TIMESTAMP '2026-07-02 15:00:00',
        TIMESTAMP '2026-07-02 15:00:00'
    )
ON CONFLICT (draft_id, step_code) DO UPDATE
SET
    status = EXCLUDED.status,
    payload = EXCLUDED.payload,
    completed_at = EXCLUDED.completed_at,
    invalidated_at = EXCLUDED.invalidated_at,
    invalidated_reason = EXCLUDED.invalidated_reason,
    updated_at = EXCLUDED.updated_at;

-- =========================================================
-- 3. Draft history
-- =========================================================

INSERT INTO loan_application_draft_history (
    id,
    draft_id,
    step_code,
    action,
    old_status,
    new_status,
    note,
    changed_at
)
VALUES
    ('52000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001', NULL, 'CREATE_DRAFT', NULL, 'DRAFT', 'Tạo draft từ khách hàng đã tồn tại.', TIMESTAMP '2026-07-01 09:00:00'),
    ('52000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000001', 'CUSTOMER_IDENTIFY', 'START_STEP', 'NOT_STARTED', 'IN_PROGRESS', 'Frontend mở bước định danh.', TIMESTAMP '2026-07-01 09:01:00'),
    ('52000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000001', 'CUSTOMER_IDENTIFY', 'COMPLETE_STEP', 'IN_PROGRESS', 'COMPLETED', 'Định danh và OTP thành công.', TIMESTAMP '2026-07-01 09:08:00'),
    ('52000000-0000-0000-0000-000000000004', '50000000-0000-0000-0000-000000000001', 'PRELIMINARY_INFO', 'SAVE_STEP', 'NOT_STARTED', 'IN_PROGRESS', 'Lưu nhu cầu vay sơ bộ.', TIMESTAMP '2026-07-01 09:18:00'),

    ('52000000-0000-0000-0000-000000000005', '50000000-0000-0000-0000-000000000002', NULL, 'CREATE_DRAFT', NULL, 'DRAFT', 'Tạo draft cho khách hàng Trần Thị Bình.', TIMESTAMP '2026-07-01 10:00:00'),
    ('52000000-0000-0000-0000-000000000006', '50000000-0000-0000-0000-000000000002', 'CUSTOMER_IDENTIFY', 'COMPLETE_STEP', 'IN_PROGRESS', 'COMPLETED', 'Hoàn tất định danh.', TIMESTAMP '2026-07-01 10:06:00'),
    ('52000000-0000-0000-0000-000000000007', '50000000-0000-0000-0000-000000000002', 'PRELIMINARY_INFO', 'COMPLETE_STEP', 'IN_PROGRESS', 'COMPLETED', 'Chốt nhu cầu ban đầu 60 triệu.', TIMESTAMP '2026-07-01 10:18:00'),
    ('52000000-0000-0000-0000-000000000008', '50000000-0000-0000-0000-000000000002', 'CUSTOMER_DETAIL', 'COMPLETE_STEP', 'IN_PROGRESS', 'COMPLETED', 'Bổ sung thông tin nghề nghiệp và giải ngân.', TIMESTAMP '2026-07-01 10:35:00'),
    ('52000000-0000-0000-0000-000000000009', '50000000-0000-0000-0000-000000000002', 'ASSET_DETAIL', 'COMPLETE_STEP', 'IN_PROGRESS', 'COMPLETED', 'Định giá xe theo số tiền vay 60 triệu.', TIMESTAMP '2026-07-01 10:55:00'),
    ('52000000-0000-0000-0000-000000000010', '50000000-0000-0000-0000-000000000002', 'PRELIMINARY_INFO', 'REOPEN_STEP', 'COMPLETED', 'IN_PROGRESS', 'Khách đổi số tiền vay.', TIMESTAMP '2026-07-01 11:10:00'),
    ('52000000-0000-0000-0000-000000000011', '50000000-0000-0000-0000-000000000002', 'PRELIMINARY_INFO', 'COMPLETE_STEP', 'IN_PROGRESS', 'COMPLETED', 'Cập nhật requestedAmount = 65000000.', TIMESTAMP '2026-07-01 11:12:00'),
    ('52000000-0000-0000-0000-000000000012', '50000000-0000-0000-0000-000000000002', 'ASSET_DETAIL', 'INVALIDATE_STEP', 'COMPLETED', 'INVALIDATED', 'Số tiền vay mới vượt hạn mức LTV đã tính.', TIMESTAMP '2026-07-01 11:13:00'),
    ('52000000-0000-0000-0000-000000000013', '50000000-0000-0000-0000-000000000002', 'FINAL_LOAN_PROPOSAL', 'SAVE_STEP', 'NOT_STARTED', 'IN_PROGRESS', 'Backend gợi ý đổi sang sản phẩm XM_HIGH_LIMIT hoặc giảm số tiền vay.', TIMESTAMP '2026-07-01 11:15:00'),

    ('52000000-0000-0000-0000-000000000014', '50000000-0000-0000-0000-000000000003', NULL, 'CREATE_DRAFT', NULL, 'DRAFT', 'Tạo draft đã đi đủ luồng để test hoàn tất.', TIMESTAMP '2026-07-02 08:30:00'),
    ('52000000-0000-0000-0000-000000000015', '50000000-0000-0000-0000-000000000003', 'CUSTOMER_IDENTIFY', 'COMPLETE_STEP', 'IN_PROGRESS', 'COMPLETED', 'Hoàn tất định danh.', TIMESTAMP '2026-07-02 08:36:00'),
    ('52000000-0000-0000-0000-000000000016', '50000000-0000-0000-0000-000000000003', 'PRELIMINARY_INFO', 'COMPLETE_STEP', 'IN_PROGRESS', 'COMPLETED', 'Hoàn tất thông tin sơ bộ.', TIMESTAMP '2026-07-02 08:45:00'),
    ('52000000-0000-0000-0000-000000000017', '50000000-0000-0000-0000-000000000003', 'CUSTOMER_DETAIL', 'COMPLETE_STEP', 'IN_PROGRESS', 'COMPLETED', 'Hoàn tất thông tin khách hàng.', TIMESTAMP '2026-07-02 09:00:00'),
    ('52000000-0000-0000-0000-000000000018', '50000000-0000-0000-0000-000000000003', 'ASSET_DETAIL', 'COMPLETE_STEP', 'IN_PROGRESS', 'COMPLETED', 'Hoàn tất tài sản và định giá.', TIMESTAMP '2026-07-02 09:18:00'),
    ('52000000-0000-0000-0000-000000000019', '50000000-0000-0000-0000-000000000003', 'FINAL_LOAN_PROPOSAL', 'COMPLETE_STEP', 'IN_PROGRESS', 'COMPLETED', 'Chốt đề xuất vay.', TIMESTAMP '2026-07-02 09:30:00'),
    ('52000000-0000-0000-0000-000000000020', '50000000-0000-0000-0000-000000000003', 'UPLOAD_COMPLETE', 'COMPLETE_STEP', 'IN_PROGRESS', 'COMPLETED', 'Upload đủ chứng từ bắt buộc.', TIMESTAMP '2026-07-02 09:41:00'),
    ('52000000-0000-0000-0000-000000000021', '50000000-0000-0000-0000-000000000003', NULL, 'COMPLETE_DRAFT', 'DRAFT', 'COMPLETED', 'Draft sẵn sàng convert sang loan_application.', TIMESTAMP '2026-07-02 09:42:00'),

    ('52000000-0000-0000-0000-000000000022', '50000000-0000-0000-0000-000000000004', NULL, 'CREATE_DRAFT', NULL, 'DRAFT', 'Tạo draft dùng để test convert.', TIMESTAMP '2026-07-02 13:00:00'),
    ('52000000-0000-0000-0000-000000000023', '50000000-0000-0000-0000-000000000004', 'CUSTOMER_IDENTIFY', 'COMPLETE_STEP', 'IN_PROGRESS', 'COMPLETED', 'Hoàn tất định danh.', TIMESTAMP '2026-07-02 13:05:00'),
    ('52000000-0000-0000-0000-000000000024', '50000000-0000-0000-0000-000000000004', 'UPLOAD_COMPLETE', 'COMPLETE_STEP', 'IN_PROGRESS', 'COMPLETED', 'Hoàn tất bước upload.', TIMESTAMP '2026-07-02 14:04:00'),
    ('52000000-0000-0000-0000-000000000025', '50000000-0000-0000-0000-000000000004', NULL, 'COMPLETE_DRAFT', 'DRAFT', 'COMPLETED', 'Hoàn tất draft trước convert.', TIMESTAMP '2026-07-02 14:05:00'),
    ('52000000-0000-0000-0000-000000000026', '50000000-0000-0000-0000-000000000004', NULL, 'CONVERT_DRAFT', 'COMPLETED', 'CONVERTED', 'Convert thành APP-2026-000001.', TIMESTAMP '2026-07-02 14:10:00'),

    ('52000000-0000-0000-0000-000000000027', '50000000-0000-0000-0000-000000000005', NULL, 'CREATE_DRAFT', NULL, 'DRAFT', 'Tạo draft cho khách inactive.', TIMESTAMP '2026-07-02 15:00:00'),
    ('52000000-0000-0000-0000-000000000028', '50000000-0000-0000-0000-000000000005', 'CUSTOMER_IDENTIFY', 'COMPLETE_STEP', 'IN_PROGRESS', 'COMPLETED', 'Định danh lưu lại nhưng OTP chưa đạt.', TIMESTAMP '2026-07-02 15:08:00'),
    ('52000000-0000-0000-0000-000000000029', '50000000-0000-0000-0000-000000000005', 'PRELIMINARY_INFO', 'COMPLETE_STEP', 'IN_PROGRESS', 'COMPLETED', 'Lưu nhu cầu vay 10 triệu.', TIMESTAMP '2026-07-02 15:20:00'),
    ('52000000-0000-0000-0000-000000000030', '50000000-0000-0000-0000-000000000005', 'CUSTOMER_DETAIL', 'SAVE_STEP', 'NOT_STARTED', 'IN_PROGRESS', 'Lưu dở thông tin khách hàng.', TIMESTAMP '2026-07-02 15:40:00'),
    ('52000000-0000-0000-0000-000000000031', '50000000-0000-0000-0000-000000000005', NULL, 'CANCEL_DRAFT', 'DRAFT', 'CANCELLED', 'Khách không tiếp tục nhu cầu vay.', TIMESTAMP '2026-07-02 16:20:00')
ON CONFLICT (id) DO UPDATE
SET
    draft_id = EXCLUDED.draft_id,
    step_code = EXCLUDED.step_code,
    action = EXCLUDED.action,
    old_status = EXCLUDED.old_status,
    new_status = EXCLUDED.new_status,
    note = EXCLUDED.note,
    changed_at = EXCLUDED.changed_at;
