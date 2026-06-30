# Loan Onboarding Backend

Spring Boot backend cho **Customer & Loan Onboarding**. Backend hiện chạy với database thật qua Flyway migration.

## Tech stack

| Thành phần | Ghi chú |
|---|---|
| Java | 21 |
| Spring Boot | 3.x |
| Build | Maven Wrapper |
| API docs | Springdoc OpenAPI / Swagger |
| Default profile | `db` |
| Database | PostgreSQL + Flyway |

## Run

```powershell
.\mvnw clean test
.\mvnw spring-boot:run
```

Mở:

```text
http://localhost:8080/api/v1/health
http://localhost:8080/swagger-ui/index.html
```

## Package structure

```text
com.f88.loanonboarding
├── common          # ApiResponse, ErrorCode
├── config          # OpenAPI, local CORS, RestTemplate
├── controller      # REST API
├── dto             # request/response DTO
├── enums           # state/result enum
├── exception       # exception handler
├── rule            # rule guard tối thiểu
├── service         # service interface
└── service.impl    # DB service implementation
```

## API trạng thái hiện tại

| Nhóm API | Trạng thái |
|---|---|
| Health | Chạy được |
| Customer lookup | Đọc bảng `customer` |
| OCR CCCD | Gọi FPT AI |
| Loan application | Đọc/ghi `loan_application` và state history |
| Reference data loan purpose | Theo CHECK constraint trong migration |
| Asset/valuation | Chưa có schema, trả `ERR_SCHEMA_NOT_READY` |

## Profile

```properties
spring.profiles.default=db
```

Kết nối DB lấy từ `application-db.properties` và có thể override bằng biến môi trường `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
