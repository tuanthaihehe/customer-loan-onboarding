-- Customer Loan Onboarding - Application Profile, Mock Scoring and Document Schema
-- PostgreSQL dialect
--
-- This backend Flyway migration folds the latest database design from:
-- - database/migrations/V10__add_lead_customer_status.sql
-- - database/migrations/V12__add_loan_application_reference_person.sql
-- - database/migrations/V13_add_additional_customer_info_and_additional_loan_info.sql
-- - database/migrations/V14_add_extra_fields_customer_and_loan_application.sql
-- - database/migrations/V15__add_vehicle_identifier_fields_to_asset.sql
-- - database/migrations/V16__add_mock_score_grade_rule.sql
-- - database/migrations/V17__add_loan_application_document.sql
-- - database/seed/V8__seed_bank_and_occupation.sql
-- - database/seed/V9__seed_mock_score_grade_rule.sql
-- - database/seed/V10__seed_document_type.sql

-- =========================================================
-- 1. Customer status and profile fields
-- =========================================================

ALTER TABLE customer
DROP CONSTRAINT IF EXISTS chk_customer_status;

ALTER TABLE customer
ADD CONSTRAINT chk_customer_status
CHECK (status IN ('ACTIVE', 'INACTIVE', 'BLACKLIST', 'LEAD'));

ALTER TABLE customer
ADD COLUMN IF NOT EXISTS gender VARCHAR(20),
ADD COLUMN IF NOT EXISTS email VARCHAR(255),
ADD COLUMN IF NOT EXISTS marital_status VARCHAR(30),
ADD COLUMN IF NOT EXISTS permanent_address TEXT;

ALTER TABLE customer
DROP CONSTRAINT IF EXISTS chk_customer_gender;

ALTER TABLE customer
ADD CONSTRAINT chk_customer_gender
CHECK (
    gender IS NULL
    OR gender IN ('MALE', 'FEMALE')
);

ALTER TABLE customer
DROP CONSTRAINT IF EXISTS chk_customer_marital_status;

