<<<<<<< HEAD
# Customer & Loan Onboarding - Database Baseline
=======
# Customer & Loan Onboarding
>>>>>>> origin/phuc-db

Repository này là baseline backend cho đề tài **Customer & Loan Onboarding**. Từ phiên bản này backend mặc định chạy bằng database thật theo Flyway migration trong `backend/loan-onboarding/src/main/resources/db/migration`.

## Phạm vi hiện tại

Database hiện có do BA/DA cung cấp đang chốt các phần:

- `customer`: tra cứu khách hàng theo CCCD/số điện thoại/tên + ngày sinh.
- `loan_application`: tạo hồ sơ vay, lưu số tiền/kỳ hạn/mục đích vay/chi nhánh.
- `loan_application_state`, `loan_application_state_transition`, `loan_application_state_history`: lifecycle hồ sơ vay.
- OCR CCCD gọi FPT AI và trả dữ liệu cho FE tự điền form định danh.

`asset` và `asset_valuation` chưa có bảng trong migration hiện tại. Các endpoint asset/valuation vẫn giữ contract để FE nhìn thấy trên Swagger, nhưng sẽ trả lỗi nghiệp vụ tiếng Việt `ERR_SCHEMA_NOT_READY` cho tới khi BA/DA bổ sung migration tương ứng.

## Cách chạy

```powershell
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

## Tài liệu cần đọc

| Thứ tự | File | Mục đích |
|---:|---|---|
| 1 | `database/README.md` | Database scope hiện tại |
| 2 | `database/data-dictionary 1.md` | Field/constraint theo schema |
| 3 | `database/lifecycle 1.md` | Lifecycle hồ sơ vay |
| 4 | `docs/api/01_API_REQUEST_RESPONSE_SAMPLES.md` | Request/response mẫu theo DB thật |
| 5 | `03_OCR_CCCD.md` | Tài liệu OCR CCCD |

## Nguyên tắc phát triển tiếp

- Không sửa `.env`, `docker`, hoặc `db/migration` khi không có yêu cầu rõ.
- Không hard-code dữ liệu thay cho database/seed.
- Khi BA/DA thêm migration/seed mới, service và tài liệu phải cập nhật theo schema mới.
- Error message trả Swagger ưu tiên tiếng Việt để dễ test.
