# Project Overview

## 1. Tên đề tài

**Customer & Loan Onboarding**

## 2. Mục tiêu demo backend

Tạo backend đủ để demo luồng nhân viên PGD tạo một hồ sơ vay và gửi hồ sơ sang bước phê duyệt.

## 3. Đặc điểm kỹ thuật

| Hạng mục | Giá trị |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot |
| API docs | Swagger/OpenAPI |
| Profile mặc định | `mock` |
| Database | Chưa dùng thật |
| Build tool | Maven Wrapper |

## 4. Kiến trúc hiện tại

```text
Controller
→ Service interface
→ Mock service implementation
→ Rule skeleton / Mock data provider
→ DTO response
→ ApiResponse<T>
```

## 5. Không phải production backend

Project chưa xử lý:

- xác thực/phân quyền;
- transaction DB;
- persistence;
- approval decision thật;
- contract;
- disbursement;
- external integration.
