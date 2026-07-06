# Data Dictionary - Customer Loan Onboarding

Tài liệu này được cập nhật theo các migration trong `database/migrations` đến `V20__add_registration_certificate_number_to_asset.sql`.

Mục tiêu của tài liệu là mô tả nhanh vai trò bảng, các cột chính và quan hệ dữ liệu. Phần index được bỏ để tài liệu tập trung vào mô hình nghiệp vụ và foreign key.

## Quy ước chung

- `id`: khóa kỹ thuật dạng `UUID`, mặc định `gen_random_uuid()`.
- Các cột `code`, `*_code`, `product_code`: mã nghiệp vụ dùng để tra cứu, hiển thị hoặc đồng bộ.
- Các bảng danh mục thường có `is_active` và `sort_order` để frontend lọc/hiển thị dropdown.
- Các cột tiền tệ dùng `NUMERIC(18,2)`, mặc định đơn vị tiền là `VND` nếu có `currency_code`.
- Các bảng upload/chứng từ chỉ lưu metadata hoặc URL file, không lưu binary file.

## Tổng quan quan hệ

| Nhóm | Bảng |
|---|---|
| Khách hàng | `customer`, `kyc_profile` |
| Hồ sơ vay | `loan_application`, `loan_application_state`, `loan_application_state_transition`, `loan_application_state_history` |
| Thông tin hồ sơ phụ | `loan_application_reference_person`, `loan_application_document`, `document_type` |
| Danh mục tài chính | `loan_purpose`, `loan_term`, `loan_product`, `income_source`, `occupation`, `bank`, `score_grade` |
| Mapping sản phẩm vay | `loan_product_purpose`, `loan_product_vehicle_type`, `loan_product_term`, `loan_product_score_grade` |
| Tài sản xe | `vehicle_type`, `vehicle_brand`, `vehicle_model`, `vehicle_version`, `vehicle_year`, `vehicle_color`, `vehicle_variant`, `vehicle_market_price`, `asset` |
| Định giá | `asset_deduction_type`, `asset_valuation`, `asset_valuation_deduction` |
| Demo scoring | `mock_score_grade_rule` |

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

- `customer 1 - N loan_application`
- `customer 1 - N kyc_profile`

## `loan_application_state`

Danh mục state hợp lệ của hồ sơ vay.

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `code` | `VARCHAR(50)` | Yes | Mã state, unique. |
| `name` | `VARCHAR(100)` | Yes | Tên state. |
| `description` | `TEXT` | No | Mô tả state. |
| `is_initial` | `BOOLEAN` | Yes | Đánh dấu state khởi tạo. |
| `is_terminal` | `BOOLEAN` | Yes | Đánh dấu state kết thúc. |
| `sort_order` | `INT` | Yes | Thứ tự hiển thị. |

Quan hệ:

- Được tham chiếu bởi `loan_application.current_state_id`.
- Được tham chiếu bởi `loan_application_state_transition`.
- Được tham chiếu bởi `loan_application_state_history`.

## `loan_application_state_transition`

Cấu hình các đường chuyển state hợp lệ.

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `from_state_id` | `UUID` | Yes | FK đến state nguồn. |
| `to_state_id` | `UUID` | Yes | FK đến state đích. |
| `action_code` | `VARCHAR(50)` | Yes | Mã hành động chuyển state. |
| `action_name` | `VARCHAR(100)` | Yes | Tên hành động. |
| `description` | `TEXT` | No | Mô tả hành động. |

Quan hệ:

- `loan_application_state 1 - N loan_application_state_transition` qua `from_state_id`.
- `loan_application_state 1 - N loan_application_state_transition` qua `to_state_id`.

## `loan_purpose`

Danh mục mục đích vay.

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `code` | `VARCHAR(50)` | Yes | Mã mục đích vay, unique. |
| `name` | `VARCHAR(100)` | Yes | Tên hiển thị. |
| `description` | `TEXT` | No | Mô tả. |
| `is_active` | `BOOLEAN` | Yes | Có còn dùng để chọn hay không. |
| `sort_order` | `INT` | Yes | Thứ tự hiển thị. |

