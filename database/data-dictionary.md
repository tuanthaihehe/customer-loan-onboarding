# Data Dictionary - Customer Loan Onboarding

Tài liệu này được cập nhật theo các migration trong `database/migrations` đến `V26__drop_mock_score_grade_rule.sql` và seed đến `database/seed/V17__add_additional_document_types.sql`.

Mục tiêu là mô tả nhanh vai trò bảng, các cột chính và quan hệ dữ liệu. Phần index chi tiết được bỏ để tài liệu tập trung vào mô hình nghiệp vụ và foreign key.

## Quy ước chung

- `id`: khóa kỹ thuật dạng `UUID`, mặc định `gen_random_uuid()`.
- Các cột `code`, `*_code`, `product_code`: mã nghiệp vụ dùng để tra cứu, hiển thị hoặc đồng bộ.
- Các bảng danh mục thường có `is_active` và `sort_order` để frontend lọc/hiển thị dropdown.
- Các cột tiền tệ dùng `NUMERIC(18,2)`, mặc định đơn vị tiền là `VND` nếu có `currency_code`.
- Các bảng upload/chứng từ chỉ lưu metadata hoặc URL file, không lưu binary file.
- `mock_score_grade_rule` đã bị drop ở `V26`; scoring hiện dùng các bảng `*_score_band`.

## Tổng quan nhóm bảng

| Nhóm | Bảng |
|---|---|
| Khách hàng/KYC | `customer`, `kyc_profile` |
| Hồ sơ vay thật | `loan_application`, `loan_application_state`, `loan_application_state_transition`, `loan_application_state_history` |
| Hồ sơ nháp theo step | `loan_application_step`, `loan_application_draft`, `loan_application_draft_step_data`, `loan_application_draft_history` |
| Thông tin phụ hồ sơ | `loan_application_reference_person`, `loan_application_document`, `document_type` |
| Danh mục tài chính | `loan_purpose`, `loan_term`, `loan_product`, `income_source`, `occupation`, `bank`, `score_grade` |
| Mapping sản phẩm vay | `loan_product_purpose`, `loan_product_vehicle_type`, `loan_product_term`, `loan_product_score_grade` |
| Tài sản xe | `vehicle_type`, `vehicle_brand`, `vehicle_model`, `vehicle_version`, `vehicle_year`, `vehicle_color`, `vehicle_variant`, `vehicle_market_price`, `asset` |
| Định giá | `asset_deduction_type`, `asset_valuation`, `asset_valuation_deduction` |
| Scoring | `income_score_band`, `age_score_band`, `dependent_score_band`, `overall_score_grade_band` |

## `customer`

Lưu thông tin định danh ổn định của khách hàng.

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `customer_code` | `VARCHAR(50)` | Yes | Mã khách hàng, unique. |
| `full_name` | `VARCHAR(255)` | Yes | Họ tên khách hàng. |
| `phone_number` | `VARCHAR(20)` | No | Số điện thoại, unique khi có giá trị. |
| `identity_number` | `VARCHAR(20)` | No | Số CCCD/CMND/giấy tờ định danh, unique khi có giá trị. |
| `date_of_birth` | `DATE` | No | Ngày sinh. |
| `status` | `VARCHAR(30)` | Yes | `ACTIVE`, `INACTIVE`, `BLACKLIST`, `LEAD`. |
| `gender` | `VARCHAR(20)` | No | `MALE`, `FEMALE`. |
| `email` | `VARCHAR(255)` | No | Email, unique khi có giá trị. |
| `marital_status` | `VARCHAR(30)` | No | `SINGLE`, `MARRIED`. |
| `permanent_address` | `TEXT` | No | Địa chỉ thường trú. |

Quan hệ:

- `customer 1 - N loan_application`.
- `customer 1 - N loan_application_draft`.
- `customer 1 - N kyc_profile`.

## Hồ sơ vay thật

### `loan_application_state`

