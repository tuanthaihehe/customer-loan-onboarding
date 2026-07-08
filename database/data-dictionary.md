# Data Dictionary - Customer Loan Onboarding

Tai lieu nay duoc cap nhat theo schema trong `backend/loan-onboarding/src/main/resources/db` den migration `V27__move_draft_flow_to_loan_application.sql` va seed den `V17__add_additional_document_types.sql`.

Muc dich: cung cap danh sach bang, cot, y nghia nghiep vu va quan he chinh de doi BA/DA loc property.

## Ghi chu quan trong

- `loan_application` hien la bang trung tam cho ca ho so dang nhap va ho so da nop. V27 da chuyen flow nhap nhap tu `loan_application_draft` sang `loan_application`.
- Cac bang `loan_application_draft`, `loan_application_draft_step_data`, `loan_application_draft_history` da bi drop o V27 va khong con la schema hien tai.
- Bang `mock_score_grade_rule` da bi drop o V26. Scoring hien dung cac bang `income_score_band`, `age_score_band`, `dependent_score_band`, `overall_score_grade_band`.
- Database khong co bang/chuc nang Eligibility.
- Cac cot tien te dung `NUMERIC(18,2)`; mac dinh tien te la VND neu co `currency_code`.
- Cac bang upload chi luu metadata/file URL, khong luu binary file.

## Nhom bang

| Nhom | Bang |
|---|---|
| Khach hang/KYC | `customer`, `kyc_profile` |
| Ho so vay/lifecycle | `loan_application`, `loan_application_state`, `loan_application_state_transition`, `loan_application_state_history` |
| Flow nhap theo step | `loan_application_step`, `loan_application_step_data`, `loan_application_step_history` |
| Thong tin phu ho so | `loan_application_reference_person`, `document_type`, `loan_application_document` |
| Danh muc tai chinh | `loan_purpose`, `loan_term`, `bank`, `occupation`, `income_source`, `score_grade`, `loan_product` |
| Mapping san pham | `loan_product_purpose`, `loan_product_vehicle_type`, `loan_product_term`, `loan_product_score_grade` |
| Tai san/xe | `vehicle_type`, `vehicle_brand`, `vehicle_model`, `vehicle_version`, `vehicle_year`, `vehicle_color`, `vehicle_variant`, `vehicle_market_price`, `asset` |
| Dinh gia | `asset_deduction_type`, `asset_valuation`, `asset_valuation_deduction` |
| Credit scoring | `income_score_band`, `age_score_band`, `dependent_score_band`, `overall_score_grade_band` |

## customer

Luu thong tin dinh danh on dinh cua khach hang.

| Column | Type | Required | Y nghia |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khoa ky thuat. |
| `customer_code` | `VARCHAR(50)` | Yes | Ma khach hang, unique. |
| `full_name` | `VARCHAR(255)` | Yes | Ho ten khach hang. |
| `phone_number` | `VARCHAR(20)` | No | So dien thoai, unique khi co gia tri. |
| `identity_number` | `VARCHAR(20)` | No | So CCCD/CMND/giay to dinh danh, unique khi co gia tri. |
| `date_of_birth` | `DATE` | No | Ngay sinh. |
| `status` | `VARCHAR(30)` | Yes | Trang thai khach hang: `ACTIVE`, `INACTIVE`, `BLACKLIST`, `LEAD`. |
| `gender` | `VARCHAR(20)` | No | Gioi tinh: `MALE`, `FEMALE`. |
| `email` | `VARCHAR(255)` | No | Email, unique khi co gia tri. |
| `marital_status` | `VARCHAR(30)` | No | Tinh trang hon nhan: `SINGLE`, `MARRIED`. |
| `permanent_address` | `TEXT` | No | Dia chi thuong tru. |

Quan he: `customer 1 - N loan_application`, `customer 1 - N kyc_profile`.

## loan_application

Bang trung tam cua ho so vay. Theo V27, record duoc tao tu dau flow va duoc cap nhat qua tung man hinh/step.

