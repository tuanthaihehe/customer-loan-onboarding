-- Customer Loan Onboarding - KYC, Income Source and Draft Flow Schema
-- PostgreSQL dialect
--
-- This backend Flyway migration folds the latest database design from:
-- - database/migrations/V18__add_kyc_profile.sql
-- - database/migrations/V19__update_loan_application_product_and_income_source.sql
-- - database/migrations/V20__add_registration_certificate_number_to_asset.sql
-- - database/migrations/V21__add_loan_application_draft_step_flow.sql
-- - database/migrations/V22__simplify_loan_application_draft_step_flow.sql
-- - database/migrations/V23__remove_unused_kyc_profile_fields.sql
-- - database/migrations/V24__refine_loan_application_draft_review_flow.sql
-- - database/seed/V11__seed_income_source.sql
-- - database/seed/V12__seed_loan_application_step.sql
-- - database/seed/V14__seed_loan_application_draft_review_flow.sql


-- =========================================================
-- Source: database/migrations/V18__add_kyc_profile.sql
-- =========================================================

-- Customer Loan Onboarding - Add KYC Profile
-- PostgreSQL dialect
-- Version: V18
--
-- Scope:
-- - Create kyc_profile table.
--
-- Business context:
-- - KYC profile records the latest identity verification and trust signals
--   for a customer during onboarding.
-- - It is created after customer is found/created and identification checks begin.
-- - loan_application may not exist yet at that time.
-- - When a loan application is created, the KYC profile can be linked to it.
--
-- Relationship:
-- - customer 1 - N kyc_profile
-- - loan_application 0/1 - 1 kyc_profile
--
-- Design notes:
-- - customer_id is NOT NULL because KYC verifies a specific customer.
-- - loan_application_id is NULLABLE because KYC can be created before the loan application.
-- - UNIQUE(loan_application_id) ensures one loan application can have at most one KYC profile.
-- - PostgreSQL allows multiple NULL values in a UNIQUE constraint, so multiple pre-application
--   KYC profiles are allowed before they are linked to loan applications.
-- - status and overall_score are intentionally not stored. Backend can derive them from
--   the raw check result fields.

CREATE TABLE kyc_profile (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    customer_id UUID NOT NULL
        REFERENCES customer(id),

    loan_application_id UUID
        REFERENCES loan_application(id) ON DELETE SET NULL,

    -- TRUE  = customer is in blacklist
    -- FALSE = customer is not in blacklist
    -- NULL  = not checked yet
    blacklist_check_result BOOLEAN,

    -- TRUE  = OTP verification passed
    -- FALSE = OTP verification failed
    -- NULL  = not verified yet
    phone_otp_verification_result BOOLEAN,

    -- Face eKYC scores from 0 to 100.
    -- Higher score means better trust/confidence.
    face_match_score NUMERIC(5, 2),
    liveness_detection_score NUMERIC(5, 2),
    face_authenticity_score NUMERIC(5, 2),

    checked_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    note TEXT,

    CONSTRAINT uq_kyc_profile_loan_application
        UNIQUE (loan_application_id),

    CONSTRAINT chk_kyc_face_match_score
        CHECK (
            face_match_score IS NULL
            OR (face_match_score >= 0 AND face_match_score <= 100)
        ),

    CONSTRAINT chk_kyc_liveness_detection_score
        CHECK (
            liveness_detection_score IS NULL
            OR (liveness_detection_score >= 0 AND liveness_detection_score <= 100)
        ),

    CONSTRAINT chk_kyc_face_authenticity_score
        CHECK (
            face_authenticity_score IS NULL
            OR (face_authenticity_score >= 0 AND face_authenticity_score <= 100)
        )
);

CREATE INDEX idx_kyc_profile_customer_id
ON kyc_profile(customer_id);

CREATE INDEX idx_kyc_profile_loan_application_id
ON kyc_profile(loan_application_id);


-- =========================================================
-- Source: database/migrations/V19__update_loan_application_product_and_income_source.sql
-- =========================================================

-- Customer Loan Onboarding - Update Loan Application Product and Income Source
-- PostgreSQL dialect
-- Version: V19
--
-- Scope:
-- - Remove duplicated applicant snapshot fields from loan_application.
-- - Add selected loan product reference to loan_application.
-- - Create income_source catalog.
-- - Add income_source_id reference to loan_application.
--
-- Design notes:
-- - Customer stable information should stay in customer.
-- - Application-specific financial/work information stays in loan_application.
-- - applicant_occupation and applicant_monthly_income are removed because they overlap
--   with occupation_id and monthly_income_amount.
-- - loan_product_id stores which loan product was selected for the application.
-- - income_source_id stores the applicant's declared source of income using a catalog.