Danh mục state hợp lệ của hồ sơ vay thật.

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `code` | `VARCHAR(50)` | Yes | Mã state, unique. |
| `name` | `VARCHAR(100)` | Yes | Tên state. |
| `description` | `TEXT` | No | Mô tả state. |
| `is_initial` | `BOOLEAN` | Yes | Đánh dấu state khởi tạo. |
| `is_terminal` | `BOOLEAN` | Yes | Đánh dấu state kết thúc. |
| `sort_order` | `INT` | Yes | Thứ tự hiển thị. |

### `loan_application_state_transition`

Cấu hình đường chuyển state hợp lệ.

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `from_state_id` | `UUID` | Yes | FK đến state nguồn. |
| `to_state_id` | `UUID` | Yes | FK đến state đích. |
| `action_code` | `VARCHAR(50)` | Yes | Mã hành động chuyển state. |
| `action_name` | `VARCHAR(100)` | Yes | Tên hành động. |
| `description` | `TEXT` | No | Mô tả hành động. |

### `loan_application`

Bảng hồ sơ vay thật. Sau V21, luồng nháp được tách sang `loan_application_draft`; bảng này đại diện record hồ sơ vay đã được tạo thật.

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `loan_application_code` | `VARCHAR(50)` | Yes | Mã hồ sơ vay, unique. |
| `customer_id` | `UUID` | Yes | FK đến khách hàng. |
| `current_state_id` | `UUID` | Yes | FK đến state hiện tại. |
| `requested_amount` | `NUMERIC(18,2)` | No | Số tiền khách hàng muốn vay. |
| `loan_term_months` | `INT` | No | Snapshot số tháng vay, cho phép `3, 6, 9, 12, 18, 24`. |
| `branch` | `TEXT` | No | Chi nhánh/đơn vị xử lý theo dữ liệu cũ. |
| `asset_id` | `UUID` | No | FK đến tài sản đã chọn. |
| `loan_purpose_id` | `UUID` | No | FK đến mục đích vay. |
| `loan_term_id` | `UUID` | No | FK đến kỳ hạn vay. |
| `occupation_id` | `UUID` | No | FK đến nghề nghiệp. |
| `disbursement_bank_id` | `UUID` | No | FK đến ngân hàng giải ngân. |
| `disbursement_account_number` | `VARCHAR(50)` | No | Số tài khoản nhận giải ngân. |
| `disbursement_account_name` | `VARCHAR(255)` | No | Tên tài khoản nhận giải ngân. |
| `current_address` | `TEXT` | No | Địa chỉ hiện tại. |
| `workplace_name` | `VARCHAR(255)` | No | Tên nơi làm việc/cơ sở kinh doanh. |
| `workplace_address` | `TEXT` | No | Địa chỉ nơi làm việc/cơ sở kinh doanh. |
| `monthly_income_amount` | `NUMERIC(18,2)` | No | Thu nhập hàng tháng. |
| `loan_product_id` | `UUID` | No | FK đến sản phẩm vay đã chọn. |
| `income_source_id` | `UUID` | No | FK đến nguồn thu nhập. |

Quan hệ:

- Thuộc về một `customer`.
- Có một state hiện tại trong `loan_application_state`.
- Có thể gắn một `asset`, `loan_purpose`, `loan_term`, `occupation`, `bank`, `loan_product`, `income_source`.
- Có nhiều `loan_application_state_history`, `loan_application_reference_person`, `loan_application_document`.
- Có tối đa một `kyc_profile` do `kyc_profile.loan_application_id` unique.
- Có thể được tham chiếu bởi `loan_application_draft.converted_loan_application_id` khi draft được convert.

### `loan_application_state_history`

Lịch sử chuyển state của hồ sơ vay thật.

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `loan_application_id` | `UUID` | Yes | FK đến hồ sơ vay. |
| `from_state_id` | `UUID` | No | State trước khi chuyển, `NULL` cho sự kiện tạo mới. |
| `to_state_id` | `UUID` | Yes | State sau khi chuyển. |
| `action_code` | `VARCHAR(50)` | Yes | Mã hành động. |
| `changed_at` | `TIMESTAMP` | Yes | Thời điểm chuyển state. |
| `changed_by` | `VARCHAR(100)` | No | Người/hệ thống thực hiện. |
| `note` | `TEXT` | No | Ghi chú. |

