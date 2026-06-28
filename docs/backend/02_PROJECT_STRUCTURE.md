# PROJECT_STRUCTURE.md

# Cấu trúc package và mối liên hệ trong project `loan-onboarding`

## 1. Mục đích tài liệu

Tài liệu này mô tả cấu trúc source code backend của project **Customer & Loan Onboarding**.

Mục tiêu là giúp DEV/AI/FE/BA khi đọc project có thể hiểu nhanh:

- Package nào dùng để làm gì.
- File nào liên hệ với file nào.
- Request từ Swagger/FE đi qua các tầng nào.
- Dữ liệu mock đang được đặt ở đâu.
- Rule demo được đặt ở đâu.
- Những package nào hiện tại chỉ là placeholder, chưa triển khai thật.
- Cách thêm API mới mà không làm rối cấu trúc project.

Project hiện tại đang phục vụ **DEMO Flow 1**:

```text
Customer Lookup
→ Create Loan Application Draft
→ Save Draft
→ Asset Lookup
→ Save Asset Snapshot
→ Valuation Preview
→ Eligibility Check
→ Submit For Approval
```

Phạm vi hiện tại là **API-first + Mock-first**, chưa phải production backend hoàn chỉnh.

---

## 2. Cấu trúc tổng quan

Package gốc:

```text
src/main/java/com/f88/loanonboarding
```

Cấu trúc chính:

```text
com.f88.loanonboarding
├── audit
├── common
├── config
├── controller
├── dto
├── entity
├── enums
├── exception
├── mapper
├── mock
├── repository
├── rule
├── service
├── validation
└── LoanOnboardingApplication.java
```

Tài nguyên cấu hình:

```text
src/main/resources
├── application.properties
├── application-mock.properties
└── application-db.properties
```

Test:

```text
src/test
```

---

## 3. Sơ đồ luồng xử lý request

Luồng xử lý chuẩn của backend hiện tại:

```text
Swagger / FE
    ↓
Controller
    ↓
DTO Request
    ↓
Service Interface
    ↓
Mock Service Implementation
    ↓
Rule / Guard Demo
    ↓
Mock Data Provider
    ↓
DTO Response
    ↓
ApiResponse<T>
    ↓
Swagger / FE
```

Giải thích ngắn:

| Tầng | Vai trò |
|---|---|
| Swagger / FE | Nơi gọi API |
| Controller | Nhận request, định nghĩa endpoint |
| DTO Request | Định nghĩa dữ liệu đầu vào |
| Service Interface | Định nghĩa nghiệp vụ ở mức abstract |
| Mock Service Implementation | Xử lý flow demo bằng dữ liệu mock |
| Rule / Guard Demo | Kiểm tra điều kiện tối thiểu của flow |
| Mock Data Provider | Cung cấp dữ liệu giả cho demo |
| DTO Response | Định nghĩa dữ liệu đầu ra |
| ApiResponse<T> | Chuẩn hóa format response trả về |

---

## 4. Vai trò từng package

## 4.1. `controller`

### Vai trò

Package `controller` chứa các REST API endpoint.

Controller là lớp đầu tiên nhận request từ:

- Swagger UI;
- frontend;
- công cụ test API;
- client khác.

Controller không nên chứa logic nghiệp vụ phức tạp. Controller chỉ nên:

1. Nhận request.
2. Gọi service tương ứng.
3. Bọc kết quả bằng `ApiResponse<T>`.
4. Trả response cho client.

### Ví dụ trách nhiệm

```text
CustomerController
- Nhận request tra cứu khách hàng.
- Gọi CustomerService.
- Trả CustomerLookupResponse.

LoanApplicationController
- Nhận request tạo hồ sơ vay.
- Nhận request lưu nháp.
- Nhận request gửi hồ sơ đi phê duyệt.
- Gọi LoanApplicationService.

AssetController
- Nhận request tra cứu tài sản.
- Gọi AssetService.

AssetValuationController
- Nhận request định giá thử tài sản.
- Gọi AssetValuationService.

EligibilityController
- Nhận request kiểm tra điều kiện vay.
- Gọi EligibilityService.

ReferenceDataController
- Trả dữ liệu danh mục/dropdown phục vụ UI.
```

### Nguyên tắc

Controller không nên:

- tự tạo dữ liệu mock;
- tự viết rule;
- tự xử lý logic nghiệp vụ dài;
- tự thao tác DB;
- tự mapping entity phức tạp.

