# Customer & Loan Onboarding - Demo Flow 1

Repository này là baseline backend cho đề tài **Customer & Loan Onboarding**. Mục tiêu hiện tại không phải xây dựng hệ thống production đầy đủ, mà là chuẩn bị một backend **API-first, mock-first** để demo được **luồng 1: tạo hồ sơ vay và gửi đi phê duyệt**.

## 1. Phạm vi hiện tại

Backend cần chạy được luồng sau:

```text
Customer Lookup
→ Create Loan Application Draft
→ Save Loan Application Draft
→ Asset Lookup
→ Save Asset Snapshot
→ Preview / Save Asset Valuation
→ Run Eligibility Check
→ Submit Loan Application For Approval
```

Kết quả mong muốn của demo:

```text
Một Loan Application được tạo, có thông tin khách hàng, thông tin khoản vay, thông tin tài sản, kết quả định giá, kết quả eligibility và được gửi sang bước phê duyệt.
```

Endpoint cuối của demo:

```text
POST /api/v1/loan-applications/{applicationCode}/submit-for-approval
```

## 2. Những gì đã chuẩn bị

| Nhóm | Trạng thái |
|---|---|
| Spring Boot backend | Đã có |
| Health check API | Đã có |
| Swagger/OpenAPI | Đã có |
| API skeleton cho Flow 1 | Đã có |
| DTO request/response | Đã có |
| `ApiResponse<T>` chuẩn | Đã có |
| Exception handling | Đã có |
| Mock service | Đã có |
| Mock data provider | Đã tách riêng |
| Demo guard/rule skeleton | Đã có ở mức tối giản |
| Tài liệu `.md` cho AI/DEV | Đã có |

## 3. Những gì chưa thuộc phạm vi hiện tại

Các phần sau chưa làm sâu vì demo hiện tại chỉ dừng ở gửi hồ sơ đi phê duyệt:

| Không làm sâu hiện tại | Lý do |
|---|---|
| Unit test chi tiết cho rule | Rule nghiệp vụ chưa được team chốt hoàn thiện |
| Controller test chi tiết | API/rule còn có thể thay đổi theo BA/FE/mentor |
| Rule registry/rule engine | Quá nặng cho demo Flow 1 |
| Entity/Repository thật | Chờ ERD/DB schema chính thức |
| Flyway/Liquibase migration | Chờ ERD |
| Approval decision đầy đủ | Ngoài phạm vi Flow 1 |
| Contract signing | Ngoài phạm vi Flow 1 |
| Disbursement/Core integration | Ngoài phạm vi Flow 1 |
| Security/JWT | Chưa cần cho demo local |

## 4. Cách chạy backend

```powershell
cd backend/loan-onboarding
.\mvnw clean test
.\mvnw spring-boot:run
```

Backend mặc định chạy bằng `mock` profile, không cần database.

```text
http://localhost:8080/api/v1/health
http://localhost:8080/swagger-ui/index.html
http://localhost:8080/swagger-ui.html
```

## 5. Thứ tự đọc tài liệu

| Thứ tự | File | Mục đích |
|---:|---|---|
| 1 | `docs/00_READ_ME_FIRST.md` | Tổng quan phạm vi và cách đọc tài liệu |
| 2 | `docs/01_DEMO_FLOW_1.md` | Luồng demo chính cần chạy |
| 3 | `docs/api/01_API_REQUEST_RESPONSE_SAMPLES.md` | Request/response mẫu theo flow |
| 4 | `docs/api-test/01_API_SWAGGER_TEST_REPORT.md` | Checklist test Swagger |
| 5 | `docs/backend/01_RULE_SKELETON.md` | Cách hiểu rule skeleton hiện tại |
| 6 | `docs/ai-context/01_AI_CONTEXT.md` | Context cho AI/DEV khi tiếp tục code |
| 7 | `docs/dev-handoff/00_READ_ME_FIRST.md` | Handoff cho dev mới |

## 6. Nguyên tắc phát triển tiếp

- Giữ scope là **Demo Flow 1**.
- Ưu tiên API chạy được và tài liệu rõ ràng.
- Không thiết kế DB thật trước khi ERD chốt.
- Không làm rule engine phức tạp khi rule chưa ổn định.
- Không viết business logic trong controller.
- Mock data phải nằm trong package `mock`, không hard-code rải rác trong service.