## Hồ sơ nháp theo step

### `loan_application_step`

Danh mục step trong luồng nhập hồ sơ nháp.

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `code` | `VARCHAR(50)` | Yes | Mã step, primary key. |
| `name` | `VARCHAR(255)` | Yes | Tên step. |
| `step_order` | `INT` | Yes | Thứ tự step, unique. |
| `description` | `TEXT` | No | Mô tả. |
| `is_active` | `BOOLEAN` | Yes | Có còn dùng hay không. |
| `created_at` | `TIMESTAMP` | Yes | Thời điểm tạo. |
| `updated_at` | `TIMESTAMP` | Yes | Thời điểm cập nhật. |

### `loan_application_draft`

Container hồ sơ nháp trước khi tạo hồ sơ vay thật.

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `draft_code` | `VARCHAR(50)` | Yes | Mã draft, unique. |
| `customer_id` | `UUID` | Yes | FK đến khách hàng. |
| `current_step_code` | `VARCHAR(50)` | Yes | FK đến step hiện tại. |
| `status` | `VARCHAR(30)` | Yes | `DRAFT`, `COMPLETED`, `CONVERTED`, `CANCELLED`, `EXPIRED`. |
| `converted_loan_application_id` | `UUID` | No | FK đến hồ sơ vay thật sau khi convert. |
| `expired_at` | `TIMESTAMP` | No | Thời điểm hết hạn nếu có. |
| `created_at` | `TIMESTAMP` | Yes | Thời điểm tạo. |
| `updated_at` | `TIMESTAMP` | Yes | Thời điểm cập nhật. |

### `loan_application_draft_step_data`

Dữ liệu từng step của draft, lưu payload theo JSONB.

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `draft_id` | `UUID` | Yes | FK đến draft. |
| `step_code` | `VARCHAR(50)` | Yes | FK đến step. |
| `status` | `VARCHAR(30)` | Yes | `NOT_STARTED`, `IN_PROGRESS`, `COMPLETED`. |
| `payload` | `JSONB` | Yes | Payload dữ liệu của step. |
| `completed_at` | `TIMESTAMP` | No | Thời điểm hoàn tất step. |
| `invalidated_at` | `TIMESTAMP` | No | Thời điểm payload bị đánh dấu cần review. |
| `invalidated_reason` | `TEXT` | No | Lý do cần review theo thiết kế cũ, vẫn còn cột. |
| `requires_review` | `BOOLEAN` | Yes | Downstream step còn giữ payload nhưng cần rà soát lại. |
| `invalidated_by_step_code` | `VARCHAR(50)` | No | Step thay đổi khiến step này cần review. |
| `reviewed_at` | `TIMESTAMP` | No | Thời điểm đã review lại. |
| `created_at` | `TIMESTAMP` | Yes | Thời điểm tạo. |
| `updated_at` | `TIMESTAMP` | Yes | Thời điểm cập nhật. |

Ràng buộc chính: unique `(draft_id, step_code)`.

### `loan_application_draft_history`

Audit log cho draft và step.

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `draft_id` | `UUID` | Yes | FK đến draft. |
| `step_code` | `VARCHAR(50)` | No | FK đến step liên quan. |
| `action` | `VARCHAR(50)` | Yes | `CREATE_DRAFT`, `START_STEP`, `SAVE_STEP`, `COMPLETE_STEP`, `REOPEN_STEP`, `INVALIDATE_STEP`, `CANCEL_DRAFT`, `EXPIRE_DRAFT`, `COMPLETE_DRAFT`, `CONVERT_DRAFT`. |
| `old_status` | `VARCHAR(30)` | No | Status cũ. |
| `new_status` | `VARCHAR(30)` | No | Status mới. |
| `note` | `TEXT` | No | Ghi chú. |
| `changed_at` | `TIMESTAMP` | Yes | Thời điểm thay đổi. |
| `metadata` | `JSONB` | Yes | Metadata audit bổ sung. |

## Thông tin phụ hồ sơ

### `loan_application_reference_person`

