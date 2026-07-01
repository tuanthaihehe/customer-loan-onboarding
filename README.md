# Customer & Loan Onboarding

Repository này là backend cho đề tài **Customer & Loan Onboarding**. Backend chạy theo hướng **database-first** với PostgreSQL, Flyway, JPA Entity và Spring Data Repository để thao tác trên dữ liệu thật.

## Phạm Vi Hiện Tại

Database do BA/DA cung cấp đang chốt các phần chính:

- `customer`: tra cứu khách hàng theo CCCD, số điện thoại hoặc tên + ngày sinh.
- `loan_application`: tạo hồ sơ vay, lưu số tiền, kỳ hạn, mục đích vay, chi nhánh và tài sản.
- `loan_purpose`, `loan_term`: danh mục mục đích vay và kỳ hạn vay cho dropdown/frontend.
- `loan_application_state`, `loan_application_state_transition`, `loan_application_state_history`: lifecycle hồ sơ vay.
- Nhóm vehicle catalog: loại xe, hãng xe, dòng xe, phiên bản, năm sản xuất, màu, biến thể và giá thị trường.
- `asset`, `asset_valuation`, `asset_valuation_deduction`: lưu tài sản và kết quả định giá.
- OCR CCCD gọi FPT AI và trả dữ liệu cho FE tự điền form định danh.

Theo cập nhật BA/DA mới nhất, **không làm Eligibility** vì không có trong database.

## Cách Chạy

```powershell
docker compose up -d postgres
cd backend/loan-onboarding
.\mvnw clean test
.\mvnw spring-boot:run
```

Backend mặc định dùng profile `db`:

```properties
spring.profiles.default=db
```

URL:

```text
Health:  http://localhost:8080/api/v1/health
Swagger: http://localhost:8080/swagger-ui/index.html
```

## Tài Liệu Cần Đọc

| Thứ tự | File | Mục đích |
| -----: | ---- | -------- |
| 1 | `database/README.md` | Database scope hiện tại |
| 2 | `database/data-dictionary.md` | Field/constraint theo schema |
| 3 | `database/lifecycle.md` | Lifecycle hồ sơ vay |
| 4 | `docs/api/01_API_REQUEST_RESPONSE_SAMPLES.md` | Request/response mẫu theo DB thật |
| 5 | `03_OCR_CCCD.md` | Tài liệu OCR CCCD |
| 6 | `docs/02_WORK_LOG_2026_06_30.md` | Work log ngày 2026-06-30 |

## Nguyên Tắc Phát Triển Tiếp

- Không sửa `.env`, docker hoặc migration/seed khi không có yêu cầu rõ.
- Không hard-code dữ liệu nghiệp vụ thay cho database/seed.
- Không thêm lại mock data provider hoặc mock service.
- Service xử lý nghiệp vụ, Repository xử lý truy vấn JPA/JPQL.
- Khi BA/DA thêm migration/seed mới, service và tài liệu phải cập nhật theo schema mới.
- Error message trả Swagger ưu tiên tiếng Việt để dễ test.