ALTER TABLE loan_application
DROP COLUMN IF EXISTS applicant_full_name,
DROP COLUMN IF EXISTS applicant_identity_number,
DROP COLUMN IF EXISTS applicant_phone_number,
DROP COLUMN IF EXISTS applicant_date_of_birth,
DROP COLUMN IF EXISTS applicant_gender,
DROP COLUMN IF EXISTS applicant_occupation,
DROP COLUMN IF EXISTS applicant_monthly_income;

ALTER TABLE loan_application
ADD COLUMN IF NOT EXISTS loan_product_id UUID REFERENCES loan_product(id);

CREATE INDEX IF NOT EXISTS idx_loan_application_loan_product_id
ON loan_application(loan_product_id);

CREATE TABLE IF NOT EXISTS income_source (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,

    CONSTRAINT uq_income_source_code
        UNIQUE (code)
);

ALTER TABLE loan_application
ADD COLUMN IF NOT EXISTS income_source_id UUID REFERENCES income_source(id);

CREATE INDEX IF NOT EXISTS idx_loan_application_income_source_id
ON loan_application(income_source_id);


-- =========================================================
-- Source: database/migrations/V20__add_registration_certificate_number_to_asset.sql
-- =========================================================

-- Customer Loan Onboarding - Add Vehicle Registration Number to Asset
-- PostgreSQL dialect
-- Version: V20
--
-- Scope:
-- - Add vehicle registration certificate number to asset.
--
-- Business meaning:
-- - registration_certificate_number is the number/code printed on the vehicle
--   registration document, also commonly called the vehicle registration/cavet number.
-- - This is different from license_plate.
--
-- Design notes:
-- - Nullable because existing/draft asset records may not have this information yet.
-- - Unique nullable index prevents duplicate registration certificate numbers when provided.

ALTER TABLE asset
ADD COLUMN IF NOT EXISTS registration_certificate_number VARCHAR(100);

CREATE UNIQUE INDEX IF NOT EXISTS uq_asset_registration_certificate_number
ON asset(registration_certificate_number)
WHERE registration_certificate_number IS NOT NULL;


-- =========================================================
-- Source: database/migrations/V21__add_loan_application_draft_step_flow.sql
-- =========================================================

-- Customer Loan Onboarding - Add Loan Application Draft Step Flow
-- PostgreSQL dialect
-- Version: V21
--
-- Scope:
-- - Add a separate draft container for step-by-step loan onboarding.
-- - Keep real loan_application creation until the draft is completed and converted.
-- - Store each step payload separately as JSONB.
-- - Add draft history because the existing schema already tracks loan application lifecycle history.
--
-- Design notes:
-- - The existing customer.id and loan_application.id primary keys are UUID.
-- - Status values follow the current project style: VARCHAR with CHECK constraints.
-- - TIMESTAMP is used consistently with existing business tables.
-- - No staff/user table exists in the schema, so draft history does not include changed_by.
-- - No deleted_at column is added because soft delete is not a consistent convention in current tables.

CREATE TABLE IF NOT EXISTS loan_application_step (
    code VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    step_order INT NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_loan_application_step_order
        UNIQUE (step_order)
);

CREATE INDEX IF NOT EXISTS idx_loan_application_step_active_order
ON loan_application_step(is_active, step_order);

CREATE TABLE IF NOT EXISTS loan_application_draft (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    draft_code VARCHAR(50) NOT NULL,
    customer_id UUID NOT NULL REFERENCES customer(id),
    current_step_code VARCHAR(50) NOT NULL REFERENCES loan_application_step(code),

    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    converted_loan_application_id UUID REFERENCES loan_application(id),

    expired_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    converted_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_loan_application_draft_code
        UNIQUE (draft_code),

    CONSTRAINT chk_loan_application_draft_status
        CHECK (status IN (
            'DRAFT',
            'COMPLETED',
            'CONVERTED',
            'CANCELLED',
            'EXPIRED'
        ))
);

CREATE INDEX IF NOT EXISTS idx_loan_application_draft_customer_id
ON loan_application_draft(customer_id);

CREATE INDEX IF NOT EXISTS idx_loan_application_draft_status
ON loan_application_draft(status);

CREATE INDEX IF NOT EXISTS idx_loan_application_draft_current_step_code
ON loan_application_draft(current_step_code);

CREATE INDEX IF NOT EXISTS idx_loan_application_draft_converted_application_id
ON loan_application_draft(converted_loan_application_id);

