# API Request/Response Samples

Base URL:

```text
http://localhost:8080
```

## 0. Health

```http
GET /api/v1/health
```

```json
{
  "success": true,
  "message": "Success",
  "data": "Loan Onboarding API is running",
  "errorCode": null,
  "timestamp": "2026-06-30T10:00:00"
}
```

## 1. Customer Lookup

Seed có sẵn ví dụ `CUS-2026-000001`.

```http
POST /api/v1/customers/lookup
Content-Type: application/json
```

```json
{
  "fullName": "Nguyễn Văn An",
  "dateOfBirth": "1995-01-15",
  "identifierType": "CCCD",
  "identifierNumber": "001201000001",
  "phoneNumber": "0901000001"
}
```

```json
{
  "success": true,
  "message": "Customer lookup completed",
  "data": {
    "found": true,
    "customerCode": "CUS-2026-000001",
    "customerState": "ACTIVE",
    "legalEligibilityStatus": "ELIGIBLE",
    "onboardingPermission": "ALLOW_CREATE_APPLICATION",
    "matchedCustomer": {
      "fullName": "Nguyễn Văn An",
      "dateOfBirth": "1995-01-15",
      "identifierNumber": "001201000001",
      "phoneNumber": "0901000001"
    },
    "reasonCode": null
  },
  "errorCode": null,
  "timestamp": "2026-06-30T10:01:00"
}
```

## 2. Create Loan Application Draft

```http
POST /api/v1/loan-applications
Content-Type: application/json
```

```json
{
  "customerCode": "CUS-2026-000001",
  "applicationChannel": "PGD",
  "branchCode": "BR-001",
  "staffCode": "staff_001"
}
```

```json
{
  "success": true,
  "message": "Loan application draft created",
  "data": {
    "applicationCode": "APP-2026-000006",
    "applicationState": "APP_DRAFT",
    "customerCode": "CUS-2026-000001",
    "createdDate": "2026-06-30T10:02:00",
    "lastSavedAt": "2026-06-30T10:02:00"
  },
  "errorCode": null,
  "timestamp": "2026-06-30T10:02:00"
}
```

## 3. Save Loan Application Draft

`loanPurpose` phải nằm trong enum-like values của migration `V4__add_loan_terms_and_purpose_enum.sql`.

```http
PATCH /api/v1/loan-applications/APP-2026-000006/draft
Content-Type: application/json
```

```json
{
  "applicantSnapshot": {
    "fullName": "Nguyễn Văn An",
    "dateOfBirth": "1995-01-15",
    "gender": "MALE",
    "identifierNumber": "001201000001",
    "phoneNumber": "0901000001",
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

```json
{
  "success": true,
  "message": "Loan application draft saved",
  "data": {
    "applicationCode": "APP-2026-000006",
    "applicationState": "APP_DRAFT",
    "customerCode": "CUS-2026-000001",
    "createdDate": "2026-06-30T10:02:00",
    "lastSavedAt": "2026-06-30T10:03:00"
  },
  "errorCode": null,
  "timestamp": "2026-06-30T10:03:00"
}
```

## 4. Get Detail

```http
GET /api/v1/loan-applications/APP-2026-000006
```

Response đọc từ `loan_application`, `customer`, `loan_application_state` và history.

## 5. Complete Preliminary Step

```http
POST /api/v1/loan-applications/APP-2026-000006/steps/preliminary/complete
```

```json
{
  "success": true,
  "message": "Preliminary step completed",
  "data": {
    "applicationCode": "APP-2026-000006",
    "step": "PRELIMINARY",
    "completed": true,
    "nextStep": "SUBMIT_FOR_APPROVAL",
    "validationErrors": []
  },
  "errorCode": null,
  "timestamp": "2026-06-30T10:04:00"
}
```

## 6. Submit For Approval

```http
POST /api/v1/loan-applications/APP-2026-000006/submit-for-approval
```

```json
{
  "success": true,
  "message": "Loan application submitted for approval",
  "data": {
    "applicationCode": "APP-2026-000006",
    "applicationState": "APP_SUBMITTED",
    "approvalCaseCode": null,
    "eventName": "LoanApplicationSubmitted",
    "submittedAt": "2026-06-30T10:05:00",
    "message": "Hồ sơ đã được gửi vào luồng xử lý theo lifecycle trong database"
  },
  "errorCode": null,
  "timestamp": "2026-06-30T10:05:00"
}
```

## 7. Asset/Valuation Chưa Có Schema

Ví dụ:

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

```json
{
  "success": false,
  "message": "Database hiện tại chưa có bảng tài sản; migration đang loại asset khỏi phạm vi phiên bản này",
  "data": null,
  "errorCode": "ERR_SCHEMA_NOT_READY",
  "timestamp": "2026-06-30T10:06:00"
}
```

Các endpoint valuation trả cùng nhóm lỗi vì chưa có bảng định giá tài sản.

## 8. Validation Error

```json
{
  "success": false,
  "message": "fullName: Họ và tên là bắt buộc",
  "data": null,
  "errorCode": "ERR_400",
  "timestamp": "2026-06-30T10:07:00"
}
```
