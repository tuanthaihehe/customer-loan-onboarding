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
| `seed/V1__seed_reference_data.sql`     | Seed dữ liệu tham chiếu: trạng thái hồ sơ vay và transition hợp lệ                   |
| `db/migration/V3__seed_demo_data.sql`  | Seed dữ liệu business để test API qua database thật                                 |

## Phạm vi schema hiện tại

Schema hiện tại phục vụ module tạo và xử lý hồ sơ vay:

| Bảng                                | Vai trò                                        |
| ----------------------------------- | ---------------------------------------------- |
| `customer`                          | Thông tin khách hàng                           |
| `loan_application`                  | Hồ sơ vay – object trung tâm của module        |
| `loan_application_state`            | Danh sách trạng thái hợp lệ của hồ sơ vay      |
| `loan_application_state_transition` | Các cặp chuyển trạng thái được phép            |
| `loan_application_state_history`    | Lịch sử chuyển trạng thái của từng hồ sơ vay   |

## Quy ước mã nghiệp vụ

| Đối tượng  | Format            | Ví dụ             |
| ---------- | ----------------- | ----------------- |
| Khách hàng | `CUS-YYYY-XXXXXX` | `CUS-2026-000001` |
| Hồ sơ vay  | `APP-YYYY-XXXXXX` | `APP-2026-000001` |

`*_code` là mã nghiệp vụ dùng để tra cứu và hiển thị. Khóa ngoại giữa các bảng luôn dùng `id` (UUID), không dùng `*_code`.

## Điểm thiết kế quan trọng

**Lifecycle chỉ cho `loan_application`**

`customer.status` là enum đơn giản dùng `CHECK constraint`. Chỉ `loan_application` có bộ bảng lifecycle riêng với lịch sử chuyển trạng thái. Xem chi tiết tại `lifecycle.md`.

## Chạy migration và seed

Migration và seed được viết theo quy ước Flyway (đặt tên `V{version}__{description}.sql`).

Thứ tự chạy:

```text
migrations/V1__init_schema.sql
seed/V1__seed_reference_data.sql
```

Backend hiện chạy mặc định với profile `db`, kết nối PostgreSQL thật và truy cập dữ liệu qua JPA Repository.

## Những gì chưa có trong schema hiện tại

Các bảng sau chờ team BA chốt workflow trước khi thiết kế:

| Bảng                | Phụ thuộc vào                   |
| ------------------- | ------------------------------- |
| `asset_valuation`   | Workflow định giá tài sản       |
| `eligibility_check` | Tiêu chí eligibility chính thức |
| `approval_case`     | Workflow phê duyệt hồ sơ        |