CREATE TABLE IF NOT EXISTS loan_application_draft_step_data (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    draft_id UUID NOT NULL REFERENCES loan_application_draft(id) ON DELETE CASCADE,
    step_code VARCHAR(50) NOT NULL REFERENCES loan_application_step(code),

    status VARCHAR(30) NOT NULL DEFAULT 'NOT_STARTED',
    payload JSONB NOT NULL DEFAULT '{}'::jsonb,

    completed_at TIMESTAMP,
    invalidated_at TIMESTAMP,
    invalidated_reason TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_loan_application_draft_step_data
        UNIQUE (draft_id, step_code),

    CONSTRAINT chk_loan_application_draft_step_data_status
        CHECK (status IN (
            'NOT_STARTED',
            'IN_PROGRESS',
            'COMPLETED',
            'INVALIDATED',
            'LOCKED'
        ))
);

CREATE INDEX IF NOT EXISTS idx_loan_application_draft_step_data_draft_id
ON loan_application_draft_step_data(draft_id);

CREATE INDEX IF NOT EXISTS idx_loan_application_draft_step_data_step_code
ON loan_application_draft_step_data(step_code);

CREATE INDEX IF NOT EXISTS idx_loan_application_draft_step_data_status
ON loan_application_draft_step_data(status);

CREATE TABLE IF NOT EXISTS loan_application_draft_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    draft_id UUID NOT NULL REFERENCES loan_application_draft(id) ON DELETE CASCADE,
    step_code VARCHAR(50) REFERENCES loan_application_step(code),

    action VARCHAR(50) NOT NULL,
    old_status VARCHAR(30),
    new_status VARCHAR(30),
    note TEXT,

    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_loan_application_draft_history_action
        CHECK (action IN (
            'CREATE_DRAFT',
            'START_STEP',
            'SAVE_STEP',
            'COMPLETE_STEP',
            'REOPEN_STEP',
            'INVALIDATE_STEP',
            'CANCEL_DRAFT',
            'EXPIRE_DRAFT',
            'COMPLETE_DRAFT',
            'CONVERT_DRAFT'
        ))
);

CREATE INDEX IF NOT EXISTS idx_loan_application_draft_history_draft_id
ON loan_application_draft_history(draft_id);

CREATE INDEX IF NOT EXISTS idx_loan_application_draft_history_step_code
ON loan_application_draft_history(step_code);

CREATE INDEX IF NOT EXISTS idx_loan_application_draft_history_changed_at
ON loan_application_draft_history(changed_at);


-- =========================================================
-- Source: database/migrations/V22__simplify_loan_application_draft_step_flow.sql
-- =========================================================

-- Customer Loan Onboarding - Simplify Loan Application Draft Step Flow
-- PostgreSQL dialect
-- Version: V22
--
-- Scope:
-- - Remove persisted LOCKED step status because it can be derived from step order
--   and prerequisite step states in API responses.
-- - Remove duplicate draft terminal timestamps; audit timing is represented in
--   loan_application_draft_history.changed_at.
-- - Remove loan_application_draft_history.created_at because history rows already
--   store changed_at.

UPDATE loan_application_draft_step_data
SET
    status = 'NOT_STARTED',
    updated_at = CURRENT_TIMESTAMP
WHERE status = 'LOCKED';

ALTER TABLE loan_application_draft_step_data
DROP CONSTRAINT IF EXISTS chk_loan_application_draft_step_data_status;

ALTER TABLE loan_application_draft_step_data
ADD CONSTRAINT chk_loan_application_draft_step_data_status
CHECK (status IN (
    'NOT_STARTED',
    'IN_PROGRESS',
    'COMPLETED',
    'INVALIDATED'
));

ALTER TABLE loan_application_draft
DROP COLUMN IF EXISTS cancelled_at,
DROP COLUMN IF EXISTS converted_at;

ALTER TABLE loan_application_draft_history
DROP COLUMN IF EXISTS created_at;


-- =========================================================
-- Source: database/migrations/V23__remove_unused_kyc_profile_fields.sql
-- =========================================================

-- Customer Loan Onboarding - Remove unused KYC profile fields
-- PostgreSQL dialect
-- Version: V23
--
-- Scope:
-- - Remove KYC fields that are no longer captured/stored.

ALTER TABLE kyc_profile
    DROP CONSTRAINT IF EXISTS chk_kyc_face_authenticity_score;

ALTER TABLE kyc_profile
    DROP COLUMN IF EXISTS blacklist_check_result,
    DROP COLUMN IF EXISTS phone_otp_verification_result,
    DROP COLUMN IF EXISTS face_authenticity_score;


-- =========================================================
-- Source: database/migrations/V24__refine_loan_application_draft_review_flow.sql
-- =========================================================