Controller nên gọi service.

---

## 4.2. `service`

### Vai trò

Package `service` chứa lớp xử lý nghiệp vụ ở mức application/service layer.

Trong project hiện tại, service có 2 loại chính:

```text
Service Interface
Service Mock Implementation
```

Ví dụ:

```text
CustomerService
CustomerServiceMockImpl
```

### Mối liên hệ

```text
Controller → Service Interface → Mock Implementation
```

Controller chỉ biết interface, không nên phụ thuộc trực tiếp vào implementation cụ thể.

### Vì sao cần interface?

Dù hiện tại đang dùng mock, sau này khi có DB thật có thể thêm implementation khác:

```text
CustomerService
├── CustomerServiceMockImpl
└── CustomerServiceDbImpl
```

Khi đó controller gần như không cần thay đổi.

### Vai trò trong DEMO Flow 1

Service hiện tại dùng để mô phỏng flow:

```text
Tra cứu khách hàng
Tạo hồ sơ vay nháp
Lưu thông tin tài sản
Định giá thử
Kiểm tra điều kiện vay
Gửi hồ sơ đi phê duyệt
```

---

## 4.3. `mock`

### Vai trò

Package `mock` chứa các class cung cấp dữ liệu giả cho demo.

Đây là package quan trọng vì project đang đi theo hướng **Mock-first**.

### Vì sao cần tách package `mock`?

Nếu để dữ liệu mock trực tiếp trong service, service sẽ bị lẫn nhiều trách nhiệm:

```text
Nhận request
Xử lý flow
Tự tạo dữ liệu giả
Tự quyết định scenario demo
```

Sau khi tách `mock`, trách nhiệm rõ hơn:

```text
Service = xử lý flow demo
Mock Data Provider = cung cấp dữ liệu giả
```

### Ví dụ file

```text
DemoCustomerMockDataProvider
DemoLoanApplicationMockDataProvider
DemoAssetMockDataProvider
DemoValuationMockDataProvider
DemoEligibilityMockDataProvider
DemoReferenceDataMockDataProvider
```

### Mối liên hệ

```text
Mock Service Implementation → Mock Data Provider
```

Ví dụ:

```text
CustomerServiceMockImpl
    ↓
DemoCustomerMockDataProvider
```

### Nguyên tắc

Package `mock` chỉ phục vụ demo, không phải nguồn dữ liệu production.

Sau này khi có ERD/DB:

```text
Mock Data Provider có thể được giữ lại để demo/test
DB Repository sẽ xử lý dữ liệu thật
```

---

## 4.4. `dto`

### Vai trò

Package `dto` chứa các class định nghĩa dữ liệu vào/ra của API.

DTO là contract giữa backend với:

- Swagger;
- frontend;
- BA/DEV khi review API;
- tài liệu request/response sample.

DTO không phải entity database.

### Phân loại

Thường chia thành:

```text
dto/request
dto/response
```

Hoặc theo domain:

```text
dto/request/customer
dto/response/customer
dto/request/loan
dto/response/loan
```

### Mối liên hệ

```text
Controller nhận Request DTO
Service xử lý Request DTO
Service trả Response DTO
Controller bọc Response DTO bằng ApiResponse<T>
```

### Ví dụ

```text
CustomerLookupRequest
CustomerLookupResponse

CreateLoanApplicationRequest
CreateLoanApplicationResponse

AssetLookupRequest
AssetLookupResponse

ValuationPreviewRequest
ValuationPreviewResponse

EligibilityCheckRequest
EligibilityCheckResponse

SubmitForApprovalResponse
```

### Nguyên tắc

DTO nên:

- rõ field;
- rõ ý nghĩa;
- phục vụ API contract;
- không chứa annotation mapping DB;
- không dùng thay thế entity.

DTO không nên:

- chứa logic nghiệp vụ phức tạp;
- chứa quan hệ DB;
- chứa rule;
- phụ thuộc vào JPA entity.

---

## 4.5. `common`

### Vai trò

Package `common` chứa các thành phần dùng chung cho toàn project.

Thành phần quan trọng nhất hiện tại là:

```text
ApiResponse<T>
```

### Vai trò của `ApiResponse<T>`

Dùng để chuẩn hóa response của toàn bộ API.

Format response thống nhất:

```json
{
  "success": true,
  "message": "Success",
  "data": {},
  "errorCode": null,
  "timestamp": "2026-xx-xxTxx:xx:xx"
}
```

