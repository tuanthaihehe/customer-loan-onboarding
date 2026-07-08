# Loan Application Onboarding API Test Data

Ngày cập nhật: 2026-07-07

## API chuẩn sau khi bỏ Draft

Base path:

```text
/api/v1/loan-applications/onboarding
```

Không dùng nữa:

```text
/api/v1/loan-application-drafts
```

## 1. Khởi tạo hồ sơ vay

```http
POST /api/v1/loan-applications/onboarding
Content-Type: application/json
```

Body theo `customerCode`:

```json
{
  "customerCode": "CUS-000001"
}
```

Body theo `customerId`:

```json
{
  "customerId": "00000000-0000-0000-0000-000000000001"
}
```

Body nếu muốn hoàn thành luôn bước định danh khách hàng:

```json
{
  "customerCode": "CUS-000001",
  "customerIdentifyPayload": {
    "fullName": "Nguyen Van A",
    "identityNumber": "001234567890",
    "phoneNumber": "0901234567"
  }
}
```

Kỳ vọng:

- Không có `customerIdentifyPayload`: `applicationState = APP_CREATED`.
- Có `customerIdentifyPayload`: `applicationState = APP_IN_PROGRESS`.
- Response trả `applicationCode`; FE lưu mã này để gọi các API tiếp theo.

## 2. Lấy chi tiết hồ sơ để fill lại form

```http
GET /api/v1/loan-applications/onboarding/{applicationCode}
```

Kỳ vọng:

- Response có `currentStepCode`.
- Response có `steps[]`.
- FE fill form theo `steps[].stepCode` và `steps[].payload`.

## 3. Lấy danh sách hồ sơ onboarding

```http
GET /api/v1/loan-applications/onboarding
```

Filter theo trạng thái:

```http
GET /api/v1/loan-applications/onboarding?status=CREATED
GET /api/v1/loan-applications/onboarding?status=IN_PROGRESS
GET /api/v1/loan-applications/onboarding?status=COMPLETED
GET /api/v1/loan-applications/onboarding?status=SUBMITTED
GET /api/v1/loan-applications/onboarding?status=CANCELLED
GET /api/v1/loan-applications/onboarding?status=EXPIRED
```

## 4. Hoàn thành bước CUSTOMER_IDENTIFY

```http
POST /api/v1/loan-applications/onboarding/{applicationCode}/steps/CUSTOMER_IDENTIFY/complete
Content-Type: application/json
```

```json
{
  "payload": {
    "fullName": "Nguyen Van A",
    "identityNumber": "001234567890",
    "phoneNumber": "0901234567",
    "dateOfBirth": "1995-01-15"
  }
}
```

Kỳ vọng:

- Step `CUSTOMER_IDENTIFY` thành `COMPLETED`.
- Nếu hồ sơ đang `APP_CREATED`, backend chuyển sang `APP_IN_PROGRESS`.
- `currentStepCode` chuyển sang bước tiếp theo.

## 5. Hoàn thành bước PRELIMINARY_INFO

```http
POST /api/v1/loan-applications/onboarding/{applicationCode}/steps/PRELIMINARY_INFO/complete
Content-Type: application/json
```

```json
{
  "payload": {
    "preliminaryLoanPackage": {
      "requestedAmount": 20000000,
      "loanPurposeCode": "BUSINESS",
      "loanTermMonths": 12
    }
  }
}
```

Kỳ vọng:

- Backend lưu payload vào `loan_application_step_data`.
- Backend cập nhật các trường chính trên `loan_application` nếu payload có đủ dữ liệu: `requested_amount`, `loan_purpose_id`, `loan_term_id`, `loan_term_months`.

## 6. Hoàn thành bước CUSTOMER_ASSET_LOAN_PROPOSAL

```http
POST /api/v1/loan-applications/onboarding/{applicationCode}/steps/CUSTOMER_ASSET_LOAN_PROPOSAL/complete
Content-Type: application/json
```

```json
{
  "payload": {
    "selectedLoanOffer": {
      "loanProductCode": "LP_MOTORBIKE_STANDARD",
      "finalRequestedAmount": 18000000,
      "finalLoanTermMonths": 12,
      "loanPurposeCode": "BUSINESS"
    }
  }
}
```

Kỳ vọng:

- Backend cập nhật sản phẩm vay nếu `loanProductCode` tồn tại.
- Backend cập nhật số tiền vay, kỳ hạn, mục đích vay theo offer đã chọn.

## 7. Hoàn thành bước upload chứng từ

Tên step phụ thuộc seed `loan_application_step`. Với dữ liệu đã thiết kế trước đó có thể là:

```http
POST /api/v1/loan-applications/onboarding/{applicationCode}/steps/UPLOAD_COMPLETE/complete
Content-Type: application/json
```

```json
{
  "payload": {
    "documents": [
      {
        "documentTypeCode": "CUSTOMER_IDENTITY_FRONT",
        "fileUrl": "https://s3.example.com/los/applications/APP-2026-TEST/customer-id-front.jpg"
      },
      {
        "documentTypeCode": "ASSET_REGISTRATION",
        "fileUrl": "https://s3.example.com/los/applications/APP-2026-TEST/asset-registration.jpg"
      }
    ]
  }
}
```

Kỳ vọng:

- Step upload thành `COMPLETED`.
- Nếu tất cả step trong `loan_application_step` đã hoàn thành, hồ sơ chuyển sang `APP_COMPLETED`.

## 8. Nộp hồ sơ sang thẩm định

```http
POST /api/v1/loan-applications/onboarding/{applicationCode}/submit
```

Điều kiện:

- `applicationState = APP_COMPLETED`.
- Tất cả step đều `COMPLETED`.
- Không có step nào `requiresReview = true`.

Kỳ vọng:

- Hồ sơ chuyển sang `APP_SUBMITTED`.

## 9. Hủy hồ sơ

```http
POST /api/v1/loan-applications/onboarding/{applicationCode}/cancel
Content-Type: application/json
```

```json
{
  "note": "Khach hang khong tiep tuc vay"
}
```

Kỳ vọng:

- Hồ sơ chuyển sang `APP_CANCELLED`.
- Hồ sơ không còn được chỉnh sửa trong onboarding flow.