| Column | Type | Required | Y nghia |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khoa ky thuat. |
| `loan_application_code` | `VARCHAR(50)` | Yes | Ma ho so vay, unique, vi du `APP-2026-...`. |
| `customer_id` | `UUID` | Yes | FK den `customer`. |
| `current_state_id` | `UUID` | Yes | FK den `loan_application_state`, trang thai lifecycle hien tai. |
| `requested_amount` | `NUMERIC(18,2)` | No | So tien khach hang muon vay. |
| `loan_term_months` | `INT` | No | Snapshot so thang vay da chon. |
| `branch` | `TEXT` | No | Chi nhanh/PGD xu ly theo du lieu hien co. |
| `asset_id` | `UUID` | No | FK den `asset`. |
| `loan_purpose_id` | `UUID` | No | FK den `loan_purpose`. |
| `loan_term_id` | `UUID` | No | FK den `loan_term`. |
| `occupation_id` | `UUID` | No | FK den `occupation`. |
| `disbursement_bank_id` | `UUID` | No | FK den `bank`, ngan hang giai ngan. |
| `disbursement_account_number` | `VARCHAR(50)` | No | So tai khoan nhan giai ngan. |
| `disbursement_account_name` | `VARCHAR(255)` | No | Ten chu tai khoan nhan giai ngan. |
| `current_address` | `TEXT` | No | Dia chi hien tai. |
| `workplace_name` | `VARCHAR(255)` | No | Don vi cong tac/noi lam viec/co so kinh doanh. |
| `workplace_address` | `TEXT` | No | Dia chi noi lam viec/co so kinh doanh. |
| `monthly_income_amount` | `NUMERIC(18,2)` | No | Thu nhap hang thang. |
| `loan_product_id` | `UUID` | No | FK den `loan_product`, goi vay da chon. |
| `income_source_id` | `UUID` | No | FK den `income_source`, nguon thu nhap. |
| `current_step_code` | `VARCHAR(50)` | No | FK den `loan_application_step`, buoc hien tai cua flow nhap. |
| `expired_at` | `TIMESTAMP` | No | Thoi diem het han ho so neu co. |
| `created_at` | `TIMESTAMP` | Yes | Thoi diem tao ho so. |
| `updated_at` | `TIMESTAMP` | Yes | Thoi diem cap nhat gan nhat. |

Quan he:

- Thuoc ve mot `customer`.
- Co lifecycle state hien tai trong `loan_application_state`.
- Co step hien tai trong `loan_application_step`.
- Co the gan `asset`, `loan_purpose`, `loan_term`, `occupation`, `bank`, `loan_product`, `income_source`.
- Co nhieu `loan_application_step_data`, `loan_application_step_history`, `loan_application_state_history`, `loan_application_reference_person`, `loan_application_document`.
- Co toi da mot `kyc_profile` khi `kyc_profile.loan_application_id` co gia tri.

## loan_application_state

Danh muc trang thai lifecycle cua ho so vay.

| Column | Type | Required | Y nghia |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khoa ky thuat. |
| `code` | `VARCHAR(50)` | Yes | Ma state, unique. Hien co: `APP_CREATED`, `APP_IN_PROGRESS`, `APP_COMPLETED`, `APP_SUBMITTED`, `APP_CANCELLED`, `APP_EXPIRED`. |
| `name` | `VARCHAR(100)` | Yes | Ten state. |
| `description` | `TEXT` | No | Mo ta state. |
| `is_initial` | `BOOLEAN` | Yes | Co phai state khoi tao khong. |
| `is_terminal` | `BOOLEAN` | Yes | Co phai state ket thuc khong. |
| `sort_order` | `INT` | Yes | Thu tu hien thi. |

## loan_application_state_transition

Cau hinh cac chuyen trang thai hop le.

| Column | Type | Required | Y nghia |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khoa ky thuat. |
| `from_state_id` | `UUID` | Yes | FK den state nguon. |
| `to_state_id` | `UUID` | Yes | FK den state dich. |
| `action_code` | `VARCHAR(50)` | Yes | Ma hanh dong chuyen state, vi du `IDENTIFY_COMPLETE`, `COMPLETE_APPLICATION`, `SUBMIT`, `EXPIRE`, `CANCEL`, `REOPEN`. |
| `action_name` | `VARCHAR(100)` | Yes | Ten hanh dong. |
| `description` | `TEXT` | No | Mo ta hanh dong. |

## loan_application_state_history

Nhat ky lifecycle state cua ho so vay.