Người tham chiếu của hồ sơ vay.

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `loan_application_id` | `UUID` | Yes | FK đến hồ sơ vay. |
| `full_name` | `VARCHAR(255)` | Yes | Họ tên người tham chiếu. |
| `phone_number` | `VARCHAR(20)` | Yes | Số điện thoại người tham chiếu. |
| `address` | `TEXT` | No | Địa chỉ. |
| `relationship_type` | `VARCHAR(50)` | Yes | `FATHER`, `MOTHER`, `SPOUSE`, `SIBLING`, `RELATIVE`, `FRIEND`, `COLLEAGUE`, `OTHER`. |
| `note` | `TEXT` | No | Ghi chú. |
| `created_at` | `TIMESTAMP` | Yes | Thời điểm tạo. |
| `updated_at` | `TIMESTAMP` | Yes | Thời điểm cập nhật. |

### `document_type`

Danh mục loại chứng từ.

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `code` | `VARCHAR(50)` | Yes | Mã loại chứng từ, unique. |
| `name` | `VARCHAR(255)` | Yes | Tên loại chứng từ. |
| `description` | `TEXT` | No | Mô tả. |
| `is_required` | `BOOLEAN` | Yes | Có bắt buộc hay không. |
| `is_active` | `BOOLEAN` | Yes | Có còn dùng hay không. |
| `sort_order` | `INT` | Yes | Thứ tự hiển thị. |

Seed `V17` bổ sung:

- `BORROWER_HOLDING_CITIZEN_ID_IMAGE`
- `BORROWER_PORTRAIT_VIDEO`
- `INCOME_PROOF`
- `OCCUPATION_PROOF_DOCUMENT`
- `RESIDENCE_PROOF_DOCUMENT`
- `DEPENDENT_PROOF_DOCUMENT`

### `loan_application_document`

Chứng từ/file upload gắn với hồ sơ vay.

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `loan_application_id` | `UUID` | Yes | FK đến hồ sơ vay. |
| `document_type_id` | `UUID` | Yes | FK đến loại chứng từ. |
| `file_url` | `TEXT` | Yes | URL hoặc path file đã upload. |
| `file_name` | `VARCHAR(255)` | No | Tên file gốc/hiển thị. |
| `uploaded_at` | `TIMESTAMP` | Yes | Thời điểm upload. |
| `uploaded_by` | `VARCHAR(100)` | No | Người upload. |
| `note` | `TEXT` | No | Ghi chú. |

Ràng buộc chính: unique `(loan_application_id, document_type_id)`.

## `kyc_profile`

Thông tin KYC/eKYC còn lưu sau `V23`.

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `customer_id` | `UUID` | Yes | FK đến khách hàng. |
| `loan_application_id` | `UUID` | No | FK đến hồ sơ vay, unique khi có giá trị. |
| `face_match_score` | `NUMERIC(5,2)` | No | Điểm khớp khuôn mặt, 0-100. |
| `liveness_detection_score` | `NUMERIC(5,2)` | No | Điểm liveness, 0-100. |
| `checked_at` | `TIMESTAMP` | No | Thời điểm kiểm tra. |
| `created_at` | `TIMESTAMP` | Yes | Thời điểm tạo. |
| `updated_at` | `TIMESTAMP` | Yes | Thời điểm cập nhật. |
| `note` | `TEXT` | No | Ghi chú. |

Các cột `blacklist_check_result`, `phone_otp_verification_result`, `face_authenticity_score` đã bị drop ở `V23`.

## Danh mục tài chính và sản phẩm vay

### Catalog cơ bản

| Table | Cột chính | Ý nghĩa |
|---|---|---|
| `loan_purpose` | `id`, `code`, `name`, `description`, `is_active`, `sort_order` | Danh mục mục đích vay. |
| `loan_term` | `id`, `code`, `term_months`, `name`, `description`, `is_active`, `sort_order` | Danh mục kỳ hạn vay. |
| `bank` | `id`, `code`, `name`, `short_name`, `is_active`, `sort_order` | Danh mục ngân hàng giải ngân. |
| `occupation` | `id`, `code`, `name`, `description`, `is_active`, `sort_order` | Danh mục nghề nghiệp. |
| `income_source` | `id`, `code`, `name`, `description`, `is_active`, `sort_order` | Danh mục nguồn thu nhập. |
| `score_grade` | `id`, `code`, `name`, `description`, `is_active`, `sort_order` | Danh mục hạng điểm dùng cho sản phẩm vay và kết quả scoring. |

