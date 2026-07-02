# Customer Loan Onboarding Database

Tài liệu này mô tả schema database hiện tại cho module Customer Loan Onboarding.

## Phạm Vi Hiện Tại

Schema hiện tại tập trung vào:

- lưu thông tin khách hàng cơ bản để tra cứu;
- tạo và quản lý hồ sơ vay;
- quản lý lifecycle của hồ sơ vay;
- quản lý danh mục mục đích vay, kỳ hạn vay và sản phẩm vay;
- quản lý danh mục xe, tài sản xe gắn với hồ sơ vay;
- định giá tài sản và các khoản giảm trừ;
- lưu thông tin người tham chiếu, snapshot thông tin sơ bộ và chứng từ hồ sơ vay.

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
│   ├── V13__add_preliminary_applicant_snapshot.sql
│   ├── V14__add_customer_bank_occupation_extra_fields.sql
│   ├── V15__add_vehicle_identifier_fields_to_asset.sql
│   ├── V16__add_mock_score_grade_rule.sql
│   └── V17__add_loan_application_document.sql
└── seed
    ├── V1__seed_reference_data.sql
    ├── V2__seed_demo_business_data.sql
    ├── V3__seed_vehicle_catalog_and_asset.sql
    ├── V4__seed_asset_deduction_type.sql
    ├── V5__seed_loan_purpose.sql
    ├── V6__seed_loan_term.sql
    ├── V7__seed_loan_product.sql
    ├── V8__seed_bank_and_occupation.sql
    ├── V9__seed_mock_score_grade_rule.sql
    └── V10__seed_document_type.sql
```

## Bảng Chính

| Bảng | Vai trò |
|---|---|
| `customer` | Lưu thông tin khách hàng cơ bản phục vụ tra cứu và tạo hồ sơ vay. |
| `loan_application` | Lưu hồ sơ vay, bao gồm thông tin khoản vay, snapshot khách hàng sơ bộ, chi nhánh và tài sản được chọn. |
| `loan_purpose` | Danh mục mục đích vay cho dropdown/frontend. |
| `loan_term` | Danh mục kỳ hạn vay cho dropdown/frontend. |
| `loan_product` | Danh mục sản phẩm vay và điều kiện áp dụng. |
| `score_grade` | Danh mục hạng điểm phục vụ rule/đề xuất sản phẩm vay. |
| `loan_application_state` | Danh mục state hợp lệ của hồ sơ vay. |
| `loan_application_state_transition` | Cấu hình state nào được phép chuyển sang state nào. |
| `loan_application_state_history` | Nhật ký lifecycle của từng hồ sơ vay. |
| `asset` | Lưu tài sản xe ở mức hồ sơ. |
| `asset_valuation` | Lưu kết quả định giá tài sản. |
| `asset_valuation_deduction` | Lưu các yếu tố giảm trừ đã áp dụng trong một lần định giá. |
| `document_type` | Danh mục loại chứng từ cần upload. |
| `loan_application_document` | Lưu chứng từ của hồ sơ vay. |

## Ghi Chú Thiết Kế

- `loan_application.loan_purpose_id`, `loan_term_id`, `loan_term_months`, `branch`, `asset_id` cho phép `NULL` vì hồ sơ có thể được tạo ở trạng thái nháp.
- `loan_application.loan_term_months` lưu snapshot số tháng thực tế đã chọn.
- Các trường `loan_application.applicant_*` lưu snapshot thông tin sơ bộ tại thời điểm nhân viên lưu hồ sơ.
- Các mốc thời gian lifecycle được ghi nhận trong `loan_application_state_history.changed_at`.
- `loan_application.asset_id` nullable để tạo hồ sơ trước, chọn tài sản sau.
- `vehicle_market_price` lưu giá theo `vehicle_variant` và ngày hiệu lực.
- `asset_valuation.final_value_amount` bằng `market_price_amount - total_deduction_amount`.
- `V2__seed_demo_business_data.sql` chỉ dùng để tạo dữ liệu demo cho việc kiểm tra backend và database.

## Cách chạy SQL bằng Docker

Backend đang chạy Flyway theo thứ tự trong `backend/loan-onboarding/src/main/java/com/f88/loanonboarding/config/DatabaseMigrationConfig.java`. Khi chạy backend với profile `db`, hệ thống sẽ tự chạy migration và seed theo các mốc đã cấu hình.