| Column | Type | Required | Y nghia |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khoa ky thuat. |
| `loan_application_id` | `UUID` | Yes | FK den `loan_application`. |
| `from_state_id` | `UUID` | No | State truoc khi chuyen, null khi tao moi. |
| `to_state_id` | `UUID` | Yes | State sau khi chuyen. |
| `action_code` | `VARCHAR(50)` | Yes | Ma hanh dong lifecycle. |
| `changed_at` | `TIMESTAMP` | Yes | Thoi diem chuyen state. |
| `changed_by` | `VARCHAR(100)` | No | Nguoi/he thong thuc hien. |
| `note` | `TEXT` | No | Ghi chu. |

## loan_application_step

Danh muc cac buoc trong flow nhap ho so vay.

| Column | Type | Required | Y nghia |
|---|---|---:|---|
| `code` | `VARCHAR(50)` | Yes | Ma step, primary key. |
| `name` | `VARCHAR(255)` | Yes | Ten step. |
| `step_order` | `INT` | Yes | Thu tu step, unique. |
| `description` | `TEXT` | No | Mo ta step. |
| `is_active` | `BOOLEAN` | Yes | Step con duoc dung hay khong. |
| `created_at` | `TIMESTAMP` | Yes | Thoi diem tao. |
| `updated_at` | `TIMESTAMP` | Yes | Thoi diem cap nhat. |

## loan_application_step_data

Payload JSONB theo tung step cua `loan_application`.

| Column | Type | Required | Y nghia |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khoa ky thuat. |
| `loan_application_id` | `UUID` | Yes | FK den `loan_application`. |
| `step_code` | `VARCHAR(50)` | Yes | FK den `loan_application_step`. |
| `status` | `VARCHAR(30)` | Yes | Trang thai step: `NOT_STARTED`, `IN_PROGRESS`, `COMPLETED`. |
| `payload` | `JSONB` | Yes | Du lieu cua step. |
| `requires_review` | `BOOLEAN` | Yes | Step giu payload cu nhung can review lai do step truoc thay doi. |
| `invalidated_by_step_code` | `VARCHAR(50)` | No | Step gay ra viec can review. |
| `invalidated_at` | `TIMESTAMP` | No | Thoi diem bi danh dau can review. |
| `invalidated_reason` | `TEXT` | No | Ly do can review. |
| `reviewed_at` | `TIMESTAMP` | No | Thoi diem da review lai. |
| `completed_at` | `TIMESTAMP` | No | Thoi diem hoan thanh step. |
| `created_at` | `TIMESTAMP` | Yes | Thoi diem tao. |
| `updated_at` | `TIMESTAMP` | Yes | Thoi diem cap nhat. |

Rang buoc chinh: unique `(loan_application_id, step_code)`.

## loan_application_step_history

Audit log cho cac thao tac step tren `loan_application`.

| Column | Type | Required | Y nghia |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khoa ky thuat. |
| `loan_application_id` | `UUID` | Yes | FK den `loan_application`. |
| `step_code` | `VARCHAR(50)` | No | FK den step lien quan. |
| `action` | `VARCHAR(50)` | Yes | Hanh dong: `CREATE_APPLICATION`, `START_STEP`, `SAVE_STEP`, `COMPLETE_STEP`, `REOPEN_STEP`, `INVALIDATE_STEP`, `CANCEL_APPLICATION`, `EXPIRE_APPLICATION`, `COMPLETE_APPLICATION`, `SUBMIT_APPLICATION`. |
| `old_status` | `VARCHAR(30)` | No | Trang thai cu. |
| `new_status` | `VARCHAR(30)` | No | Trang thai moi. |
| `note` | `TEXT` | No | Ghi chu. |
| `changed_at` | `TIMESTAMP` | Yes | Thoi diem thay doi. |
| `metadata` | `JSONB` | Yes | Metadata audit bo sung. |

## loan_application_reference_person

Nguoi tham chieu cua ho so vay.

| Column | Type | Required | Y nghia |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khoa ky thuat. |
| `loan_application_id` | `UUID` | Yes | FK den `loan_application`. |
| `full_name` | `VARCHAR(255)` | Yes | Ho ten nguoi tham chieu. |
| `phone_number` | `VARCHAR(20)` | Yes | So dien thoai nguoi tham chieu. |
| `address` | `TEXT` | No | Dia chi. |
| `relationship_type` | `VARCHAR(50)` | Yes | Moi quan he: `FATHER`, `MOTHER`, `SPOUSE`, `SIBLING`, `RELATIVE`, `FRIEND`, `COLLEAGUE`, `OTHER`. |
| `note` | `TEXT` | No | Ghi chu. |
| `created_at` | `TIMESTAMP` | Yes | Thoi diem tao. |
| `updated_at` | `TIMESTAMP` | Yes | Thoi diem cap nhat. |

