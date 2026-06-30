# Customer & Loan Onboarding

Repository này là backend cho đề tài **Customer & Loan Onboarding**. Backend hiện chạy theo hướng **database-first** với PostgreSQL/Flyway để thao tác trên dữ liệu thật.

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
| Service dùng database thật | Đã có qua JPA Repository |
| Flyway migration | Đã có |
| Rule skeleton | Đã có ở mức tối giản |
| Tài liệu `.md` cho AI/DEV | Đã có |

## 3. Những gì chưa thuộc phạm vi hiện tại

Các phần sau chưa làm sâu vì demo hiện tại chỉ dừng ở gửi hồ sơ đi phê duyệt:

| Không làm sâu hiện tại | Lý do |
|---|---|
| Unit test chi tiết cho rule | Rule nghiệp vụ chưa được team chốt hoàn thiện |
| Controller test chi tiết | API/rule còn có thể thay đổi theo BA/FE/mentor |
| Rule registry/rule engine | Quá nặng cho demo Flow 1 |
| Entity/Repository JPA | Đã có cho các bảng hiện tại |
| Bảng asset/valuation/eligibility chi tiết | Chờ ERD/workflow chính thức |
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

Backend mặc định chạy bằng profile `db`, cần PostgreSQL đang chạy:

```powershell
docker compose up -d postgres
```

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
- Mọi dữ liệu nghiệp vụ mới phải đi qua database thật.
- Không làm rule engine phức tạp khi rule chưa ổn định.
- Không viết business logic trong controller.
- Không thêm lại data provider giả lập hoặc service giả lập.
