# Customer Loan Onboarding Database

Tài liệu này mô tả schema database hiện tại cho module Customer Loan Onboarding.

## Phạm vi hiện tại

Schema hiện tại phục vụ các phần chính:

- lưu thông tin khách hàng cơ bản để tra cứu;
- tạo và quản lý hồ sơ vay;
- quản lý lifecycle của hồ sơ vay;
- danh mục xe, tài sản xe gắn với hồ sơ vay;
- định giá tài sản và các khoản giảm trừ.

## Danh sách file

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
│   └── V7__update_customer_status_enum.sql
└── seed
    ├── V1__seed_reference_data.sql
    ├── V2__seed_demo_business_data.sql
    ├── V3__seed_vehicle_catalog_and_asset.sql
    └── V4__seed_asset_deduction_type.sql
```

## Bảng chính

| Bảng | Vai trò |
|---|---|
| `customer` | Lưu thông tin khách hàng cơ bản phục vụ tra cứu và tạo hồ sơ vay. |
| `loan_application` | Lưu hồ sơ vay, bao gồm thông tin khoản vay, chi nhánh và tài sản được chọn. |
| `loan_application_state` | Danh mục state hợp lệ của hồ sơ vay. |
| `loan_application_state_transition` | Cấu hình state nào được phép chuyển sang state nào. |
| `loan_application_state_history` | Nhật ký lifecycle của từng hồ sơ vay. |
| `vehicle_type`, `vehicle_brand`, `vehicle_model`, `vehicle_version`, `vehicle_year`, `vehicle_color`, `vehicle_variant` | Danh mục xe phục vụ dropdown và định giá. |
| `vehicle_market_price` | Giá thị trường theo biến thể xe và thời gian hiệu lực. |
| `asset` | Tài sản xe được chọn trong ngữ cảnh hồ sơ vay. |
| `asset_deduction_type` | Danh mục khoản giảm trừ định giá. |
| `asset_valuation` | Kết quả định giá tài sản. |
| `asset_valuation_deduction` | Các khoản giảm trừ được áp dụng cho một lần định giá. |

## Ghi chú thiết kế

- `loan_application.requested_amount`, `loan_purpose`, `loan_term_months`, `branch`, `asset_id` cho phép `NULL` vì hồ sơ có thể được tạo ở trạng thái nháp.
- Các mốc thời gian lifecycle được ghi nhận trong `loan_application_state_history.changed_at`.
- `loan_application.asset_id` nullable để tạo hồ sơ trước, chọn tài sản sau.
- `asset` hiện không lưu `customer_id`; khách hàng của tài sản được suy ra qua `asset -> loan_application -> customer`.
- `vehicle_market_price` lưu giá theo `vehicle_variant` và ngày hiệu lực.
- `asset_valuation.final_value_amount` luôn bằng `market_price_amount - total_deduction_amount`.
- `V2__seed_demo_business_data.sql` chỉ dùng để tạo dữ liệu demo cho việc kiểm tra backend và database.

## Thứ tự chạy SQL bằng Docker

Nếu PostgreSQL container tên là `los-postgres`:

```bash
docker exec -i los-postgres psql -U postgres -d loan_onboarding < database/migrations/V1__init_schema.sql
docker exec -i los-postgres psql -U postgres -d loan_onboarding < database/migrations/V2__add_loan_terms_and_purpose_enum.sql
docker exec -i los-postgres psql -U postgres -d loan_onboarding < database/migrations/V3__add_branch.sql
docker exec -i los-postgres psql -U postgres -d loan_onboarding < database/migrations/V4__add_vehicle_catalog_and_asset_revised.sql
docker exec -i los-postgres psql -U postgres -d loan_onboarding < database/migrations/V5__link_asset_to_loan_application.sql
docker exec -i los-postgres psql -U postgres -d loan_onboarding < database/migrations/V6__add_asset_valuation_and_deductions.sql
docker exec -i los-postgres psql -U postgres -d loan_onboarding < database/migrations/V7__update_customer_status_enum.sql
docker exec -i los-postgres psql -U postgres -d loan_onboarding < database/seed/V1__seed_reference_data.sql
docker exec -i los-postgres psql -U postgres -d loan_onboarding < database/seed/V2__seed_demo_business_data.sql
docker exec -i los-postgres psql -U postgres -d loan_onboarding < database/seed/V3__seed_vehicle_catalog_and_asset.sql
docker exec -i los-postgres psql -U postgres -d loan_onboarding < database/seed/V4__seed_asset_deduction_type.sql
```