## document_type

Danh muc loai chung tu can upload.

| Column | Type | Required | Y nghia |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khoa ky thuat. |
| `code` | `VARCHAR(50)` | Yes | Ma loai chung tu, unique. |
| `name` | `VARCHAR(255)` | Yes | Ten loai chung tu. |
| `description` | `TEXT` | No | Mo ta. |
| `is_required` | `BOOLEAN` | Yes | Co bat buoc upload hay khong. |
| `is_active` | `BOOLEAN` | Yes | Co con su dung hay khong. |
| `sort_order` | `INT` | Yes | Thu tu hien thi. |

Seed `V10` va `V17` tao cac loai chung tu cho CCCD, cavet, anh tai san, anh chan dung/eKYC va chung tu bo sung.

## loan_application_document

Chung tu/file upload gan voi ho so vay.

| Column | Type | Required | Y nghia |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khoa ky thuat. |
| `loan_application_id` | `UUID` | Yes | FK den `loan_application`. |
| `document_type_id` | `UUID` | Yes | FK den `document_type`. |
| `file_url` | `TEXT` | Yes | URL/path file da upload. |
| `file_name` | `VARCHAR(255)` | No | Ten file goc/ten hien thi. |
| `uploaded_at` | `TIMESTAMP` | Yes | Thoi diem upload. |
| `uploaded_by` | `VARCHAR(100)` | No | Nguoi upload. |
| `note` | `TEXT` | No | Ghi chu. |

Rang buoc chinh: unique `(loan_application_id, document_type_id)`.

## kyc_profile

Thong tin KYC/eKYC gan voi khach hang va co the gan voi mot ho so vay.

| Column | Type | Required | Y nghia |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khoa ky thuat. |
| `customer_id` | `UUID` | Yes | FK den `customer`. |
| `loan_application_id` | `UUID` | No | FK den `loan_application`, unique khi co gia tri. |
| `face_match_score` | `NUMERIC(5,2)` | No | Diem doi chieu khuon mat, 0-100. |
| `liveness_detection_score` | `NUMERIC(5,2)` | No | Diem liveness, 0-100. |
| `checked_at` | `TIMESTAMP` | No | Thoi diem kiem tra. |
| `created_at` | `TIMESTAMP` | Yes | Thoi diem tao. |
| `updated_at` | `TIMESTAMP` | Yes | Thoi diem cap nhat. |
| `note` | `TEXT` | No | Ghi chu. |

V23 da drop cac cot `blacklist_check_result`, `phone_otp_verification_result`, `face_authenticity_score`.

## Danh muc tai chinh va san pham

### loan_purpose

| Column | Type | Required | Y nghia |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khoa ky thuat. |
| `code` | `VARCHAR(50)` | Yes | Ma muc dich vay, unique. |
| `name` | `VARCHAR(100)` | Yes | Ten muc dich vay. |
| `description` | `TEXT` | No | Mo ta. |
| `is_active` | `BOOLEAN` | Yes | Co con su dung hay khong. |
| `sort_order` | `INT` | Yes | Thu tu hien thi. |

### loan_term

| Column | Type | Required | Y nghia |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khoa ky thuat. |
| `code` | `VARCHAR(50)` | Yes | Ma ky han, unique. |
| `term_months` | `INT` | Yes | So thang vay, unique. |
| `name` | `VARCHAR(100)` | Yes | Ten ky han. |
| `description` | `TEXT` | No | Mo ta. |
| `is_active` | `BOOLEAN` | Yes | Co con su dung hay khong. |
| `sort_order` | `INT` | Yes | Thu tu hien thi. |

### bank

| Column | Type | Required | Y nghia |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khoa ky thuat. |
| `code` | `VARCHAR(50)` | Yes | Ma ngan hang, unique. |
| `name` | `VARCHAR(255)` | Yes | Ten ngan hang. |
| `short_name` | `VARCHAR(100)` | No | Ten ngan/ten viet tat. |
| `is_active` | `BOOLEAN` | Yes | Co con su dung hay khong. |
| `sort_order` | `INT` | Yes | Thu tu hien thi. |