### Mối liên hệ

```text
Controller → ApiResponse<T> → Client
```

Bất kỳ controller nào cũng nên trả về response theo chuẩn này.

### Lợi ích

- FE dễ xử lý response.
- API thống nhất.
- Swagger dễ đọc.
- Khi lỗi xảy ra, response không bị mỗi endpoint một kiểu.

---

## 4.6. `exception`

### Vai trò

Package `exception` xử lý lỗi và exception chung.

Các thành phần thường có:

```text
BusinessException
GlobalExceptionHandler
ErrorCode
```

### Mối liên hệ

```text
Service / Rule phát sinh lỗi
    ↓
BusinessException
    ↓
GlobalExceptionHandler
    ↓
ApiResponse<T> với success = false
```

### Vai trò từng thành phần

| Thành phần | Vai trò |
|---|---|
| `BusinessException` | Đại diện cho lỗi nghiệp vụ |
| `GlobalExceptionHandler` | Bắt lỗi tập trung, trả response chuẩn |
| `ErrorCode` | Chuẩn hóa mã lỗi |

### Nguyên tắc

Không nên để controller tự `try/catch` quá nhiều.

Nên để lỗi đi về `GlobalExceptionHandler` để trả response thống nhất.

---

## 4.7. `enums`

### Vai trò

Package `enums` chứa các giá trị cố định dùng trong hệ thống.

Ví dụ:

```text
ApplicationState
EligibilityResult
ValuationResult
AssetType
```

### Vì sao cần enum?

Nếu dùng string rời rạc, dễ sai chính tả:

```text
Draft
DRAFT
draft
APP_DRAFT
```

Enum giúp thống nhất trạng thái giữa code và tài liệu.

### Mối liên hệ

```text
DTO / Service / Rule → Enums
```

Ví dụ:

```text
LoanApplicationResponse.applicationState = APP_DRAFT
EligibilityCheckResponse.result = PASS
```

### Nguyên tắc

Enum nên dùng cho:

- trạng thái hồ sơ;
- kết quả kiểm tra;
- loại tài sản;
- trạng thái định giá;
- trạng thái xử lý demo.

---

## 4.8. `rule`

### Vai trò

Package `rule` chứa các rule/guard kiểm soát hành động trong flow.

Trong phạm vi hiện tại, rule chỉ là **demo guard rule**, chưa phải rule engine hoàn chỉnh.

### Điều đã chốt

Hiện tại chưa cần:

- unit test rule chi tiết;
- rule registry;
- rule engine phức tạp;
- rule config database.

Lý do:

- team chưa chốt rule nghiệp vụ cuối cùng;
- project chỉ demo Flow 1;
- mục tiêu là gửi được hồ sơ đi phê duyệt;
- không nên làm quá phức tạp.

### Rule hiện tại nên kiểm tra gì?

Ở mức demo, chỉ cần các điều kiện tối thiểu:

```text
Hồ sơ tồn tại
Hồ sơ đang ở trạng thái hợp lệ
Có customer snapshot
Có loan information
Có asset snapshot nếu sản phẩm yêu cầu
Đã có valuation preview
Đã chạy eligibility check
Eligibility result đạt điều kiện demo
```

### Mối liên hệ

```text
Service → Rule / Guard → Nếu hợp lệ thì xử lý tiếp
```

Ví dụ:

```text
LoanApplicationServiceMockImpl
    ↓
LoanApplicationDemoRuleValidator
    ↓
Submit For Approval
```

### Nguyên tắc

Rule không nên đặt trong controller.

Rule nên được gọi từ service.

---

## 4.9. `config`

### Vai trò

Package `config` chứa cấu hình cho Spring Boot và các thành phần hạ tầng.

Ví dụ:

```text
OpenApiConfig
LocalCorsConfig
```

### Vai trò cụ thể

| File | Vai trò |
|---|---|
| `OpenApiConfig` | Cấu hình thông tin Swagger/OpenAPI |
| `LocalCorsConfig` | Cho phép FE local gọi API backend local nếu cần |

### Mối liên hệ

```text
Spring Boot startup → đọc config → cấu hình toàn app
```

### Lưu ý về CORS

Nếu chỉ demo bằng Swagger, CORS chưa bắt buộc.

Nếu FE chạy ở:

```text
http://localhost:3000
http://localhost:5173
```

và gọi BE ở:

```text
http://localhost:8080
```

thì cần CORS config để tránh lỗi trình duyệt.

---

