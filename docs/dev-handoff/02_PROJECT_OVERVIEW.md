# Project Overview

Backend hiện là Spring Boot service chạy database thật theo Flyway migration.

## Trạng thái hiện tại

| Hạng mục | Trạng thái |
|---|---|
| Profile mặc định | `db` |
| Database | PostgreSQL + Flyway |
| Customer lookup | Đọc bảng `customer` |
| Loan application | Đọc/ghi `loan_application` và state history |
| OCR CCCD | Gọi FPT AI |
| Asset/valuation | Chưa có schema, trả `ERR_SCHEMA_NOT_READY` |

## Flow đang hỗ trợ

```text
Customer lookup
→ Create loan application draft
→ Save loan request
→ Complete preliminary step
→ Submit application
```

Submit chuyển hồ sơ từ `APP_DRAFT` sang `APP_SUBMITTED` theo transition trong database.