### occupation

| Column | Type | Required | Y nghia |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khoa ky thuat. |
| `code` | `VARCHAR(50)` | Yes | Ma nghe nghiep, unique. |
| `name` | `VARCHAR(255)` | Yes | Ten nghe nghiep. |
| `description` | `TEXT` | No | Mo ta. |
| `is_active` | `BOOLEAN` | Yes | Co con su dung hay khong. |
| `sort_order` | `INT` | Yes | Thu tu hien thi. |

### income_source

| Column | Type | Required | Y nghia |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khoa ky thuat. |
| `code` | `VARCHAR(50)` | Yes | Ma nguon thu nhap, unique. |
| `name` | `VARCHAR(255)` | Yes | Ten nguon thu nhap. |
| `description` | `TEXT` | No | Mo ta. |
| `is_active` | `BOOLEAN` | Yes | Co con su dung hay khong. |
| `sort_order` | `INT` | Yes | Thu tu hien thi. |

### score_grade

| Column | Type | Required | Y nghia |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khoa ky thuat. |
| `code` | `VARCHAR(10)` | Yes | Ma hang diem, unique, vi du `A`, `B`, `C`. |
| `name` | `VARCHAR(100)` | Yes | Ten hang diem. |
| `description` | `TEXT` | No | Mo ta. |
| `is_active` | `BOOLEAN` | Yes | Co con su dung hay khong. |
| `sort_order` | `INT` | Yes | Thu tu hien thi. |

### loan_product

Danh muc goi/san pham vay va dieu kien co ban.

| Column | Type | Required | Y nghia |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khoa ky thuat. |
| `product_code` | `VARCHAR(50)` | Yes | Ma san pham vay, unique. |
| `product_name` | `VARCHAR(255)` | Yes | Ten san pham vay. |
| `applies_to_all_loan_purposes` | `BOOLEAN` | Yes | Co ap dung cho moi muc dich vay hay khong. |
| `min_loan_amount` | `NUMERIC(18,2)` | Yes | Han muc vay toi thieu. |
| `max_loan_amount` | `NUMERIC(18,2)` | Yes | Han muc vay toi da theo san pham. |
| `max_ltv_percent` | `NUMERIC(5,2)` | Yes | Ty le LTV toi da. |
| `monthly_interest_rate_percent` | `NUMERIC(5,2)` | Yes | Lai suat thang. |
| `is_active` | `BOOLEAN` | Yes | Co con su dung hay khong. |
| `sort_order` | `INT` | Yes | Thu tu hien thi. |

Mapping san pham:

| Table | Column | Y nghia |
|---|---|---|
| `loan_product_purpose` | `loan_product_id`, `loan_purpose_id` | San pham ap dung cho muc dich vay nao. |
| `loan_product_vehicle_type` | `loan_product_id`, `vehicle_type_id` | San pham ap dung cho loai xe nao. |
| `loan_product_term` | `loan_product_id`, `loan_term_id` | San pham ap dung cho ky han nao. |
| `loan_product_score_grade` | `loan_product_id`, `score_grade_id` | San pham ap dung cho hang diem nao. |

## Nhom xe va tai san

Chuoi catalog xe: `vehicle_type -> vehicle_brand -> vehicle_model -> vehicle_version -> vehicle_year -> vehicle_variant`.

| Table | Cot chinh | Y nghia |
|---|---|---|
| `vehicle_type` | `id`, `code`, `name`, `description`, `is_active`, `sort_order` | Loai xe. |
| `vehicle_brand` | `id`, `vehicle_type_id`, `code`, `name`, `is_active`, `sort_order` | Hang xe theo loai xe. |
| `vehicle_model` | `id`, `vehicle_brand_id`, `code`, `name`, `is_active`, `sort_order` | Dong xe theo hang. |
| `vehicle_version` | `id`, `vehicle_model_id`, `code`, `name`, `is_active`, `sort_order` | Phien ban xe. |
| `vehicle_year` | `id`, `vehicle_version_id`, `manufacture_year`, `is_active`, `sort_order` | Nam san xuat theo phien ban. |
| `vehicle_color` | `id`, `code`, `name`, `is_active`, `sort_order` | Danh muc mau xe. |
| `vehicle_variant` | `id`, `vehicle_year_id`, `vehicle_color_id`, `code`, `name`, `is_active`, `sort_order` | Bien the xe dung de dinh gia. |
| `vehicle_market_price` | `id`, `vehicle_variant_id`, `price_amount`, `currency_code`, `price_source`, `effective_from`, `effective_to`, `note` | Gia thi truong theo bien the va thoi gian hieu luc. |

