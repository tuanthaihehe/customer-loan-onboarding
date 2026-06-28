# API Request/Response Samples - Demo Flow 1

Tài liệu này trình bày request/response mẫu theo đúng thứ tự chạy demo Flow 1.

Base URL:

```text
http://localhost:8080
```

Swagger:

```text
http://localhost:8080/swagger-ui/index.html
```

## 0. Health check

### Request

```http
GET /api/v1/health
```

### Response

```json
{
  "success": true,
  "message": "Success",
  "data": "Loan Onboarding API is running",
  "errorCode": null,
  "timestamp": "2026-06-27T10:00:00"
}
```

---

## 1. Customer Lookup

### Request

```http
POST /api/v1/customers/lookup
Content-Type: application/json
```

```json
{
  "fullName": "Nguyen Van A",
  "dateOfBirth": "2003-03-09",
  "identifierType": "CCCD",
  "identifierNumber": "123544353234",
  "phoneNumber": "0918254354"
}
```

### Response

```json
{
  "success": true,
  "message": "Customer lookup completed",
  "data": {
    "found": true,
    "customerCode": "CUS-000001",
    "customerState": "CUS_PRE",
    "legalEligibilityStatus": "ELIGIBLE",
    "onboardingPermission": "ALLOWED",
    "matchedCustomer": {
      "fullName": "Nguyen Van A",
      "dateOfBirth": "2003-03-09",
      "identifierNumber": "123544353234",
      "phoneNumber": "0918254354"
    },
    "reasonCode": null
  },
  "errorCode": null,
  "timestamp": "2026-06-27T10:00:00"
}
```

---

## 2. Create Loan Application Draft

### Request

```http
POST /api/v1/loan-applications
Content-Type: application/json
```

```json
{
  "customerCode": "CUS-000001",
  "applicationChannel": "PGD",
  "branchCode": "BR-001",
  "staffCode": "STAFF-001"
}
```

### Response

```json
{
  "success": true,
  "message": "Loan application draft created",
  "data": {
    "applicationCode": "APP-2026-000001",
    "applicationState": "APP_DRAFT",
    "customerCode": "CUS-000001",
    "createdDate": "2026-06-27T10:01:00",
    "lastSavedAt": "2026-06-27T10:01:00"
  },
  "errorCode": null,
  "timestamp": "2026-06-27T10:01:00"
}
```

---

## 3. Save Loan Application Draft

### Request

```http
PATCH /api/v1/loan-applications/APP-2026-000001/draft
Content-Type: application/json
```

```json
{
  "applicantSnapshot": {
    "fullName": "Nguyen Van A",
    "dateOfBirth": "2003-03-09",
    "gender": "MALE",
    "identifierNumber": "123544353234",
    "phoneNumber": "0918254354",
    "occupation": "BUSINESS_OWNER",
    "monthlyIncome": 15000000
  },
  "loanRequest": {
    "loanPurpose": "BUSINESS",
    "requestedAmount": 20000000,
    "requestedTenure": 12
  }
}
```

### Response

```json
{
  "success": true,
  "message": "Loan application draft saved",
  "data": {
    "applicationCode": "APP-2026-000001",
    "applicationState": "APP_DRAFT",
    "customerCode": "CUS-000001",
    "createdDate": "2026-06-27T09:50:00",
    "lastSavedAt": "2026-06-27T10:02:00"
  },
  "errorCode": null,
  "timestamp": "2026-06-27T10:02:00"
}
```

---

## 4. Asset Lookup

### Request

```http
POST /api/v1/assets/lookup
Content-Type: application/json
```

```json
{
  "assetType": "MOTORBIKE",
  "licensePlate": "29A12345",
  "chassisNumber": "CHASSIS001",
  "engineNumber": "ENGINE001"
}
```

### Response

```json
{
  "success": true,
  "message": "Asset lookup completed",
  "data": {
    "found": false,
    "assetCode": null,
    "assetState": null,
    "eligibleForPledge": true,
    "reasonCode": null
  },
  "errorCode": null,
  "timestamp": "2026-06-27T10:03:00"
}
```

---

## 5. Save Asset Snapshot

### Request

```http
PATCH /api/v1/loan-applications/APP-2026-000001/asset-snapshot
Content-Type: application/json
```

