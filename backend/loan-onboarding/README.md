# Loan Onboarding Backend

Spring Boot backend cho **Customer & Loan Onboarding**. Backend hiện chạy với database thật qua Flyway migration, JPA Entity và Spring Data Repository.

## Tech Stack

| Thành phần | Ghi chú |
|---|---|
| Java | 21 |
| Spring Boot | 3.x |
| Build | Maven Wrapper |
| API docs | Springdoc OpenAPI / Swagger |
| Default profile | `db` |
| Database | PostgreSQL + Flyway + JPA Repository |

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

## Package Structure

```text
com.f88.loanonboarding
├── common          # ApiResponse, ErrorCode
├── config          # OpenAPI, local CORS, RestTemplate, Flyway runner
├── controller      # REST API
├── dto             # request/response DTO
├── entity          # JPA entity map với database thật
├── enums           # state/result enum
├── exception       # exception handler
├── repository      # Spring Data JPA repository/query riêng
├── rule            # rule guard tối thiểu
├── service         # service interface
└── service.impl    # service implementation chuẩn, gọi repository
```

## API Trạng Thái Hiện Tại

| Nhóm API | Trạng thái |
|---|---|
| Health | Chạy được |
| Customer lookup | Đọc bảng `customer` |
| OCR CCCD | Gọi FPT AI |
| Loan application | Đọc/ghi `loan_application`, state history, loan purpose, loan term |
| Reference data | Đọc catalog thật từ DB |
| Asset | Đọc/ghi `asset` và gắn với hồ sơ vay |
| Asset valuation | Tính market price, preview valuation và lưu valuation |
| Eligibility | Không làm theo chốt BA/DA |

## Profile

```properties
spring.profiles.default=db
```

Profile `db` kết nối PostgreSQL theo `application-db.properties` và có thể override bằng biến môi trường `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
