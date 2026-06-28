# Demo Flow 1 - Tạo hồ sơ vay và gửi đi phê duyệt

## 1. Mục tiêu Flow 1

Flow 1 chứng minh backend có thể hỗ trợ nhân viên PGD tạo một hồ sơ vay nháp, bổ sung thông tin cần thiết, kiểm tra điều kiện cơ bản và gửi hồ sơ sang bước phê duyệt.

Kết quả cuối cùng:

```text
LoanApplication.applicationState = APP_SUBMITTED
ApprovalCase mock được sinh ra với approvalCaseCode
Event mock = LoanApplicationSubmittedForApproval
```

## 2. Luồng nghiệp vụ demo

```text
Khách hàng phát sinh nhu cầu vay
→ Nhân viên PGD tra cứu khách hàng
→ Hệ thống trả thông tin khách hàng mock
→ Nhân viên tạo hồ sơ vay nháp
→ Nhân viên lưu thông tin khách hàng và nhu cầu vay
→ Nhân viên tra cứu tài sản
→ Nhân viên lưu tài sản vào hồ sơ
→ Hệ thống tính định giá thử
→ Hệ thống chạy eligibility check
→ Nhân viên gửi hồ sơ sang bước phê duyệt
```

## 3. API sequence

| Step | API | Mục đích | Expected result |
|---:|---|---|---|
| 0 | `GET /api/v1/health` | Kiểm tra backend chạy | `Loan Onboarding API is running` |
| 1 | `POST /api/v1/customers/lookup` | Tra cứu khách hàng | `customerCode = CUS-000001` |
| 2 | `POST /api/v1/loan-applications` | Tạo hồ sơ nháp | `applicationCode = APP-2026-000001` |
| 3 | `PATCH /api/v1/loan-applications/{applicationCode}/draft` | Lưu thông tin hồ sơ | State vẫn là `APP_DRAFT` |
| 4 | `POST /api/v1/assets/lookup` | Tra cứu tài sản | `eligibleForPledge = true` |
| 5 | `PATCH /api/v1/loan-applications/{applicationCode}/asset-snapshot` | Gắn tài sản vào hồ sơ | Trả asset snapshot |
| 6 | `POST /api/v1/loan-applications/{applicationCode}/asset-valuations/preview` | Tính định giá thử | Trả `loanableValue` |
| 7 | `PATCH /api/v1/loan-applications/{applicationCode}/valuation-preview` | Lưu định giá thử | `valuationState = VAL_ACTIVE` |
| 8 | `POST /api/v1/loan-applications/{applicationCode}/eligibility-checks` | Kiểm tra điều kiện | `eligibilityResult = PASSED` |
| 9 | `POST /api/v1/loan-applications/{applicationCode}/submit-for-approval` | Gửi phê duyệt | `APP_SUBMITTED` và `approvalCaseCode` |

## 4. Dữ liệu demo cố định

| Field | Value |
|---|---|
| `customerCode` | `CUS-000001` |
| `applicationCode` | `APP-2026-000001` |
| `approvalCaseCode` | `APR-2026-000001` |
| `assetType` | `MOTORBIKE` |
| `licensePlate` | `29A12345` |
| `marketValue` | `30000000` |
| `ltvRatio` | `70` |
| `eligibilityResult` | `PASSED` |

## 5. Không thuộc Flow 1

Flow 1 không xử lý các bước sau:

- phê duyệt thật sự `approve/reject`;
- sinh hợp đồng;
- ký hợp đồng;
- tạo loan account thật;
- giải ngân;
- đồng bộ Core Banking;
- rule engine production;
- database persistence.

Các phần này có thể bổ sung sau khi mentor/team mở rộng scope.
