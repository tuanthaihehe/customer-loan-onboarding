# Loan Onboarding Backend

Spring Boot backend cho demo **Customer & Loan Onboarding - Flow 1**.

## 1. Mục tiêu

Backend này dùng để demo:

```text
Tạo hồ sơ vay nháp → bổ sung thông tin khoản vay → kiểm tra eligibility → gửi đi phê duyệt
```

Không phải production backend hoàn chỉnh.

## 2. Tech stack

| Thành phần | Phiên bản/Ghi chú |
|---|---|
| Java | 21 |
| Spring Boot | 3.x |
| Build | Maven Wrapper |
| API docs | Springdoc OpenAPI / Swagger |
| Default profile | `db` |
| Database | PostgreSQL/Flyway/JPA Repository |

## 3. Run

```powershell
.\mvnw clean test
.\mvnw spring-boot:run
```

Mở:

```text
http://localhost:8080/api/v1/health
http://localhost:8080/swagger-ui/index.html
```

## 4. Package structure

```text
com.f88.loanonboarding
├── common          # ApiResponse, ErrorCode
├── config          # OpenAPI, local CORS
├── controller      # REST API
├── dto             # request/response DTO
├── enums           # state/result enum
├── exception       # exception handler
├── rule            # rule skeleton/demo guard
├── service         # service interface
├── service.impl    # service implementation chuẩn, gọi repository
├── entity          # JPA entity map với bảng hiện có
├── repository      # Spring Data JPA repository/query riêng
└── mapper          # placeholder, chưa dùng thật
```

## 5. Endpoint chính của Flow 1

| Step | API |
|---:|---|
| 0 | `GET /api/v1/health` |
| 1 | `POST /api/v1/customers/lookup` |
| 2 | `POST /api/v1/loan-applications` |
| 3 | `PATCH /api/v1/loan-applications/{applicationCode}/draft` |
| 4 | `POST /api/v1/assets/lookup` |
| 5 | `PATCH /api/v1/loan-applications/{applicationCode}/asset-snapshot` |
| 6 | `POST /api/v1/loan-applications/{applicationCode}/asset-valuations/preview` |
| 7 | `PATCH /api/v1/loan-applications/{applicationCode}/valuation-preview` |
| 8 | `POST /api/v1/loan-applications/{applicationCode}/eligibility-checks` |
| 9 | `POST /api/v1/loan-applications/{applicationCode}/submit-for-approval` |

## 6. Scope chưa làm

- Unit test rule chi tiết: chưa cần vì rule chưa chốt.
- Controller test chi tiết: chưa cần, dùng Swagger checklist trước.
- Rule registry: chưa cần.
- Entity/repository thật: chờ ERD.
- Approval/contract/disbursement full: ngoài Flow 1.

## 7. Config profile

Mặc định:

```properties
spring.profiles.default=db
```

Profile `db` kết nối PostgreSQL theo `application-db.properties`.