### `loan_product`

Danh mục sản phẩm vay.

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `product_code` | `VARCHAR(50)` | Yes | Mã sản phẩm, unique. |
| `product_name` | `VARCHAR(255)` | Yes | Tên sản phẩm. |
| `applies_to_all_loan_purposes` | `BOOLEAN` | Yes | Có áp dụng cho mọi mục đích vay hay không. |
| `min_loan_amount` | `NUMERIC(18,2)` | Yes | Số tiền vay tối thiểu. |
| `max_loan_amount` | `NUMERIC(18,2)` | Yes | Số tiền vay tối đa. |
| `max_ltv_percent` | `NUMERIC(5,2)` | Yes | Tỷ lệ LTV tối đa. |
| `monthly_interest_rate_percent` | `NUMERIC(5,2)` | Yes | Lãi suất tháng. |
| `is_active` | `BOOLEAN` | Yes | Có còn dùng hay không. |
| `sort_order` | `INT` | Yes | Thứ tự hiển thị. |

Mapping sản phẩm vay:

| Table | Ý nghĩa |
|---|---|
| `loan_product_purpose` | Sản phẩm áp dụng cho mục đích vay nào. |
| `loan_product_vehicle_type` | Sản phẩm áp dụng cho loại xe nào. |
| `loan_product_term` | Sản phẩm áp dụng cho kỳ hạn nào. |
| `loan_product_score_grade` | Sản phẩm áp dụng cho hạng điểm nào. |

## Nhóm bảng xe và tài sản

Chuỗi catalog xe:

`vehicle_type -> vehicle_brand -> vehicle_model -> vehicle_version -> vehicle_year -> vehicle_variant`

| Table | Cột chính | Ý nghĩa |
|---|---|---|
| `vehicle_type` | `id`, `code`, `name`, `description`, `is_active`, `sort_order` | Loại xe. |
| `vehicle_brand` | `id`, `vehicle_type_id`, `code`, `name`, `is_active`, `sort_order` | Hãng xe theo loại xe. |
| `vehicle_model` | `id`, `vehicle_brand_id`, `code`, `name`, `is_active`, `sort_order` | Dòng xe theo hãng. |
| `vehicle_version` | `id`, `vehicle_model_id`, `code`, `name`, `is_active`, `sort_order` | Phiên bản xe. |
| `vehicle_year` | `id`, `vehicle_version_id`, `manufacture_year`, `is_active`, `sort_order` | Năm sản xuất. |
| `vehicle_color` | `id`, `code`, `name`, `is_active`, `sort_order` | Danh mục màu xe. |
| `vehicle_variant` | `id`, `vehicle_year_id`, `vehicle_color_id`, `code`, `name`, `is_active`, `sort_order` | Biến thể xe định giá được. |
| `vehicle_market_price` | `id`, `vehicle_variant_id`, `price_amount`, `currency_code`, `price_source`, `effective_from`, `effective_to`, `note` | Giá thị trường theo biến thể và thời gian hiệu lực. |

### `asset`

Tài sản xe gắn vào hồ sơ vay qua `loan_application.asset_id`.

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `asset_code` | `VARCHAR(50)` | Yes | Mã tài sản, unique. |
| `vehicle_variant_id` | `UUID` | Yes | FK đến biến thể xe. |
| `license_plate` | `VARCHAR(20)` | Yes | Biển số xe, unique. |
| `status` | `VARCHAR(30)` | Yes | `AVAILABLE`, `PLEDGED`, `RELEASED`, `SETTLED`. |
| `frame_number` | `VARCHAR(100)` | No | Số khung, unique khi có giá trị. |
| `engine_number` | `VARCHAR(100)` | No | Số máy, unique khi có giá trị. |
| `registration_issue_date` | `DATE` | No | Ngày cấp đăng ký xe. |
| `registration_certificate_number` | `VARCHAR(100)` | No | Số giấy đăng ký/cavet xe, unique khi có giá trị. |

