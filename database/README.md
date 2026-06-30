# Database README

Folder này lưu tài liệu database do BA/DA cung cấp. Source migration đang được backend dùng trực tiếp nằm tại:

```text
backend/loan-onboarding/src/main/resources/db/migration
```

## Scope schema hiện tại

| Bảng | Vai trò |
|---|---|
| `customer` | Lưu thông tin khách hàng cơ bản |
| `loan_application` | Lưu hồ sơ vay |
| `loan_application_state` | Danh mục trạng thái hồ sơ vay |
| `loan_application_state_transition` | Cấu hình chuyển trạng thái hợp lệ |
| `loan_application_state_history` | Lịch sử lifecycle của hồ sơ vay |

Các migration bổ sung hiện có:

- `loan_term_months` trên `loan_application`.
- `loan_purpose` dạng controlled code qua CHECK constraint.
- `branch` trên `loan_application`.

## Seed hiện tại

| File | Nội dung |
|---|---|
| `seed/V1__seed_reference_data 1.sql` | State và transition của lifecycle |
| `seed/V2__seed_demo_business_data 1.sql` | Customer, loan application và state history mẫu |

## Lưu ý quan trọng

Schema hiện tại **chưa có bảng asset/asset valuation**. Backend không được tự dựng dữ liệu thay thế cho asset; khi cần phần này, BA/DA sẽ bổ sung migration/seed mới.

Các file trong `db/migration` của backend là nguồn chạy thật của team dev. Không sửa trực tiếp nếu không có yêu cầu rõ.
