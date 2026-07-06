# Loan Application Draft API Test Data

Tài liệu này mô tả luồng API hồ sơ vay nháp nhiều bước cho frontend test.

Nguyên tắc chính:

```text
Customer được tạo/quản lý bởi Customer API riêng.
Loan Application Draft API không tạo Customer mới.
Frontend truyền customerId hoặc customerCode đã tồn tại để tạo draft.
Sau khi có draftCode, toàn bộ bước sau chỉ dùng draftCode.
```

## 1. Tạo hồ sơ vay nháp

```http
POST /api/v1/loan-application-drafts
```

Request mẫu dùng dữ liệu seed:

```json
{
  "customerCode": "CUS-2026-000001",
  "customerIdentifyPayload": {
    "source": "CUSTOMER_API",
    "customerCode": "CUS-2026-000001",
    "fullName": "Nguyen Van An",
    "phoneNumber": "0901000001",
    "identityNumber": "001201000001",
    "dateOfBirth": "1995-01-15",
    "ocrRequestId": "OCR-TEST-000001"
  }
}
```

Kỳ vọng:

```text
- Tạo loan_application_draft.
- Gắn customer_id theo customerCode.
- Tạo đủ 6 step data.
- CUSTOMER_IDENTIFY = COMPLETED.
- currentStepCode = PRELIMINARY_INFO.
```

Lấy `draftCode` trong response để gọi các API sau.

## 2. Lưu nháp bước thông tin sơ bộ

```http
PUT /api/v1/loan-application-drafts/{draftCode}/steps/PRELIMINARY_INFO
```

Request:

```json
{
  "status": "IN_PROGRESS",
  "payload": {
    "loanRequest": {
      "loanPurposeCode": "PERSONAL_CONSUMPTION",
      "requestedAmount": 30000000,
      "termMonths": 12
    }
  }
}
```

Kỳ vọng:

```text
- PRELIMINARY_INFO = IN_PROGRESS.
- currentStepCode = PRELIMINARY_INFO.
- Chưa chuyển bước.
```

## 3. Hoàn thành bước thông tin sơ bộ

```http
POST /api/v1/loan-application-drafts/{draftCode}/steps/PRELIMINARY_INFO/complete
```

Request:

```json
{
  "payload": {
    "loanRequest": {
      "loanPurposeCode": "PERSONAL_CONSUMPTION",
      "requestedAmount": 30000000,
      "termMonths": 12
    }
  }
}
```

Kỳ vọng:

```text
- PRELIMINARY_INFO = COMPLETED.
- currentStepCode = CUSTOMER_DETAIL.
```

## 4. Hoàn thành bước chi tiết khách hàng

```http
POST /api/v1/loan-application-drafts/{draftCode}/steps/CUSTOMER_DETAIL/complete
```

Request:

```json
{
  "payload": {
    "customerProfile": {
      "gender": "MALE",
      "email": "an.nguyen.demo@example.com",
      "maritalStatus": "MARRIED",
      "permanentAddress": "24 Tran Duy Hung, Cau Giay, Ha Noi",
      "currentAddress": "24 Tran Duy Hung, Cau Giay, Ha Noi"
    },
    "employment": {
      "occupationCode": "OFFICE_WORKER",
      "incomeSourceCode": "SALARY",
      "monthlyIncomeAmount": 25000000,
      "workplaceName": "F88 Demo",
      "workplaceAddress": "Ha Noi"
    },
    "disbursement": {
      "bankCode": "VCB",
      "accountNumber": "0011000888999",
      "accountName": "NGUYEN VAN AN"
    }
  }
}
```

Kỳ vọng:

```text
- CUSTOMER_DETAIL = COMPLETED.
- currentStepCode = ASSET_DETAIL.
```

## 5. Hoàn thành bước tài sản

```http
POST /api/v1/loan-application-drafts/{draftCode}/steps/ASSET_DETAIL/complete
```

Request:

```json
{
  "payload": {
    "asset": {
      "assetType": "MOTORBIKE",
      "vehicleVariantCode": "YAMAHA_JUPITER_PREMIUM_2022_SILVER",
      "vehicleName": "Yamaha Jupiter Premium 2022 Bac",
      "licensePlate": "29A12346",
      "registrationCertificateNumber": "DKX-TEST-000001"
    },
    "valuation": {
      "marketValue": 28000000,
      "totalDeductionAmount": 0,
      "finalValue": 28000000
    }
  }
}
```