Ghi chú: `asset.customer_id` đã bị drop ở `V5`; customer của tài sản được suy ra qua `loan_application`.

## Định giá tài sản

| Table | Cột chính | Ý nghĩa |
|---|---|---|
| `asset_deduction_type` | `id`, `code`, `name`, `description`, `deduction_amount`, `is_active`, `sort_order` | Danh mục yếu tố giảm trừ định giá. |
| `asset_valuation` | `id`, `asset_id`, `market_price_amount`, `total_deduction_amount`, `final_value_amount`, `currency_code`, `valuation_source`, `valued_at`, `valued_by`, `note` | Snapshot kết quả định giá tài sản. |
| `asset_valuation_deduction` | `id`, `asset_valuation_id`, `deduction_type_id`, `deduction_amount_snapshot`, `note`, `created_at` | Các yếu tố giảm trừ đã áp dụng trong một lần định giá. |

Quan hệ:

- `asset 1 - N asset_valuation`.
- `asset_valuation 1 - N asset_valuation_deduction`.
- `asset_deduction_type 1 - N asset_valuation_deduction`.

## Scoring band

Các bảng scoring band thay thế `mock_score_grade_rule` từ `V25`/`V26`. Seed hiện tại dùng `rule_set_code = BASIC_SCORING_V1`.

### `income_score_band`

Map thu nhập tháng sang điểm thu nhập.

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `rule_set_code` | `VARCHAR(50)` | Yes | Bộ rule scoring. |
| `min_income_amount` | `NUMERIC(18,2)` | Yes | Cận dưới thu nhập. |
| `max_income_amount` | `NUMERIC(18,2)` | No | Cận trên thu nhập, `NULL` là không giới hạn. |
| `score_value` | `NUMERIC(10,2)` | Yes | Điểm 0-100. |
| `weight` | `NUMERIC(5,4)` | Yes | Trọng số 0-1. |
| `display_label` | `VARCHAR(255)` | No | Nhãn hiển thị. |
| `priority` | `INT` | Yes | Thứ tự ưu tiên. |
| `is_active` | `BOOLEAN` | Yes | Có còn áp dụng hay không. |
| `effective_from` | `DATE` | Yes | Ngày bắt đầu hiệu lực. |
| `effective_to` | `DATE` | No | Ngày hết hiệu lực. |
| `created_at` | `TIMESTAMP` | Yes | Thời điểm tạo. |
| `updated_at` | `TIMESTAMP` | Yes | Thời điểm cập nhật. |

### `age_score_band`

Map tuổi khách hàng sang điểm tuổi. Cấu trúc giống `income_score_band`, thay cận thu nhập bằng `min_age`, `max_age`, trọng số mặc định `0.4000`.

### `dependent_score_band`

Map số người phụ thuộc sang điểm. Cấu trúc giống `income_score_band`, thay cận thu nhập bằng `min_dependent_count`, `max_dependent_count`, trọng số mặc định `0.3000`.

### `overall_score_grade_band`

Map tổng điểm sang hạng điểm.

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `rule_set_code` | `VARCHAR(50)` | Yes | Bộ rule scoring. |
| `grade_code` | `VARCHAR(10)` | Yes | Mã hạng điểm, ví dụ `A`, `B`, `C`. |
| `min_score` | `NUMERIC(10,2)` | Yes | Cận dưới tổng điểm. |
| `max_score` | `NUMERIC(10,2)` | No | Cận trên tổng điểm, `NULL` là không giới hạn. |
| `display_label` | `VARCHAR(255)` | No | Nhãn hiển thị. |
| `priority` | `INT` | Yes | Thứ tự ưu tiên. |
| `is_active` | `BOOLEAN` | Yes | Có còn áp dụng hay không. |
| `effective_from` | `DATE` | Yes | Ngày bắt đầu hiệu lực. |
| `effective_to` | `DATE` | No | Ngày hết hiệu lực. |
| `created_at` | `TIMESTAMP` | Yes | Thời điểm tạo. |
| `updated_at` | `TIMESTAMP` | Yes | Thời điểm cập nhật. |