-- Customer Loan Onboarding - Refine Loan Application Draft Review Flow
-- PostgreSQL dialect
-- Version: V24
--
-- Scope:
-- - Represent stale downstream step payloads with requires_review instead of
--   special step statuses.
-- - Keep downstream payload data intact when an earlier step changes.
-- - Restrict draft step status to workflow progress only:
--   NOT_STARTED, IN_PROGRESS, COMPLETED.
-- - Add history metadata for audit details such as changed step and affected step.

ALTER TABLE loan_application_draft_step_data
ADD COLUMN IF NOT EXISTS requires_review BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS invalidated_by_step_code VARCHAR(50),
ADD COLUMN IF NOT EXISTS invalidated_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS reviewed_at TIMESTAMP;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_loan_application_draft_step_data_invalidated_by_step'
          AND conrelid = 'loan_application_draft_step_data'::regclass
    ) THEN
        ALTER TABLE loan_application_draft_step_data
        ADD CONSTRAINT fk_loan_application_draft_step_data_invalidated_by_step
        FOREIGN KEY (invalidated_by_step_code)
        REFERENCES loan_application_step(code);
    END IF;
END $$;

UPDATE loan_application_draft_step_data
SET
    status = 'NOT_STARTED',
    updated_at = CURRENT_TIMESTAMP
WHERE status = 'LOCKED';

UPDATE loan_application_draft_step_data
SET
    status = 'COMPLETED',
    requires_review = TRUE,
    invalidated_at = COALESCE(invalidated_at, CURRENT_TIMESTAMP),
    updated_at = CURRENT_TIMESTAMP
WHERE status = 'INVALIDATED';

ALTER TABLE loan_application_draft_step_data
DROP CONSTRAINT IF EXISTS chk_loan_application_draft_step_data_status;

ALTER TABLE loan_application_draft_step_data
ADD CONSTRAINT chk_loan_application_draft_step_data_status
CHECK (status IN (
    'NOT_STARTED',
    'IN_PROGRESS',
    'COMPLETED'
));

CREATE INDEX IF NOT EXISTS idx_loan_application_draft_step_data_requires_review
ON loan_application_draft_step_data(draft_id, requires_review);

CREATE INDEX IF NOT EXISTS idx_loan_application_draft_step_data_invalidated_by
ON loan_application_draft_step_data(invalidated_by_step_code);

ALTER TABLE loan_application_draft_history
ADD COLUMN IF NOT EXISTS metadata JSONB NOT NULL DEFAULT '{}'::jsonb;


-- =========================================================
-- Source: database/seed/V11__seed_income_source.sql
-- =========================================================

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


-- =========================================================
-- Source: database/seed/V12__seed_loan_application_step.sql
-- =========================================================

-- Customer Loan Onboarding - Seed Loan Application Draft Steps
-- PostgreSQL dialect
-- Version: V12 seed
--
-- Scope:
-- - Seed the ordered step catalog used by loan_application_draft_step_data.
--
-- This file is idempotent and can be run multiple times safely.
-- It must be run after V21__add_loan_application_draft_step_flow.sql.

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
        'Ghi nhận nhu cầu vay, thông tin khoản vay sơ bộ và các lựa chọn ban đầu.',
        TRUE
    ),
    (
        'CUSTOMER_DETAIL',
        'Chi tiết khách hàng',
        3,
        'Bổ sung thông tin cá nhân, nghề nghiệp, thu nhập, địa chỉ và thông tin giải ngân.',
        TRUE
    ),
    (
        'ASSET_DETAIL',
        'Chi tiết tài sản',
        4,
        'Ghi nhận thông tin tài sản, giấy đăng ký, định giá và các yếu tố giảm trừ.',
        TRUE
    ),
    (
        'FINAL_LOAN_PROPOSAL',
        'Đề xuất gói vay cuối cùng',
        5,
        'Chốt sản phẩm vay, số tiền, kỳ hạn, lãi suất và đề xuất cuối cùng trước khi hoàn tất.',
        TRUE
    ),
    (
        'UPLOAD_COMPLETE',
        'Upload hồ sơ và hoàn tất',
        6,
        'Upload chứng từ, kiểm tra dữ liệu toàn bộ draft và đánh dấu hồ sơ sẵn sàng convert.',
        TRUE
    )
ON CONFLICT (code) DO UPDATE
SET
    name = EXCLUDED.name,
    step_order = EXCLUDED.step_order,
    description = EXCLUDED.description,
    is_active = EXCLUDED.is_active,
    updated_at = CURRENT_TIMESTAMP;


-- =========================================================
-- Source: database/seed/V14__seed_loan_application_draft_review_flow.sql
-- =========================================================

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