## 4.10. `entity`

### Vai trò hiện tại

Package `entity` hiện tại chỉ là placeholder.

Chưa nên triển khai entity thật nếu team chưa chốt ERD/DB.

### Vì sao chưa nên tạo entity thật?

Vì nếu entity được tạo trước khi ERD chính thức, có thể gây lệch:

```text
Backend entity khác ERD
Repository khác bảng thật
Service phải refactor lại
DTO bị nhầm với entity
```

### Khi nào dùng?

Chỉ dùng khi:

- ERD đã được chốt;
- bảng đã rõ;
- field đã rõ;
- quan hệ giữa bảng đã rõ;
- team thống nhất naming convention.

---

## 4.11. `repository`

### Vai trò hiện tại

Package `repository` hiện tại chỉ là placeholder.

Repository chỉ nên được thêm khi có DB schema chính thức.

### Sau này repository dùng để làm gì?

Repository là tầng truy cập dữ liệu:

```text
Service → Repository → Database
```

Ví dụ sau này:

```text
CustomerRepository
LoanApplicationRepository
AssetRepository
EligibilityCheckRepository
```

### Hiện tại vì sao chưa dùng?

Project đang chạy mock-first, chưa cần DB thật.

---

## 4.12. `mapper`

### Vai trò hiện tại

Package `mapper` hiện tại là placeholder.

Sau này mapper dùng để chuyển đổi giữa:

```text
Entity ↔ DTO
```

hoặc:

```text
Entity → Response DTO
Request DTO → Entity
```

### Vì sao hiện tại chưa cần?

Vì chưa có entity thật và chưa có DB thật.

Khi có ERD, mapper mới có giá trị rõ ràng.

---

## 4.13. `validation`

### Vai trò hiện tại

Package `validation` hiện tại là placeholder.

Sau này có thể dùng để đặt:

- custom annotation validation;
- validator class;
- input validation dùng chung.

Ví dụ tương lai:

```text
ValidIdentityNumber
ValidPhoneNumber
LoanAmountValidator
AssetInfoValidator
```

### Lưu ý

Validation khác rule.

| Loại | Mục đích |
|---|---|
| Validation | Kiểm tra dữ liệu đầu vào đúng format/bắt buộc |
| Rule | Kiểm tra điều kiện nghiệp vụ/action có được phép chạy không |

Ví dụ:

```text
Validation:
- Số điện thoại không được rỗng
- Số tiền vay phải là số dương

Rule:
- Hồ sơ phải có trạng thái DRAFT mới được submit
- Khách hàng blacklist không được tạo hồ sơ vay
```

---

## 4.14. `audit`

### Vai trò hiện tại

Package `audit` hiện tại là placeholder.

Sau này có thể dùng để ghi nhận:

- ai thực hiện hành động;
- hành động nào được thực hiện;
- thời điểm thực hiện;
- object nào bị tác động;
- event nào được sinh ra.

### Liên hệ với BA Step 4

Theo phân tích Actor - Action - Event:

```text
Actor thực hiện Action
Action tác động Object
Action sinh Event
```

Audit sau này có thể ghi lại chuỗi này.

Ví dụ:

```text
Nhân viên PGD submit hồ sơ vay
LoanApplication bị chuyển trạng thái
Event LoanApplicationSubmittedForApproval được sinh ra
```

Hiện tại demo chưa cần audit thật.

---

## 4.15. `LoanOnboardingApplication.java`

### Vai trò

Đây là class main để khởi động Spring Boot application.

Luồng khởi động:

```text
Run LoanOnboardingApplication
    ↓
Spring Boot scan package com.f88.loanonboarding
    ↓
Load controller/service/config
    ↓
Start server localhost:8080
```

Không nên đặt logic nghiệp vụ vào file này.

---

## 5. Mối liên hệ theo từng API chính của DEMO Flow 1

## 5.1. Customer Lookup

```text
CustomerController
    ↓
CustomerLookupRequest
    ↓
CustomerService
    ↓
CustomerServiceMockImpl
    ↓
DemoCustomerMockDataProvider
    ↓
CustomerLookupResponse
    ↓
ApiResponse<CustomerLookupResponse>
```

Mục đích:

```text
Tra cứu khách hàng trước khi tạo hồ sơ vay.
```

---

## 5.2. Create Loan Application Draft