### asset

Tai san xe gan vao ho so vay thong qua `loan_application.asset_id`.

| Column | Type | Required | Y nghia |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khoa ky thuat. |
| `asset_code` | `VARCHAR(50)` | Yes | Ma tai san, unique. |
| `vehicle_variant_id` | `UUID` | Yes | FK den `vehicle_variant`. |
| `license_plate` | `VARCHAR(20)` | Yes | Bien so xe, unique. |
| `status` | `VARCHAR(30)` | Yes | Trang thai tai san: `AVAILABLE`, `PLEDGED`, `RELEASED`, `SETTLED`. |
| `frame_number` | `VARCHAR(100)` | No | So khung, unique khi co gia tri. |
| `engine_number` | `VARCHAR(100)` | No | So may, unique khi co gia tri. |
| `registration_issue_date` | `DATE` | No | Ngay cap dang ky xe. |
| `registration_certificate_number` | `VARCHAR(100)` | No | So giay dang ky/cavet xe, unique khi co gia tri. |

Ghi chu: `asset.customer_id` da bi drop o V5; customer cua tai san duoc suy ra qua `loan_application`.

## Dinh gia tai san

### asset_deduction_type

| Column | Type | Required | Y nghia |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khoa ky thuat. |
| `code` | `VARCHAR(50)` | Yes | Ma yeu to giam tru, unique. |
| `name` | `VARCHAR(100)` | Yes | Ten yeu to giam tru. |
| `description` | `TEXT` | No | Mo ta. |
| `deduction_amount` | `NUMERIC(18,2)` | Yes | So tien giam tru. |
| `is_active` | `BOOLEAN` | Yes | Co con ap dung hay khong. |
| `sort_order` | `INT` | Yes | Thu tu hien thi. |

### asset_valuation

| Column | Type | Required | Y nghia |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khoa ky thuat. |
| `asset_id` | `UUID` | Yes | FK den `asset`. |
| `market_price_amount` | `NUMERIC(18,2)` | Yes | Gia thi truong. |
| `total_deduction_amount` | `NUMERIC(18,2)` | Yes | Tong so tien giam tru. |
| `final_value_amount` | `NUMERIC(18,2)` | Yes | Gia tri sau giam tru. |
| `currency_code` | `VARCHAR(3)` | Yes | Ma tien te, mac dinh `VND`. |
| `valuation_source` | `VARCHAR(100)` | No | Nguon dinh gia. |
| `valued_at` | `TIMESTAMP` | Yes | Thoi diem dinh gia. |
| `valued_by` | `VARCHAR(100)` | No | Nguoi/he thong dinh gia. |
| `note` | `TEXT` | No | Ghi chu. |

### asset_valuation_deduction

| Column | Type | Required | Y nghia |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khoa ky thuat. |
| `asset_valuation_id` | `UUID` | Yes | FK den `asset_valuation`. |
| `deduction_type_id` | `UUID` | Yes | FK den `asset_deduction_type`. |
| `deduction_amount_snapshot` | `NUMERIC(18,2)` | Yes | So tien giam tru tai thoi diem dinh gia. |
| `note` | `TEXT` | No | Ghi chu. |
| `created_at` | `TIMESTAMP` | Yes | Thoi diem tao. |

## Credit scoring

Cac bang scoring band thay the `mock_score_grade_rule`.

### income_score_band

| Column | Type | Required | Y nghia |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khoa ky thuat. |
| `rule_set_code` | `VARCHAR(50)` | Yes | Bo rule scoring, mac dinh `BASIC_SCORING_V1`. |
| `min_income_amount` | `NUMERIC(18,2)` | Yes | Can duoi thu nhap. |
| `max_income_amount` | `NUMERIC(18,2)` | No | Can tren thu nhap, null la khong gioi han. |
| `score_value` | `NUMERIC(10,2)` | Yes | Diem scoring 0-100. |
| `weight` | `NUMERIC(5,4)` | Yes | Trong so, mac dinh `0.3000`. |
| `display_label` | `VARCHAR(255)` | No | Nhan hien thi. |
| `priority` | `INT` | Yes | Do uu tien. |
| `is_active` | `BOOLEAN` | Yes | Co con ap dung hay khong. |
| `effective_from` | `DATE` | Yes | Ngay bat dau hieu luc. |
| `effective_to` | `DATE` | No | Ngay het hieu luc. |
| `created_at` | `TIMESTAMP` | Yes | Thoi diem tao. |
| `updated_at` | `TIMESTAMP` | Yes | Thoi diem cap nhat. |

