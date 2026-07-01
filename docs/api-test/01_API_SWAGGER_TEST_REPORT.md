# Swagger Manual Test Checklist

## URL

```text
Health:  http://localhost:8080/api/v1/health
Swagger: http://localhost:8080/swagger-ui/index.html
```

## Checklist chính

| Step | API | Expected result |
|---:|---|---|
| 0 | `GET /api/v1/health` | `Loan Onboarding API is running` |
| 1 | `POST /api/v1/customers/lookup` | Tìm customer từ bảng `customer` hoặc trả `found=false` |
| 2 | `POST /api/v1/loan-applications` | Tạo hồ sơ `APP_DRAFT` |
| 3 | `PATCH /api/v1/loan-applications/{applicationCode}/draft` | Lưu khoản vay, state vẫn `APP_DRAFT` |
| 4 | `GET /api/v1/loan-applications/{applicationCode}` | Trả detail từ DB |
| 5 | `POST /api/v1/loan-applications/{applicationCode}/steps/preliminary/complete` | `completed=true` nếu đủ amount/purpose/term |
| 6 | `POST /api/v1/loan-applications/{applicationCode}/submit-for-approval` | State thành `APP_SUBMITTED` |

## Checklist schema chưa hỗ trợ

Các API này phải trả lỗi nghiệp vụ tiếng Việt, không trả dữ liệu giả:

| API | Expected |
|---|---|
| `POST /api/v1/assets/lookup` | `success=false`, `errorCode=ERR_SCHEMA_NOT_READY` |
| `PATCH /api/v1/loan-applications/{applicationCode}/asset-snapshot` | `success=false`, `errorCode=ERR_SCHEMA_NOT_READY` |
| `POST /api/v1/loan-applications/{applicationCode}/asset-valuations/preview` | `success=false`, `errorCode=ERR_SCHEMA_NOT_READY` |
| `PATCH /api/v1/loan-applications/{applicationCode}/valuation-preview` | `success=false`, `errorCode=ERR_SCHEMA_NOT_READY` |

## Validation tối thiểu

| Case | API | Input lỗi | Expected |
|---:|---|---|---|
| V1 | Customer lookup | Thiếu `fullName` | `success=false`, `errorCode=ERR_400` |
| V2 | Create draft | Thiếu `customerCode` | `success=false`, `errorCode=ERR_400` |
| V3 | Save draft | `requestedAmount <= 0` | `success=false` |
| V4 | Save draft | `loanPurpose` không nằm trong enum migration | `success=false` |

## Tiêu chí pass

- Swagger mở được.
- Flyway validate/migrate thành công.
- Customer/loan application đọc ghi database thật.
- Asset/valuation không trả dữ liệu giả khi schema chưa có.
