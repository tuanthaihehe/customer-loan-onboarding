# Swagger Manual Test Checklist - Demo Flow 1

Tài liệu này là checklist test thủ công trên Swagger. Hiện tại chưa ưu tiên controller test tự động vì API/rule có thể còn thay đổi theo BA/FE/mentor.

## 1. Mục tiêu test

- Xác nhận backend chạy được.
- Xác nhận Swagger hiển thị API.
- Xác nhận Flow 1 chạy được từ đầu đến cuối.
- Xác nhận response dùng chung format `ApiResponse<T>`.
- Xác nhận một số validation cơ bản hoạt động.

## 2. URL

```text
Health:  http://localhost:8080/api/v1/health
Swagger: http://localhost:8080/swagger-ui/index.html
```

## 3. Checklist chính

| Step | API | Expected result | Status |
|---:|---|---|---|
| 0 | `GET /api/v1/health` | Backend trả `Loan Onboarding API is running` | Manual |
| 1 | `POST /api/v1/customers/lookup` | Trả `customerCode = CUS-000001` | Manual |
| 2 | `POST /api/v1/loan-applications` | Trả `applicationCode = APP-2026-000001` | Manual |
| 3 | `PATCH /api/v1/loan-applications/{applicationCode}/draft` | Trả state `APP_DRAFT` | Manual |
| 4 | `POST /api/v1/assets/lookup` | Trả `eligibleForPledge = true` | Manual |
| 5 | `PATCH /api/v1/loan-applications/{applicationCode}/asset-snapshot` | Trả asset snapshot | Manual |
| 6 | `POST /api/v1/loan-applications/{applicationCode}/asset-valuations/preview` | Trả `loanableValue` | Manual |
| 7 | `PATCH /api/v1/loan-applications/{applicationCode}/valuation-preview` | Trả `valuationState = VAL_ACTIVE` | Manual |
| 8 | `POST /api/v1/loan-applications/{applicationCode}/eligibility-checks` | Trả `eligibilityResult = PASSED` | Manual |
| 9 | `POST /api/v1/loan-applications/{applicationCode}/submit-for-approval` | Trả `APP_SUBMITTED` và `approvalCaseCode` | Manual |

## 4. Checklist validation tối thiểu

| Case | API | Input lỗi | Expected |
|---:|---|---|---|
| V1 | Customer lookup | Thiếu `fullName` | `success = false`, `errorCode = ERR_VALIDATION` |
| V2 | Create draft | Thiếu `customerCode` | `success = false`, `errorCode = ERR_VALIDATION` |
| V3 | Save draft | `requestedAmount <= 0` | `success = false`, validation/business error |
| V4 | Asset lookup | Thiếu `licensePlate` | `success = false`, `errorCode = ERR_VALIDATION` |
| V5 | Valuation preview | `rate` âm | `success = false`, `errorCode = ERR_VALIDATION` |

## 5. Tiêu chí pass cho demo

Demo được xem là đạt khi:

```text
- Swagger mở được.
- Tất cả API chính của Flow 1 trả success.
- Bước cuối trả APP_SUBMITTED.
- Response có success/message/data/errorCode/timestamp.
- Có ít nhất một validation fail đúng format.
```

## 6. Không yêu cầu ở giai đoạn này

Chưa cần:

- unit test chi tiết cho từng rule;
- controller test chi tiết;
- integration test với database;
- approval/contract/disbursement test.

Lý do: phạm vi hiện tại là demo Flow 1 bằng mock data, rule nghiệp vụ và ERD chưa chốt hoàn thiện.