Quan hệ:

- `loan_purpose 1 - N loan_application`.
- `loan_purpose N - N loan_product` qua `loan_product_purpose`.

## `loan_term`

Danh mục kỳ hạn vay.

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `code` | `VARCHAR(50)` | Yes | Mã kỳ hạn, unique. |
| `term_months` | `INT` | Yes | Số tháng, unique và lớn hơn 0. |
| `name` | `VARCHAR(100)` | Yes | Tên hiển thị. |
| `description` | `TEXT` | No | Mô tả. |
| `is_active` | `BOOLEAN` | Yes | Có còn dùng để chọn hay không. |
| `sort_order` | `INT` | Yes | Thứ tự hiển thị. |

Quan hệ:

- `loan_term 1 - N loan_application`.
- `loan_term N - N loan_product` qua `loan_product_term`.

## `bank`

Danh mục ngân hàng giải ngân.

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `code` | `VARCHAR(50)` | Yes | Mã ngân hàng, unique. |
| `name` | `VARCHAR(255)` | Yes | Tên đầy đủ. |
| `short_name` | `VARCHAR(100)` | No | Tên viết tắt. |
| `is_active` | `BOOLEAN` | Yes | Có còn dùng để chọn hay không. |
| `sort_order` | `INT` | Yes | Thứ tự hiển thị. |

Quan hệ:

- `bank 1 - N loan_application` qua `disbursement_bank_id`.

## `occupation`

Danh mục nghề nghiệp.

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `code` | `VARCHAR(50)` | Yes | Mã nghề nghiệp, unique. |
| `name` | `VARCHAR(255)` | Yes | Tên nghề nghiệp. |
| `description` | `TEXT` | No | Mô tả. |
| `is_active` | `BOOLEAN` | Yes | Có còn dùng để chọn hay không. |
| `sort_order` | `INT` | Yes | Thứ tự hiển thị. |

Quan hệ:

- `occupation 1 - N loan_application` qua `occupation_id`.

## `income_source`

Danh mục nguồn thu nhập khai báo của người vay.

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `code` | `VARCHAR(50)` | Yes | Mã nguồn thu nhập, unique. |
| `name` | `VARCHAR(255)` | Yes | Tên nguồn thu nhập. |
| `description` | `TEXT` | No | Mô tả. |
| `is_active` | `BOOLEAN` | Yes | Có còn dùng để chọn hay không. |
| `sort_order` | `INT` | Yes | Thứ tự hiển thị. |

Seed hiện tại gồm: `SALARY`, `BUSINESS`, `SELF_EMPLOYED`, `COMMISSION`, `DRIVER_INCOME`, `RENTAL`, `FAMILY_SUPPORT`, `PENSION`, `AGRICULTURE`, `OTHER`.

Quan hệ:

- `income_source 1 - N loan_application` qua `income_source_id`.

## `score_grade`

Danh mục hạng điểm phục vụ rule/demo scoring và lọc sản phẩm vay.

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `code` | `VARCHAR(10)` | Yes | Mã hạng điểm, unique. |
| `name` | `VARCHAR(100)` | Yes | Tên hạng điểm. |
| `description` | `TEXT` | No | Mô tả. |
| `is_active` | `BOOLEAN` | Yes | Có còn dùng hay không. |
| `sort_order` | `INT` | Yes | Thứ tự ưu tiên/hiển thị. |

Quan hệ:

- `score_grade N - N loan_product` qua `loan_product_score_grade`.
- `score_grade 1 - N mock_score_grade_rule`.

## `loan_product`

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

Quan hệ:

- `loan_product 1 - N loan_application` qua `loan_product_id`.
- `loan_product N - N loan_purpose` qua `loan_product_purpose`.
- `loan_product N - N vehicle_type` qua `loan_product_vehicle_type`.
- `loan_product N - N loan_term` qua `loan_product_term`.
- `loan_product N - N score_grade` qua `loan_product_score_grade`.

## Mapping sản phẩm vay