Kỳ vọng:

```text
- ASSET_DETAIL = COMPLETED.
- currentStepCode = FINAL_LOAN_PROPOSAL.
```

## 6. Hoàn thành bước đề xuất gói vay cuối

```http
POST /api/v1/loan-application-drafts/{draftCode}/steps/FINAL_LOAN_PROPOSAL/complete
```

Request:

```json
{
  "payload": {
    "proposal": {
      "selectedProductCode": "XM_STANDARD",
      "requestedAmount": 30000000,
      "approvedAmount": 30000000,
      "termMonths": 12,
      "ltvPercent": 60.0
    }
  }
}
```

Kỳ vọng:

```text
- FINAL_LOAN_PROPOSAL = COMPLETED.
- currentStepCode = UPLOAD_COMPLETE.
```

Nếu môi trường không có product code `XM_STANDARD`, submit vẫn tạo hồ sơ vay thật nhưng bỏ qua `loanProduct`.

## 7. Hoàn thành bước upload hồ sơ

```http
POST /api/v1/loan-application-drafts/{draftCode}/steps/UPLOAD_COMPLETE/complete
```

Request:

```json
{
  "payload": {
    "documents": [
      {
        "documentTypeCode": "CITIZEN_ID_FRONT",
        "fileId": "file-cccd-front-001",
        "fileName": "cccd_front.jpg"
      },
      {
        "documentTypeCode": "CITIZEN_ID_BACK",
        "fileId": "file-cccd-back-001",
        "fileName": "cccd_back.jpg"
      }
    ],
    "checklist": {
      "requiredDocumentCount": 2,
      "uploadedRequiredDocumentCount": 2,
      "canCompleteDraft": true
    }
  }
}
```

Kỳ vọng:

```text
- UPLOAD_COMPLETE = COMPLETED.
- draft.status = COMPLETED.
- Draft sẵn sàng submit.
```

## 8. Load lại hồ sơ nháp để fill UI

```http
GET /api/v1/loan-application-drafts/{draftCode}
```

Frontend dùng:

```text
currentStepCode để mở đúng màn hình.
steps[].stepCode để map về form tương ứng.
steps[].payload để fill dữ liệu vào field.
steps[].status để hiển thị trạng thái từng bước.
steps[].requiresReview để biết step cần kiểm tra lại sau khi bước trước bị sửa.
```

## 9. Lấy danh sách hồ sơ nháp

```http
GET /api/v1/loan-application-drafts?status=DRAFT
GET /api/v1/loan-application-drafts?status=COMPLETED
GET /api/v1/loan-application-drafts?status=CONVERTED
```

Nếu bỏ `status`, API trả tất cả draft.

## 10. Submit thành hồ sơ vay thật

```http
POST /api/v1/loan-application-drafts/{draftCode}/submit
```

Điều kiện:

```text
Tất cả step phải COMPLETED.
Không step nào còn requiresReview = true.
Draft không được CANCELLED, EXPIRED hoặc CONVERTED.
```

Kỳ vọng:

```text
- Tạo loan_application thật.
- Gắn customer_id từ loan_application_draft.
- Set requested_amount, loan_purpose, loan_term, loan_product nếu đọc được từ payload.
- draft.status = CONVERTED.
- draft.converted_loan_application_id trỏ tới loan_application vừa tạo.
```

## 11. Hủy hồ sơ nháp

```http
POST /api/v1/loan-application-drafts/{draftCode}/cancel
```

Request:

```json
{
  "note": "Khach khong tiep tuc nhu cau vay."
}
```

Kỳ vọng:

```text
- draft.status = CANCELLED.
- Không cho lưu/complete/submit draft này nữa.
```

## 12. Case cần test thêm

### Sửa bước trước sau khi đã nhập bước sau

Ví dụ đã hoàn thành `ASSET_DETAIL`, sau đó sửa lại `PRELIMINARY_INFO`:

```http
PUT /api/v1/loan-application-drafts/{draftCode}/steps/PRELIMINARY_INFO
```

Kỳ vọng:

```text
Các step phía sau có dữ liệu sẽ được đánh dấu requiresReview = true.
Frontend cần hiển thị cảnh báo và yêu cầu user kiểm tra lại các bước đó.
Submit sẽ bị chặn nếu còn requiresReview = true.
```
