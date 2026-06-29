# Database

Thư mục này chứa toàn bộ tài liệu thiết kế dữ liệu, schema migration và seed data cho module **Customer & Loan Onboarding**.

## Nội dung thư mục

| File / Thư mục                         | Mục đích                                                                             |
| -------------------------------------- | ------------------------------------------------------------------------------------ |
| `erd.dbml`                             | ERD dạng DBML – import vào [dbdiagram.io](https://dbdiagram.io) để xem sơ đồ quan hệ |
| `data-dictionary.md`                   | Mô tả chi tiết từng bảng, cột, constraint và business rule                           |
| `lifecycle.md`                         | Giải thích thiết kế lifecycle state machine của `loan_application`                   |
| `migrations/V1__init_schema.sql`       | Migration khởi tạo toàn bộ schema (PostgreSQL)                                       |
| `seed/V1__seed_reference_data.sql`     | Seed dữ liệu tham chiếu: trạng thái hồ sơ vay và transition hợp lệ                   |
| `seed/V2__seed_demo_business_data.sql` | Seed dữ liệu demo: khách hàng, tài sản, hồ sơ vay, lịch sử chuyển trạng thái         |

## Phạm vi schema hiện tại

Schema gồm 6 bảng phục vụ module tạo và xử lý hồ sơ vay:

| Bảng                                | Vai trò                                        |
| ----------------------------------- | ---------------------------------------------- |
| `customer`                          | Thông tin khách hàng                           |
| `asset`                             | Thông tin tài sản (phương tiện) của khách hàng |
| `loan_application`                  | Hồ sơ vay – object trung tâm của module        |
| `loan_application_state`            | Danh sách trạng thái hợp lệ của hồ sơ vay      |
| `loan_application_state_transition` | Các cặp chuyển trạng thái được phép            |
| `loan_application_state_history`    | Lịch sử chuyển trạng thái của từng hồ sơ vay   |

## Quy ước mã nghiệp vụ

| Đối tượng  | Format            | Ví dụ             |
| ---------- | ----------------- | ----------------- |
| Khách hàng | `CUS-YYYY-XXXXXX` | `CUS-2026-000001` |
| Tài sản    | `AST-YYYY-XXXXXX` | `AST-2026-000001` |
| Hồ sơ vay  | `APP-YYYY-XXXXXX` | `APP-2026-000001` |

`*_code` là mã nghiệp vụ dùng để tra cứu và hiển thị. Khóa ngoại giữa các bảng luôn dùng `id` (UUID), không dùng `*_code`.

## Điểm thiết kế quan trọng

**`loan_application.asset_id` cho phép NULL**

Hồ sơ vay nháp có thể được tạo trước khi chọn tài sản. `asset_id` chỉ bắt buộc có khi nộp hồ sơ. Xem chi tiết tại `data-dictionary.md`.

**Partial unique index cho tài sản**

Một tài sản không được gắn vào nhiều hồ sơ vay đang mở cùng lúc. Ràng buộc này được thực thi bằng partial unique index:

```sql
CREATE UNIQUE INDEX uq_active_loan_application_asset
ON loan_application(asset_id)
WHERE asset_id IS NOT NULL
  AND closed_at IS NULL
  AND deleted_at IS NULL;
```

**Lifecycle chỉ cho `loan_application`**

`customer.status` và `asset.status` là enum đơn giản dùng `CHECK constraint`. Chỉ `loan_application` có bộ bảng lifecycle riêng với lịch sử chuyển trạng thái. Xem chi tiết tại `lifecycle.md`.

## Chạy migration và seed

Migration và seed được viết theo quy ước Flyway (đặt tên `V{version}__{description}.sql`).

Thứ tự chạy:

```text
migrations/V1__init_schema.sql
seed/V1__seed_reference_data.sql
seed/V2__seed_demo_business_data.sql
```

Backend hiện tại đang chạy với profile `mock` và chưa kết nối database thật. Schema này sẽ được dùng khi team chuyển sang profile `db`.

## Những gì chưa có trong schema hiện tại

Các bảng sau chờ team BA chốt workflow trước khi thiết kế:

| Bảng                | Phụ thuộc vào                   |
| ------------------- | ------------------------------- |
| `asset_valuation`   | Workflow định giá tài sản       |
| `eligibility_check` | Tiêu chí eligibility chính thức |
| `approval_case`     | Workflow phê duyệt hồ sơ        |