Các bảng mapping dùng để cấu hình sản phẩm vay áp dụng cho mục đích vay, loại xe, kỳ hạn và hạng điểm nào.

| Table | Column | Type | Required | Quan hệ |
|---|---|---|---:|---|
| `loan_product_purpose` | `loan_product_id` | `UUID` | Yes | FK đến `loan_product.id`. |
| `loan_product_purpose` | `loan_purpose_id` | `UUID` | Yes | FK đến `loan_purpose.id`. |
| `loan_product_vehicle_type` | `loan_product_id` | `UUID` | Yes | FK đến `loan_product.id`. |
| `loan_product_vehicle_type` | `vehicle_type_id` | `UUID` | Yes | FK đến `vehicle_type.id`. |
| `loan_product_term` | `loan_product_id` | `UUID` | Yes | FK đến `loan_product.id`. |
| `loan_product_term` | `loan_term_id` | `UUID` | Yes | FK đến `loan_term.id`. |
| `loan_product_score_grade` | `loan_product_id` | `UUID` | Yes | FK đến `loan_product.id`. |
| `loan_product_score_grade` | `score_grade_id` | `UUID` | Yes | FK đến `score_grade.id`. |

## `loan_application`

Bảng hồ sơ vay chính.

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `loan_application_code` | `VARCHAR(50)` | Yes | Mã hồ sơ vay, unique. |
| `customer_id` | `UUID` | Yes | FK đến khách hàng. |
| `current_state_id` | `UUID` | Yes | FK đến state hiện tại. |
| `requested_amount` | `NUMERIC(18,2)` | No | Số tiền khách hàng muốn vay. |
| `loan_term_months` | `INT` | No | Snapshot số tháng vay, hiện cho phép `3, 6, 9, 12, 18, 24`. |
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
- Có nhiều `loan_application_state_history`.
- Có nhiều `loan_application_reference_person`.
- Có nhiều `loan_application_document`.
- Có tối đa một `kyc_profile` do `kyc_profile.loan_application_id` unique.

## `loan_application_state_history`

Lịch sử chuyển state của hồ sơ vay.

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

## `loan_application_reference_person`

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

Quan hệ:

- `loan_application 1 - N loan_application_reference_person`.

## `document_type`

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

Quan hệ:

- `document_type 1 - N loan_application_document`.

## `loan_application_document`

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

Quan hệ:

- `loan_application 1 - N loan_application_document`.
- `document_type 1 - N loan_application_document`.

## `kyc_profile`

Thông tin KYC/eKYC và các tín hiệu tin cậy của khách hàng trong quá trình onboarding.

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `customer_id` | `UUID` | Yes | FK đến khách hàng. |
| `loan_application_id` | `UUID` | No | FK đến hồ sơ vay, unique khi có giá trị. |
| `blacklist_check_result` | `BOOLEAN` | No | Kết quả kiểm tra blacklist. |
| `phone_otp_verification_result` | `BOOLEAN` | No | Kết quả xác thực OTP điện thoại. |
| `face_match_score` | `NUMERIC(5,2)` | No | Điểm khớp khuôn mặt, 0-100. |
| `liveness_detection_score` | `NUMERIC(5,2)` | No | Điểm liveness, 0-100. |
| `face_authenticity_score` | `NUMERIC(5,2)` | No | Điểm xác thực khuôn mặt, 0-100. |
| `checked_at` | `TIMESTAMP` | No | Thời điểm kiểm tra. |
| `created_at` | `TIMESTAMP` | Yes | Thời điểm tạo. |
| `updated_at` | `TIMESTAMP` | Yes | Thời điểm cập nhật. |
| `note` | `TEXT` | No | Ghi chú. |

Quan hệ:

- `customer 1 - N kyc_profile`.
- `loan_application 0/1 - 1 kyc_profile`.

## Nhóm bảng xe

Các bảng xe tạo thành chuỗi dropdown/lookup:

`vehicle_type -> vehicle_brand -> vehicle_model -> vehicle_version -> vehicle_year -> vehicle_variant`

