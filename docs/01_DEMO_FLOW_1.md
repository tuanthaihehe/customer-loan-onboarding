# Flow Hiện Tại Theo Database

## Mục tiêu

Backend hiện hỗ trợ luồng tối thiểu dựa trên schema thật:

```text
OCR CCCD hoặc nhập thông tin KH
→ Tra cứu customer trong database
→ Tạo loan_application nháp
→ Lưu thông tin khoản vay
→ Gửi hồ sơ từ APP_DRAFT sang APP_SUBMITTED
```

## API sequence chạy được

| Step | API | Expected result |
|---:|---|---|
| 0 | `GET /api/v1/health` | Backend running |
| 1 | `POST /api/v1/customers/ocr/extract` | OCR trả thông tin định danh nếu FPT AI nhận diện được |
| 2 | `POST /api/v1/customers/lookup` | Đọc bảng `customer` |
| 3 | `POST /api/v1/loan-applications` | Tạo `loan_application` state `APP_DRAFT` |
| 4 | `PATCH /api/v1/loan-applications/{applicationCode}/draft` | Cập nhật `requested_amount`, `loan_purpose`, `loan_term_months` |
| 5 | `GET /api/v1/loan-applications/{applicationCode}` | Đọc chi tiết từ DB |
| 6 | `POST /api/v1/loan-applications/{applicationCode}/steps/preliminary/complete` | Kiểm tra dữ liệu khoản vay tối thiểu |
| 7 | `POST /api/v1/loan-applications/{applicationCode}/submit-for-approval` | Chuyển state sang `APP_SUBMITTED` và ghi history `SUBMIT` |

## API giữ contract nhưng chưa có schema

Các API sau chưa có bảng trong migration hiện tại, nên trả `ERR_SCHEMA_NOT_READY`:

| API | Lý do |
|---|---|
| `POST /api/v1/assets/lookup` | Chưa có bảng asset |
| `PATCH /api/v1/loan-applications/{applicationCode}/asset-snapshot` | `loan_application` chưa có asset reference |
| `POST /api/v1/loan-applications/{applicationCode}/asset-valuations/preview` | Chưa có bảng định giá |
| `PATCH /api/v1/loan-applications/{applicationCode}/valuation-preview` | Chưa có bảng định giá |

## Dữ liệu seed có sẵn

| Object | Ví dụ |
|---|---|
| Customer | `CUS-2026-000001`, `CUS-2026-000002`, ... |
| Loan application | `APP-2026-000001` đến `APP-2026-000005` |
| State | `APP_DRAFT`, `APP_SUBMITTED`, `APP_NEEDS_SUPPLEMENT`, `APP_IN_REVIEW`, `APP_READY_FOR_CONTRACT`, `APP_CONTRACTED`, `APP_CANCELLED` |

Khi tạo hồ sơ mới, backend sinh mã dạng `APP-2026-XXXXXX` dựa trên mã lớn nhất trong database.
