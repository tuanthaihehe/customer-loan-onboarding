# Customer Loan Onboarding Database

Tài liệu này mô tả schema database hiện tại cho module Customer Loan Onboarding.

## Phạm Vi Hiện Tại

Schema hiện tại phục vụ các phần chính:

- lưu thông tin khách hàng cơ bản để tra cứu;
- tạo và quản lý hồ sơ vay;
- quản lý lifecycle của hồ sơ vay;
- quản lý danh mục mục đích vay và kỳ hạn vay;
- quản lý danh mục xe, tài sản xe gắn với hồ sơ vay;
- định giá tài sản và các khoản giảm trừ.

Theo cập nhật BA/DA mới nhất, database **không có Eligibility** nên backend không làm chức năng Eligibility.

## Danh Sách File

```text
.
├── data-dictionary.md
├── erd.dbml
├── lifecycle.md
├── migrations
│   ├── V1__init_schema.sql
│   ├── V2__add_loan_terms_and_purpose_enum.sql
│   ├── V3__add_branch.sql
│   ├── V4__add_vehicle_catalog_and_asset_revised.sql
│   ├── V5__link_asset_to_loan_application.sql
│   ├── V6__add_asset_valuation_and_deductions.sql
│   ├── V7__update_customer_status_enum.sql
│   ├── V8__add_loan_purpose_catalog.sql
│   ├── V9__add_loan_term_catalog.sql
│   ├── V10__add_lead_customer_status.sql
│   ├── V11__add_loan_product_catalog.sql
│   ├── V12__add_loan_application_reference_person.sql
│   └── V13__add_preliminary_applicant_snapshot.sql
└── seed
    ├── V1__seed_reference_data.sql
    ├── V2__seed_demo_business_data.sql
    ├── V3__seed_vehicle_catalog_and_asset.sql
    ├── V4__seed_asset_deduction_type.sql
    ├── V5__seed_loan_purpose.sql
    ├── V6__seed_loan_term.sql
    └── V7__seed_loan_product.sql
```

## Bảng Chính

| Bảng | Vai trò |
|---|---|
| `customer` | Lưu thông tin khách hàng cơ bản phục vụ tra cứu và tạo hồ sơ vay. |
| `loan_application` | Lưu hồ sơ vay, bao gồm thông tin khoản vay, chi nhánh và tài sản được chọn. |
| `loan_purpose` | Danh mục mục đích vay cho dropdown/frontend. |
| `loan_term` | Danh mục kỳ hạn vay cho dropdown/frontend. |
| `loan_application_state` | Danh mục state hợp lệ của hồ sơ vay. |
| `loan_application_state_transition` | Cấu hình state nào được phép chuyển sang state nào. |
| `loan_application_state_history` | Nhật ký lifecycle của từng hồ sơ vay. |
| `vehicle_type`, `vehicle_brand`, `vehicle_model`, `vehicle_version`, `vehicle_year`, `vehicle_color`, `vehicle_variant` | Danh mục xe phục vụ dropdown và định giá. |
| `vehicle_market_price` | Giá thị trường theo biến thể xe và thời gian hiệu lực. |
| `asset` | Tài sản xe được chọn trong ngữ cảnh hồ sơ vay. |
| `asset_deduction_type` | Danh mục khoản giảm trừ định giá. |
| `asset_valuation` | Kết quả định giá tài sản. |
| `asset_valuation_deduction` | Các khoản giảm trừ được áp dụng cho một lần định giá. |

## Ghi Chú Thiết Kế

- `loan_application.loan_purpose_id`, `loan_term_id`, `loan_term_months`, `branch`, `asset_id` cho phép `NULL` vì hồ sơ có thể được tạo ở trạng thái nháp.
- `loan_application.loan_term_months` lưu snapshot số tháng thực tế đã chọn.
- Các mốc thời gian lifecycle được ghi nhận trong `loan_application_state_history.changed_at`.
- `loan_application.asset_id` nullable để tạo hồ sơ trước, chọn tài sản sau.
- `vehicle_market_price` lưu giá theo `vehicle_variant` và ngày hiệu lực.
- `asset_valuation.final_value_amount` bằng `market_price_amount - total_deduction_amount`.
- `V2__seed_demo_business_data.sql` chỉ dùng để tạo dữ liệu demo cho việc kiểm tra backend và database.