### `vehicle_type`

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `code` | `VARCHAR(50)` | Yes | Mã loại xe, unique. |
| `name` | `VARCHAR(100)` | Yes | Tên loại xe. |
| `description` | `TEXT` | No | Mô tả. |
| `is_active` | `BOOLEAN` | Yes | Có còn dùng hay không. |
| `sort_order` | `INT` | Yes | Thứ tự hiển thị. |

### `vehicle_brand`

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `vehicle_type_id` | `UUID` | Yes | FK đến loại xe. |
| `code` | `VARCHAR(50)` | Yes | Mã hãng xe. |
| `name` | `VARCHAR(100)` | Yes | Tên hãng xe. |
| `is_active` | `BOOLEAN` | Yes | Có còn dùng hay không. |
| `sort_order` | `INT` | Yes | Thứ tự hiển thị. |

### `vehicle_model`

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `vehicle_brand_id` | `UUID` | Yes | FK đến hãng xe. |
| `code` | `VARCHAR(50)` | Yes | Mã dòng xe. |
| `name` | `VARCHAR(100)` | Yes | Tên dòng xe. |
| `is_active` | `BOOLEAN` | Yes | Có còn dùng hay không. |
| `sort_order` | `INT` | Yes | Thứ tự hiển thị. |

### `vehicle_version`

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `vehicle_model_id` | `UUID` | Yes | FK đến dòng xe. |
| `code` | `VARCHAR(50)` | Yes | Mã phiên bản. |
| `name` | `VARCHAR(100)` | Yes | Tên phiên bản. |
| `is_active` | `BOOLEAN` | Yes | Có còn dùng hay không. |
| `sort_order` | `INT` | Yes | Thứ tự hiển thị. |

### `vehicle_year`

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `vehicle_version_id` | `UUID` | Yes | FK đến phiên bản. |
| `manufacture_year` | `INT` | Yes | Năm sản xuất, 1980-2100. |
| `is_active` | `BOOLEAN` | Yes | Có còn dùng hay không. |
| `sort_order` | `INT` | Yes | Thứ tự hiển thị. |

### `vehicle_color`

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `code` | `VARCHAR(50)` | Yes | Mã màu, unique. |
| `name` | `VARCHAR(100)` | Yes | Tên màu. |
| `is_active` | `BOOLEAN` | Yes | Có còn dùng hay không. |
| `sort_order` | `INT` | Yes | Thứ tự hiển thị. |

### `vehicle_variant`

Biến thể xe định giá được, kết hợp từ `vehicle_year` và `vehicle_color`.

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `vehicle_year_id` | `UUID` | Yes | FK đến năm sản xuất. |
| `vehicle_color_id` | `UUID` | Yes | FK đến màu xe. |
| `code` | `VARCHAR(100)` | Yes | Mã biến thể, unique. |
| `name` | `VARCHAR(255)` | Yes | Tên biến thể. |
| `is_active` | `BOOLEAN` | Yes | Có còn dùng hay không. |
| `sort_order` | `INT` | Yes | Thứ tự hiển thị. |

Quan hệ:

- `vehicle_variant 1 - N vehicle_market_price`.
- `vehicle_variant 1 - N asset`.

## `vehicle_market_price`

Giá thị trường theo từng biến thể xe và thời gian hiệu lực.

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `vehicle_variant_id` | `UUID` | Yes | FK đến biến thể xe. |
| `price_amount` | `NUMERIC(18,2)` | Yes | Giá thị trường. |
| `currency_code` | `VARCHAR(3)` | Yes | Mã tiền tệ, mặc định `VND`. |
| `price_source` | `VARCHAR(100)` | No | Nguồn giá. |
| `effective_from` | `DATE` | Yes | Ngày bắt đầu hiệu lực. |
| `effective_to` | `DATE` | No | Ngày hết hiệu lực. |
| `note` | `TEXT` | No | Ghi chú. |

## `asset`

Tài sản xe gắn vào hồ sơ vay.

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

Quan hệ:

- `vehicle_variant 1 - N asset`.
- `asset 1 - N loan_application` về mặt schema qua `loan_application.asset_id`; nghiệp vụ có thể giới hạn một tài sản trong một hồ sơ active.
- `asset 1 - N asset_valuation`.

## `asset_deduction_type`

Danh mục yếu tố giảm trừ định giá tài sản.

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `code` | `VARCHAR(50)` | Yes | Mã yếu tố giảm trừ, unique. |
| `name` | `VARCHAR(100)` | Yes | Tên yếu tố giảm trừ. |
| `description` | `TEXT` | No | Mô tả. |
| `deduction_amount` | `NUMERIC(18,2)` | Yes | Số tiền giảm trừ cố định. |
| `is_active` | `BOOLEAN` | Yes | Có còn dùng hay không. |
| `sort_order` | `INT` | Yes | Thứ tự hiển thị. |

## `asset_valuation`

Snapshot kết quả định giá tài sản.

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `asset_id` | `UUID` | Yes | FK đến tài sản. |
| `market_price_amount` | `NUMERIC(18,2)` | Yes | Giá thị trường tại thời điểm định giá. |
| `total_deduction_amount` | `NUMERIC(18,2)` | Yes | Tổng tiền giảm trừ. |
| `final_value_amount` | `NUMERIC(18,2)` | Yes | Giá trị cuối sau giảm trừ. |
| `currency_code` | `VARCHAR(3)` | Yes | Mã tiền tệ, mặc định `VND`. |
| `valuation_source` | `VARCHAR(100)` | No | Nguồn định giá. |
| `valued_at` | `TIMESTAMP` | Yes | Thời điểm định giá. |
| `valued_by` | `VARCHAR(100)` | No | Người/hệ thống định giá. |
| `note` | `TEXT` | No | Ghi chú. |

Quan hệ:

- `asset 1 - N asset_valuation`.
- `asset_valuation 1 - N asset_valuation_deduction`.

## `asset_valuation_deduction`

Các yếu tố giảm trừ đã áp dụng trong một lần định giá.

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `asset_valuation_id` | `UUID` | Yes | FK đến lần định giá. |
| `deduction_type_id` | `UUID` | Yes | FK đến loại giảm trừ. |
| `deduction_amount_snapshot` | `NUMERIC(18,2)` | Yes | Snapshot số tiền giảm trừ tại thời điểm định giá. |
| `note` | `TEXT` | No | Ghi chú. |
| `created_at` | `TIMESTAMP` | Yes | Thời điểm tạo. |

Quan hệ:

- `asset_valuation N - N asset_deduction_type` về nghiệp vụ, được thể hiện qua bảng này.

## `mock_score_grade_rule`

Rule demo để map hồ sơ vào `score_grade`.

| Column | Type | Required | Ý nghĩa |
|---|---|---:|---|
| `id` | `UUID` | Yes | Khóa kỹ thuật. |
| `rule_code` | `VARCHAR(50)` | Yes | Mã rule, unique. |
| `rule_name` | `VARCHAR(255)` | Yes | Tên rule. |
| `description` | `TEXT` | No | Mô tả. |
| `min_monthly_income_amount` | `NUMERIC(18,2)` | No | Thu nhập tháng tối thiểu. |
| `max_monthly_income_amount` | `NUMERIC(18,2)` | No | Thu nhập tháng tối đa. |
| `min_requested_amount` | `NUMERIC(18,2)` | No | Số tiền vay tối thiểu. |
| `max_requested_amount` | `NUMERIC(18,2)` | No | Số tiền vay tối đa. |
| `min_ltv_percent` | `NUMERIC(5,2)` | No | LTV tối thiểu. |
| `max_ltv_percent` | `NUMERIC(5,2)` | No | LTV tối đa. |
| `score_grade_id` | `UUID` | Yes | FK đến hạng điểm. |
| `is_active` | `BOOLEAN` | Yes | Có còn áp dụng hay không. |
| `sort_order` | `INT` | Yes | Thứ tự ưu tiên rule. |

Quan hệ:

- `score_grade 1 - N mock_score_grade_rule`.