ALTER TABLE customer
ADD CONSTRAINT chk_customer_marital_status
CHECK (
    marital_status IS NULL
    OR marital_status IN ('SINGLE', 'MARRIED')
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_customer_email
ON customer(email)
WHERE email IS NOT NULL;

-- =========================================================
-- 2. Bank and occupation catalog
-- =========================================================

CREATE TABLE IF NOT EXISTS bank (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    short_name VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_bank_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS occupation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_occupation_code UNIQUE (code)
);

INSERT INTO bank (
    code,
    name,
    short_name,
    is_active,
    sort_order
)
VALUES
    ('VCB', 'Ngân hàng TMCP Ngoại thương Việt Nam', 'Vietcombank', TRUE, 10),
    ('BIDV', 'Ngân hàng TMCP Đầu tư và Phát triển Việt Nam', 'BIDV', TRUE, 20),
    ('CTG', 'Ngân hàng TMCP Công Thương Việt Nam', 'VietinBank', TRUE, 30),
    ('TCB', 'Ngân hàng TMCP Kỹ Thương Việt Nam', 'Techcombank', TRUE, 40),
    ('MB', 'Ngân hàng TMCP Quân đội', 'MB Bank', TRUE, 50),
    ('ACB', 'Ngân hàng TMCP Á Châu', 'ACB', TRUE, 60),
    ('VPB', 'Ngân hàng TMCP Việt Nam Thịnh Vượng', 'VPBank', TRUE, 70),
    ('TPB', 'Ngân hàng TMCP Tiên Phong', 'TPBank', TRUE, 80),
    ('VIB', 'Ngân hàng TMCP Quốc tế Việt Nam', 'VIB', TRUE, 90),
    ('STB', 'Ngân hàng TMCP Sài Gòn Thương Tín', 'Sacombank', TRUE, 100),
    ('SHB', 'Ngân hàng TMCP Sài Gòn - Hà Nội', 'SHB', TRUE, 110),
    ('EIB', 'Ngân hàng TMCP Xuất Nhập khẩu Việt Nam', 'Eximbank', TRUE, 120),
    ('HDB', 'Ngân hàng TMCP Phát triển Thành phố Hồ Chí Minh', 'HDBank', TRUE, 130),
    ('MSB', 'Ngân hàng TMCP Hàng Hải Việt Nam', 'MSB', TRUE, 140),
    ('OCB', 'Ngân hàng TMCP Phương Đông', 'OCB', TRUE, 150)
ON CONFLICT (code) DO UPDATE
SET
    name = EXCLUDED.name,
    short_name = EXCLUDED.short_name,
    is_active = EXCLUDED.is_active,
    sort_order = EXCLUDED.sort_order;

INSERT INTO occupation (
    code,
    name,
    description,
    is_active,
    sort_order
)
VALUES
    ('OFFICE_WORKER', 'Nhân viên văn phòng', 'Khách hàng làm việc tại văn phòng, công ty hoặc tổ chức.', TRUE, 10),
    ('BUSINESS_OWNER', 'Chủ kinh doanh', 'Khách hàng là chủ hộ kinh doanh, cửa hàng hoặc doanh nghiệp nhỏ.', TRUE, 20),
    ('SELF_EMPLOYED', 'Tự kinh doanh', 'Khách hàng tự kinh doanh hoặc làm việc độc lập có thu nhập tự khai.', TRUE, 30),
    ('FREELANCER', 'Lao động tự do', 'Khách hàng làm việc tự do, không có hợp đồng lao động cố định.', TRUE, 40),
    ('WORKER', 'Công nhân', 'Khách hàng làm công nhân tại nhà máy, xưởng hoặc khu công nghiệp.', TRUE, 50),
    ('DRIVER', 'Tài xế', 'Khách hàng làm tài xế công nghệ, tài xế taxi, xe tải hoặc vận tải.', TRUE, 60),
    ('SALES_STAFF', 'Nhân viên bán hàng', 'Khách hàng làm nhân viên bán hàng, tư vấn bán hàng hoặc kinh doanh.', TRUE, 70),
    ('SERVICE_STAFF', 'Nhân viên dịch vụ', 'Khách hàng làm trong lĩnh vực dịch vụ, nhà hàng, khách sạn hoặc chăm sóc khách hàng.', TRUE, 80),
    ('TEACHER', 'Giáo viên', 'Khách hàng làm giáo viên, giảng viên hoặc công việc đào tạo.', TRUE, 90),
    ('HEALTHCARE_WORKER', 'Nhân viên y tế', 'Khách hàng làm bác sĩ, y tá, điều dưỡng hoặc công việc y tế.', TRUE, 100),
    ('GOVERNMENT_EMPLOYEE', 'Cán bộ công chức', 'Khách hàng làm trong cơ quan nhà nước hoặc đơn vị hành chính sự nghiệp.', TRUE, 110),
    ('STUDENT', 'Sinh viên', 'Khách hàng đang là sinh viên hoặc học viên.', TRUE, 120),
    ('RETIRED', 'Đã nghỉ hưu', 'Khách hàng đã nghỉ hưu.', TRUE, 130),
    ('UNEMPLOYED', 'Không có việc làm', 'Khách hàng hiện chưa có việc làm.', TRUE, 140),
    ('OTHER', 'Khác', 'Nghề nghiệp khác không thuộc các nhóm trên.', TRUE, 999)
ON CONFLICT (code) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    is_active = EXCLUDED.is_active,
    sort_order = EXCLUDED.sort_order;

-- =========================================================
-- 3. Loan application additional fields
-- =========================================================

ALTER TABLE loan_application
ADD COLUMN IF NOT EXISTS occupation_id UUID REFERENCES occupation(id),
ADD COLUMN IF NOT EXISTS disbursement_bank_id UUID REFERENCES bank(id),
ADD COLUMN IF NOT EXISTS disbursement_account_number VARCHAR(50),
ADD COLUMN IF NOT EXISTS disbursement_account_name VARCHAR(255),
ADD COLUMN IF NOT EXISTS current_address TEXT,
ADD COLUMN IF NOT EXISTS workplace_name VARCHAR(255),
ADD COLUMN IF NOT EXISTS workplace_address TEXT,
ADD COLUMN IF NOT EXISTS monthly_income_amount NUMERIC(18, 2);

CREATE INDEX IF NOT EXISTS idx_loan_application_occupation_id
ON loan_application(occupation_id);

CREATE INDEX IF NOT EXISTS idx_loan_application_disbursement_bank_id
ON loan_application(disbursement_bank_id);

ALTER TABLE loan_application
DROP CONSTRAINT IF EXISTS chk_loan_application_monthly_income_amount;

ALTER TABLE loan_application
ADD CONSTRAINT chk_loan_application_monthly_income_amount
CHECK (
    monthly_income_amount IS NULL
    OR monthly_income_amount >= 0
);

CREATE TABLE IF NOT EXISTS loan_application_reference_person (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    loan_application_id UUID NOT NULL REFERENCES loan_application(id) ON DELETE CASCADE,
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    address TEXT,
    relationship_type VARCHAR(50) NOT NULL,
    note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_loan_application_reference_relationship_type
        CHECK (relationship_type IN (
            'FATHER',
            'MOTHER',
            'SPOUSE',
            'SIBLING',
            'RELATIVE',
            'FRIEND',
            'COLLEAGUE',
            'OTHER'
        )),
    CONSTRAINT uq_reference_phone_per_application
        UNIQUE (loan_application_id, phone_number)
);

CREATE INDEX IF NOT EXISTS idx_reference_person_loan_application_id
ON loan_application_reference_person(loan_application_id);

CREATE INDEX IF NOT EXISTS idx_reference_person_phone_number
ON loan_application_reference_person(phone_number);

CREATE INDEX IF NOT EXISTS idx_reference_person_relationship_type
ON loan_application_reference_person(relationship_type);

-- =========================================================
-- 4. Asset vehicle identifier fields
-- =========================================================

ALTER TABLE asset
ADD COLUMN IF NOT EXISTS frame_number VARCHAR(100),
ADD COLUMN IF NOT EXISTS engine_number VARCHAR(100),
ADD COLUMN IF NOT EXISTS registration_issue_date DATE;

CREATE UNIQUE INDEX IF NOT EXISTS uq_asset_frame_number
ON asset(frame_number)
WHERE frame_number IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_asset_engine_number
ON asset(engine_number)
WHERE engine_number IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_asset_registration_issue_date
ON asset(registration_issue_date);

-- =========================================================
-- 5. Mock score grade rule
-- =========================================================

CREATE TABLE IF NOT EXISTS mock_score_grade_rule (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_code VARCHAR(50) NOT NULL,
    rule_name VARCHAR(255) NOT NULL,
    description TEXT,
    min_monthly_income_amount NUMERIC(18, 2),
    max_monthly_income_amount NUMERIC(18, 2),
    min_requested_amount NUMERIC(18, 2),
    max_requested_amount NUMERIC(18, 2),
    min_ltv_percent NUMERIC(5, 2),
    max_ltv_percent NUMERIC(5, 2),
    score_grade_id UUID NOT NULL REFERENCES score_grade(id),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_mock_score_grade_rule_code
        UNIQUE (rule_code),
    CONSTRAINT chk_mock_score_income_range
        CHECK (
            min_monthly_income_amount IS NULL
            OR max_monthly_income_amount IS NULL
            OR min_monthly_income_amount <= max_monthly_income_amount
        ),
    CONSTRAINT chk_mock_score_requested_amount_range
        CHECK (
            min_requested_amount IS NULL
            OR max_requested_amount IS NULL
            OR min_requested_amount <= max_requested_amount
        ),
    CONSTRAINT chk_mock_score_ltv_range
        CHECK (
            min_ltv_percent IS NULL
            OR max_ltv_percent IS NULL
            OR min_ltv_percent <= max_ltv_percent
        )
);

CREATE INDEX IF NOT EXISTS idx_mock_score_grade_rule_score_grade_id
ON mock_score_grade_rule(score_grade_id);

CREATE INDEX IF NOT EXISTS idx_mock_score_grade_rule_active_sort
ON mock_score_grade_rule(is_active, sort_order);

INSERT INTO mock_score_grade_rule (
    rule_code,
    rule_name,
    description,
    min_monthly_income_amount,
    max_monthly_income_amount,
    min_requested_amount,
    max_requested_amount,
    min_ltv_percent,
    max_ltv_percent,
    score_grade_id,
    is_active,
    sort_order
)
SELECT
    v.rule_code,
    v.rule_name,
    v.description,
    v.min_monthly_income_amount,
    v.max_monthly_income_amount,
    v.min_requested_amount,
    v.max_requested_amount,
    v.min_ltv_percent,
    v.max_ltv_percent,
    sg.id,
    TRUE,
    v.sort_order
FROM (
    VALUES
        ('A_HIGH_INCOME_LOW_LTV_SMALL_LOAN', 'Hạng A - thu nhập cao, LTV thấp, khoản vay nhỏ', 'Thu nhập rất tốt, khoản vay nhỏ và tỷ lệ vay trên tài sản thấp.', 30000000::numeric, NULL::numeric, NULL::numeric, 10000000::numeric, NULL::numeric, 45.00::numeric, 'A', 10),
        ('A_HIGH_INCOME_LOW_LTV_MEDIUM_LOAN', 'Hạng A - thu nhập cao, LTV thấp, khoản vay trung bình', 'Thu nhập tốt, khoản vay trung bình và LTV thấp.', 25000000::numeric, NULL::numeric, 10000000::numeric, 25000000::numeric, NULL::numeric, 50.00::numeric, 'A', 20),
        ('A_VERY_HIGH_INCOME_MODERATE_LTV', 'Hạng A - thu nhập rất cao, LTV vừa', 'Thu nhập rất cao nên có thể chấp nhận LTV ở mức vừa.', 40000000::numeric, NULL::numeric, NULL::numeric, 40000000::numeric, 50.01::numeric, 60.00::numeric, 'A', 30),
        ('A_GOOD_INCOME_TINY_LOAN', 'Hạng A - thu nhập tốt, khoản vay rất nhỏ', 'Khoản vay rất nhỏ so với thu nhập.', 18000000::numeric, NULL::numeric, NULL::numeric, 7000000::numeric, NULL::numeric, 55.00::numeric, 'A', 40),
        ('B_GOOD_INCOME_LOW_LTV', 'Hạng B - thu nhập khá, LTV thấp', 'Thu nhập khá và tỷ lệ vay trên tài sản thấp.', 15000000::numeric, 29999999::numeric, NULL::numeric, 20000000::numeric, NULL::numeric, 50.00::numeric, 'B', 100),
        ('B_GOOD_INCOME_MEDIUM_LTV', 'Hạng B - thu nhập khá, LTV trung bình', 'Thu nhập khá và LTV trung bình.', 15000000::numeric, NULL::numeric, NULL::numeric, 30000000::numeric, 50.01::numeric, 65.00::numeric, 'B', 110),
        ('B_AVERAGE_INCOME_LOW_LTV', 'Hạng B - thu nhập trung bình, LTV thấp', 'Thu nhập trung bình nhưng LTV thấp.', 10000000::numeric, 14999999::numeric, NULL::numeric, 15000000::numeric, NULL::numeric, 50.00::numeric, 'B', 120),
        ('B_HIGH_INCOME_HIGHER_LOAN', 'Hạng B - thu nhập cao, khoản vay cao', 'Thu nhập cao, khoản vay lớn nhưng LTV vẫn kiểm soát được.', 25000000::numeric, NULL::numeric, 25000001::numeric, 50000000::numeric, NULL::numeric, 70.00::numeric, 'B', 130),
        ('B_STABLE_MEDIUM_LOAN', 'Hạng B - điều kiện ổn định, khoản vay vừa', 'Thu nhập và khoản vay ở mức ổn định.', 12000000::numeric, 24999999::numeric, 7000001::numeric, 20000000::numeric, 45.01::numeric, 65.00::numeric, 'B', 140),
        ('C_AVERAGE_INCOME_MEDIUM_LTV', 'Hạng C - thu nhập trung bình, LTV trung bình cao', 'Thu nhập trung bình và LTV tương đối cao.', 8000000::numeric, 14999999::numeric, NULL::numeric, 20000000::numeric, 50.01::numeric, 75.00::numeric, 'C', 200),
        ('C_LOW_INCOME_LOW_LTV', 'Hạng C - thu nhập thấp, LTV thấp', 'Thu nhập thấp nhưng tỷ lệ vay trên tài sản thấp.', 5000000::numeric, 7999999::numeric, NULL::numeric, 10000000::numeric, NULL::numeric, 50.00::numeric, 'C', 210),
        ('C_GOOD_INCOME_HIGH_LTV', 'Hạng C - thu nhập khá, LTV cao', 'Thu nhập khá nhưng LTV cao.', 12000000::numeric, NULL::numeric, NULL::numeric, 30000000::numeric, 65.01::numeric, 80.00::numeric, 'C', 220),
        ('C_MEDIUM_INCOME_MEDIUM_LOAN', 'Hạng C - thu nhập vừa, khoản vay vừa', 'Hồ sơ ở mức chấp nhận được.', 7000000::numeric, 11999999::numeric, 7000001::numeric, 18000000::numeric, 45.01::numeric, 70.00::numeric, 'C', 230),
        ('C_SMALL_LOAN_WEAK_INCOME', 'Hạng C - khoản vay nhỏ, thu nhập yếu', 'Thu nhập yếu nhưng khoản vay nhỏ.', 4000000::numeric, 6999999::numeric, NULL::numeric, 6000000::numeric, NULL::numeric, 60.00::numeric, 'C', 240),
        ('D_VERY_HIGH_LTV', 'Hạng D - LTV rất cao', 'Tỷ lệ vay trên tài sản rất cao.', NULL::numeric, NULL::numeric, NULL::numeric, NULL::numeric, 80.01::numeric, NULL::numeric, 'D', 300),
        ('D_LOW_INCOME_MEDIUM_LTV', 'Hạng D - thu nhập thấp, LTV trung bình cao', 'Thu nhập thấp và LTV không thấp.', NULL::numeric, 4999999::numeric, NULL::numeric, NULL::numeric, 50.01::numeric, NULL::numeric, 'D', 310),
        ('D_LOW_INCOME_HIGH_LOAN', 'Hạng D - thu nhập thấp, khoản vay cao', 'Khoản vay cao so với mức thu nhập thấp.', NULL::numeric, 7999999::numeric, 15000001::numeric, NULL::numeric, NULL::numeric, NULL::numeric, 'D', 320),
        ('D_UNKNOWN_LOW_INCOME', 'Hạng D - thu nhập rất thấp', 'Thu nhập rất thấp, đánh giá rủi ro cao.', NULL::numeric, 3999999::numeric, NULL::numeric, NULL::numeric, NULL::numeric, NULL::numeric, 'D', 330),
        ('D_LARGE_LOAN_MODERATE_INCOME', 'Hạng D - khoản vay lớn, thu nhập chưa tương xứng', 'Khoản vay lớn trong khi thu nhập chưa đủ mạnh.', NULL::numeric, 14999999::numeric, 30000001::numeric, NULL::numeric, NULL::numeric, NULL::numeric, 'D', 340),
        ('B_DEFAULT_STRONG_PROFILE', 'Hạng B - mặc định cho hồ sơ tốt', 'Rule mặc định cho hồ sơ có thu nhập tốt và LTV chưa cao.', 15000000::numeric, NULL::numeric, NULL::numeric, NULL::numeric, NULL::numeric, 70.00::numeric, 'B', 900),
        ('C_DEFAULT_ACCEPTABLE_PROFILE', 'Hạng C - mặc định cho hồ sơ chấp nhận được', 'Rule mặc định cho hồ sơ đủ dữ liệu nhưng không vào A/B/D.', 5000000::numeric, NULL::numeric, NULL::numeric, NULL::numeric, NULL::numeric, 80.00::numeric, 'C', 910),
        ('D_DEFAULT_RISK_PROFILE', 'Hạng D - mặc định rủi ro', 'Rule cuối cho các hồ sơ còn lại.', NULL::numeric, NULL::numeric, NULL::numeric, NULL::numeric, NULL::numeric, NULL::numeric, 'D', 999)
) AS v(
    rule_code,
    rule_name,
    description,
    min_monthly_income_amount,
    max_monthly_income_amount,
    min_requested_amount,
    max_requested_amount,
    min_ltv_percent,
    max_ltv_percent,
    score_grade_code,
    sort_order
)
JOIN score_grade sg
    ON sg.code = v.score_grade_code
ON CONFLICT (rule_code) DO UPDATE
SET
    rule_name = EXCLUDED.rule_name,
    description = EXCLUDED.description,
    min_monthly_income_amount = EXCLUDED.min_monthly_income_amount,
    max_monthly_income_amount = EXCLUDED.max_monthly_income_amount,
    min_requested_amount = EXCLUDED.min_requested_amount,
    max_requested_amount = EXCLUDED.max_requested_amount,
    min_ltv_percent = EXCLUDED.min_ltv_percent,
    max_ltv_percent = EXCLUDED.max_ltv_percent,
    score_grade_id = EXCLUDED.score_grade_id,
    is_active = EXCLUDED.is_active,
    sort_order = EXCLUDED.sort_order;

-- =========================================================
-- 6. Loan application document management
-- =========================================================

CREATE TABLE IF NOT EXISTS document_type (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_required BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_document_type_code
        UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS loan_application_document (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    loan_application_id UUID NOT NULL
        REFERENCES loan_application(id) ON DELETE CASCADE,
    document_type_id UUID NOT NULL
        REFERENCES document_type(id),
    file_url TEXT NOT NULL,
    file_name VARCHAR(255),
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    uploaded_by VARCHAR(100),
    note TEXT,
    CONSTRAINT uq_loan_application_document_type
        UNIQUE (loan_application_id, document_type_id)
);

CREATE INDEX IF NOT EXISTS idx_loan_application_document_application_id
ON loan_application_document(loan_application_id);

CREATE INDEX IF NOT EXISTS idx_loan_application_document_type_id
ON loan_application_document(document_type_id);

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