```text
LoanApplicationController
    ↓
CreateLoanApplicationRequest
    ↓
LoanApplicationService
    ↓
LoanApplicationServiceMockImpl
    ↓
DemoLoanApplicationMockDataProvider
    ↓
CreateLoanApplicationResponse
    ↓
ApiResponse<CreateLoanApplicationResponse>
```

Mục đích:

```text
Tạo hồ sơ vay nháp.
```

---

## 5.3. Save Loan Application Draft

```text
LoanApplicationController
    ↓
UpdateLoanApplicationDraftRequest
    ↓
LoanApplicationService
    ↓
LoanApplicationServiceMockImpl
    ↓
DemoLoanApplicationMockDataProvider
    ↓
LoanApplicationDraftResponse
    ↓
ApiResponse<LoanApplicationDraftResponse>
```

Mục đích:

```text
Lưu thông tin hồ sơ vay đang nhập.
```

---

## 5.4. Asset Lookup

```text
AssetController
    ↓
AssetLookupRequest
    ↓
AssetService
    ↓
AssetServiceMockImpl
    ↓
DemoAssetMockDataProvider
    ↓
AssetLookupResponse
    ↓
ApiResponse<AssetLookupResponse>
```

Mục đích:

```text
Tra cứu thông tin tài sản trước khi gắn vào hồ sơ vay.
```

---

## 5.5. Save Asset Snapshot

```text
AssetController / LoanApplicationController
    ↓
SaveAssetSnapshotRequest
    ↓
AssetService / LoanApplicationService
    ↓
Mock Service Implementation
    ↓
DemoAssetMockDataProvider
    ↓
AssetSnapshotResponse
    ↓
ApiResponse<AssetSnapshotResponse>
```

Mục đích:

```text
Lưu snapshot thông tin tài sản vào hồ sơ vay nháp.
```

---

## 5.6. Valuation Preview

```text
AssetValuationController
    ↓
ValuationPreviewRequest
    ↓
AssetValuationService
    ↓
AssetValuationServiceMockImpl
    ↓
DemoValuationMockDataProvider
    ↓
ValuationPreviewResponse
    ↓
ApiResponse<ValuationPreviewResponse>
```

Mục đích:

```text
Tính thử giá trị định giá tài sản.
```

---

## 5.7. Eligibility Check

```text
EligibilityController
    ↓
EligibilityCheckRequest
    ↓
EligibilityService
    ↓
EligibilityServiceMockImpl
    ↓
DemoEligibilityMockDataProvider
    ↓
EligibilityCheckResponse
    ↓
ApiResponse<EligibilityCheckResponse>
```

Mục đích:

```text
Kiểm tra điều kiện vay cơ bản trước khi gửi hồ sơ đi phê duyệt.
```

---

## 5.8. Submit For Approval

```text
LoanApplicationController
    ↓
applicationCode
    ↓
LoanApplicationService
    ↓
LoanApplicationServiceMockImpl
    ↓
Rule / Guard Demo
    ↓
DemoLoanApplicationMockDataProvider
    ↓
SubmitForApprovalResponse
    ↓
ApiResponse<SubmitForApprovalResponse>
```

Mục đích:

```text
Kết thúc DEMO Flow 1 bằng việc gửi hồ sơ vay sang bước phê duyệt.
```

Kết quả mong muốn:

```text
applicationState = APP_SUBMITTED
approvalCaseCode được sinh ra
eventName = LoanApplicationSubmittedForApproval
```

---

## 6. Quan hệ giữa các package

## 6.1. Quan hệ nên có

```text
controller → service
service → rule
service → mock
service → dto
controller → dto
controller → common
exception → common
config → toàn app
```

## 6.2. Quan hệ chưa nên có ở giai đoạn hiện tại

```text
controller → repository
controller → entity
controller → mock
controller → rule trực tiếp
dto → entity
entity → dto
mock → controller
repository → controller
```

### Lý do

Controller không nên biết dữ liệu đến từ mock hay DB.

Controller chỉ nên biết service.

---

## 7. Quy tắc thêm API mới

Khi cần thêm một API mới, nên đi theo thứ tự:

```text
1. Xác định API thuộc domain nào
2. Tạo Request DTO nếu API cần body
3. Tạo Response DTO
4. Thêm method vào Service Interface
5. Implement method trong Mock Service
6. Nếu cần dữ liệu giả, thêm vào Mock Data Provider
7. Nếu cần guard đơn giản, thêm vào rule package
8. Thêm endpoint vào Controller
9. Cập nhật Swagger/API docs
10. Test trên Swagger
```