```json
{
  "assetType": "MOTORBIKE",
  "licensePlate": "29A12345",
  "brand": "HONDA",
  "model": "SH",
  "vehicleVariant": "ABS",
  "manufactureYear": 2021,
  "vehicleColor": "BLACK"
}
```

### Response

```json
{
  "success": true,
  "message": "Asset snapshot saved",
  "data": {
    "applicationCode": "APP-2026-000001",
    "assetType": "MOTORBIKE",
    "licensePlate": "29A12345",
    "brand": "HONDA",
    "model": "SH",
    "vehicleVariant": "ABS",
    "manufactureYear": 2021,
    "vehicleColor": "BLACK"
  },
  "errorCode": null,
  "timestamp": "2026-06-27T10:04:00"
}
```

---

## 6. Preview Asset Valuation

### Request

```http
POST /api/v1/loan-applications/APP-2026-000001/asset-valuations/preview
Content-Type: application/json
```

```json
{
  "assetSnapshot": {
    "assetType": "MOTORBIKE",
    "licensePlate": "29A12345",
    "brand": "HONDA",
    "model": "SH",
    "vehicleVariant": "ABS",
    "manufactureYear": 2021,
    "vehicleColor": "BLACK"
  },
  "deductionItems": [
    {
      "type": "SCRATCH",
      "rate": 3
    },
    {
      "type": "REPAINTED",
      "rate": 4
    }
  ]
}
```

### Response

```json
{
  "success": true,
  "message": "Asset valuation preview calculated",
  "data": {
    "applicationCode": "APP-2026-000001",
    "marketValue": 30000000,
    "totalDeductionRate": 7,
    "totalDeductionAmount": 2100000,
    "finalValue": 27900000,
    "ltvRatio": 70,
    "loanableValue": 19530000,
    "valuationState": "VAL_RECORDED",
    "appliedDeductionTypes": ["SCRATCH", "REPAINTED"]
  },
  "errorCode": null,
  "timestamp": "2026-06-27T10:05:00"
}
```

---

## 7. Save Asset Valuation Preview

### Request

```http
PATCH /api/v1/loan-applications/APP-2026-000001/valuation-preview
Content-Type: application/json
```

Sử dụng cùng body với bước 6.

### Response khác chính

```json
{
  "success": true,
  "message": "Asset valuation preview saved",
  "data": {
    "applicationCode": "APP-2026-000001",
    "valuationState": "VAL_ACTIVE",
    "loanableValue": 19530000
  },
  "errorCode": null,
  "timestamp": "2026-06-27T10:06:00"
}
```

---

## 8. Run Eligibility Check

### Request

```http
POST /api/v1/loan-applications/APP-2026-000001/eligibility-checks
```

### Response

```json
{
  "success": true,
  "message": "Eligibility check completed",
  "data": {
    "eligibilityCheckCode": "ELG-000001",
    "applicationCode": "APP-2026-000001",
    "checklistState": "CHECKLIST_PASSED",
    "eligibilityResult": "PASSED",
    "totalItemCount": 8,
    "completedItemCount": 8,
    "missingItemCount": 0,
    "failedItemCount": 0,
    "checkedDate": "2026-06-27T10:07:00"
  },
  "errorCode": null,
  "timestamp": "2026-06-27T10:07:00"
}
```

---

## 9. Submit Loan Application For Approval

### Request

```http
POST /api/v1/loan-applications/APP-2026-000001/submit-for-approval
```

### Response

```json
{
  "success": true,
  "message": "Loan application submitted for approval",
  "data": {
    "applicationCode": "APP-2026-000001",
    "applicationState": "APP_SUBMITTED",
    "approvalCaseCode": "APR-2026-000001",
    "eventName": "LoanApplicationSubmittedForApproval",
    "submittedAt": "2026-06-27T10:08:00",
    "message": "Demo flow completed: loan application is ready for approval review."
  },
  "errorCode": null,
  "timestamp": "2026-06-27T10:08:00"
}
```

## 10. Validation/error sample

Ví dụ thiếu `fullName` khi lookup khách hàng:

```json
{
  "success": false,
  "message": "fullName: Họ và tên là bắt buộc",
  "data": null,
  "errorCode": "ERR_VALIDATION",
  "timestamp": "2026-06-27T10:09:00"
}
```

## 11. Ghi chú

Các response timestamp có thể khác khi chạy thật. Dữ liệu mock có chủ đích cố định để demo dễ hiểu.
