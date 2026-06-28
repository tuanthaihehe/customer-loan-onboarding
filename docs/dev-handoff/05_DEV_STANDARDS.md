# DEV Standards

## 1. Coding rules

- Controller không chứa business logic.
- Service xử lý flow demo.
- Mock data nằm trong package `mock`.
- DTO không phụ thuộc entity.
- Response luôn dùng `ApiResponse<T>`.
- Không tạo entity/repository thật khi ERD chưa chốt.

## 2. Naming

| Loại | Quy ước |
|---|---|
| Controller | `XxxController` |
| Service interface | `XxxService` |
| Mock service | `XxxServiceMockImpl` |
| Mock data provider | `DemoXxxMockDataProvider` |
| Request DTO | `XxxRequest` |
| Response DTO | `XxxResponse` |

## 3. Khi thêm API

Cần cập nhật:

```text
1. Controller
2. Service interface
3. Mock service implementation
4. Request/response DTO nếu cần
5. docs/api/01_API_REQUEST_RESPONSE_SAMPLES.md
6. docs/api-test/01_API_SWAGGER_TEST_REPORT.md
```