### age_score_band

| Column | Type | Required | Y nghia |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khoa ky thuat. |
| `rule_set_code` | `VARCHAR(50)` | Yes | Bo rule scoring. |
| `min_age` | `INT` | Yes | Can duoi tuoi. |
| `max_age` | `INT` | No | Can tren tuoi, null la khong gioi han. |
| `score_value` | `NUMERIC(10,2)` | Yes | Diem scoring 0-100. |
| `weight` | `NUMERIC(5,4)` | Yes | Trong so, mac dinh `0.4000`. |
| `display_label` | `VARCHAR(255)` | No | Nhan hien thi. |
| `priority` | `INT` | Yes | Do uu tien. |
| `is_active` | `BOOLEAN` | Yes | Co con ap dung hay khong. |
| `effective_from` | `DATE` | Yes | Ngay bat dau hieu luc. |
| `effective_to` | `DATE` | No | Ngay het hieu luc. |
| `created_at` | `TIMESTAMP` | Yes | Thoi diem tao. |
| `updated_at` | `TIMESTAMP` | Yes | Thoi diem cap nhat. |

### dependent_score_band

| Column | Type | Required | Y nghia |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khoa ky thuat. |
| `rule_set_code` | `VARCHAR(50)` | Yes | Bo rule scoring. |
| `min_dependent_count` | `INT` | Yes | Can duoi so nguoi phu thuoc. |
| `max_dependent_count` | `INT` | No | Can tren so nguoi phu thuoc, null la khong gioi han. |
| `score_value` | `NUMERIC(10,2)` | Yes | Diem scoring 0-100. |
| `weight` | `NUMERIC(5,4)` | Yes | Trong so, mac dinh `0.3000`. |
| `display_label` | `VARCHAR(255)` | No | Nhan hien thi. |
| `priority` | `INT` | Yes | Do uu tien. |
| `is_active` | `BOOLEAN` | Yes | Co con ap dung hay khong. |
| `effective_from` | `DATE` | Yes | Ngay bat dau hieu luc. |
| `effective_to` | `DATE` | No | Ngay het hieu luc. |
| `created_at` | `TIMESTAMP` | Yes | Thoi diem tao. |
| `updated_at` | `TIMESTAMP` | Yes | Thoi diem cap nhat. |

### overall_score_grade_band

| Column | Type | Required | Y nghia |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khoa ky thuat. |
| `rule_set_code` | `VARCHAR(50)` | Yes | Bo rule scoring. |
| `grade_code` | `VARCHAR(10)` | Yes | Hang diem, vi du `A`, `B`, `C`. |
| `min_score` | `NUMERIC(10,2)` | Yes | Can duoi tong diem. |
| `max_score` | `NUMERIC(10,2)` | No | Can tren tong diem, null la khong gioi han. |
| `display_label` | `VARCHAR(255)` | No | Nhan hien thi. |
| `priority` | `INT` | Yes | Do uu tien. |
| `is_active` | `BOOLEAN` | Yes | Co con ap dung hay khong. |
| `effective_from` | `DATE` | Yes | Ngay bat dau hieu luc. |
| `effective_to` | `DATE` | No | Ngay het hieu luc. |
| `created_at` | `TIMESTAMP` | Yes | Thoi diem tao. |
| `updated_at` | `TIMESTAMP` | Yes | Thoi diem cap nhat. |

## Bang da loai bo khoi schema hien tai

| Bang | Ly do |
|---|---|
| `loan_application_draft` | V27 chuyen flow draft vao `loan_application`. |
| `loan_application_draft_step_data` | Thay bang `loan_application_step_data`. |
| `loan_application_draft_history` | Thay bang `loan_application_step_history`. |
| `mock_score_grade_rule` | V26 drop, thay bang cum bang scoring band o V25. |