Không nên bắt đầu bằng entity/repository nếu DB chưa chốt.

---

## 8. Quy tắc đặt code đúng package

| Loại code | Đặt ở package |
|---|---|
| REST endpoint | `controller` |
| Request/Response object | `dto` |
| Logic flow/API use case | `service` |
| Dữ liệu giả demo | `mock` |
| Rule/guard kiểm soát action | `rule` |
| Response wrapper | `common` |
| Exception/error handling | `exception` |
| Enum trạng thái/kết quả | `enums` |
| Cấu hình Spring/OpenAPI/CORS | `config` |
| Entity JPA | `entity` |
| Repository JPA | `repository` |
| Convert DTO ↔ Entity | `mapper` |
| Custom input validation | `validation` |
| Audit log/event tracking | `audit` |

---

## 9. Những package chưa triển khai thật và lý do

| Package | Trạng thái | Lý do |
|---|---|---|
| `entity` | Placeholder | Chờ ERD/DB schema |
| `repository` | Placeholder | Chờ DB thật |
| `mapper` | Placeholder | Chưa có entity thật để mapping |
| `validation` | Placeholder | Chưa cần custom validation sâu |
| `audit` | Placeholder | Demo chưa cần audit log thật |

Các package này được giữ lại để định hướng cấu trúc backend, không có nghĩa là đã triển khai production.

---

## 10. Ranh giới phạm vi hiện tại

## Hiện tại làm

```text
API skeleton
Swagger/OpenAPI
DTO request/response
Mock service
Mock data provider
ApiResponse<T>
Exception handling
Demo guard rule đơn giản
Submit hồ sơ đi phê duyệt
Tài liệu API/test/dev handoff
```

## Hiện tại chưa làm

```text
Rule engine hoàn chỉnh
Rule registry
Unit test rule chi tiết
Controller test chi tiết
Entity JPA thật
Repository thật
Migration DB
Authentication/Authorization
Approval decision full flow
Contract signing
Disbursement
Production deployment
```

---

## 11. Tư duy kiến trúc hiện tại

Project hiện tại nên được hiểu là:

```text
Demo-ready backend cho Flow 1
API-first
Mock-first
Swagger-driven
Chưa phụ thuộc ERD/DB
Chưa phải production system
```

Không nên hiểu project hiện tại là:

```text
Hệ thống loan onboarding production hoàn chỉnh
Rule engine hoàn chỉnh
Database implementation hoàn chỉnh
Approval/Contract/Disbursement end-to-end hoàn chỉnh
```

---

## 12. Gợi ý cho AI/DEV khi đọc project

Khi AI/DEV mới vào project, nên đọc theo thứ tự:

```text
1. README.md
2. docs/00_READ_ME_FIRST.md
3. docs/01_DEMO_FLOW_1.md
4. docs/api/01_API_REQUEST_RESPONSE_SAMPLES.md
5. docs/api-test/01_API_SWAGGER_TEST_REPORT.md
6. docs/backend/01_RULE_SKELETON.md
7. File controller tương ứng với API cần sửa
8. File service tương ứng
9. File mock data provider tương ứng
10. DTO request/response tương ứng
```

Không nên đọc ngay vào `entity` hoặc `repository`, vì hiện tại chưa có DB thật.

---

## 13. Checklist trước khi sửa code

Trước khi sửa một chức năng, cần trả lời:

```text
API này thuộc Flow 1 không?
API này phục vụ bước nào trong demo?
Có cần DTO mới không?
Có cần mock data mới không?
Có cần guard rule đơn giản không?
Có cần cập nhật tài liệu API sample không?
Có cần cập nhật Swagger test checklist không?
```

Nếu câu trả lời là có, cập nhật code và tài liệu cùng lúc.

---

## 14. Kết luận

Cấu trúc hiện tại được thiết kế để phục vụ giai đoạn chuẩn bị phát triển theo yêu cầu mentor:

```text
Có backend chạy được
Có Swagger xem được
Có API contract
Có mock service
Có dữ liệu mock tách riêng
Có rule/guard demo
Có tài liệu để AI/DEV/FE đọc hiểu
Có điểm kết thúc rõ ràng cho Flow 1: Submit For Approval
```

Các phần phức tạp như rule engine, controller test chi tiết, entity/repository thật và DB migration chưa cần triển khai ở giai đoạn này vì project hiện tại chỉ cần demo luồng tạo hồ sơ vay và gửi đi phê duyệt.
