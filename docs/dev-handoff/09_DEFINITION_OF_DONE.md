# Definition of Done - Demo Flow 1

Một thay đổi backend được xem là xong khi đạt các tiêu chí sau.

## 1. Code

- API nằm trong scope Flow 1 hoặc được mentor yêu cầu.
- Controller chỉ gọi service.
- Service không hard-code mock data dài dòng.
- Mock data nằm trong package `mock`.
- Response dùng `ApiResponse<T>`.
- Không tạo entity/repository thật nếu chưa có ERD.

## 2. Swagger

- Endpoint hiển thị trên Swagger.
- Request body có schema rõ.
- Response chạy được bằng dữ liệu mẫu.

## 3. Tài liệu

Khi thêm/sửa API phải cập nhật:

- `docs/api/01_API_REQUEST_RESPONSE_SAMPLES.md`;
- `docs/api-test/01_API_SWAGGER_TEST_REPORT.md`;
- các file handoff liên quan nếu scope thay đổi.

## 4. Test

Giai đoạn hiện tại yêu cầu:

```text
Manual Swagger test checklist pass
```

Chưa bắt buộc:

- unit test rule chi tiết;
- controller test chi tiết;
- integration test DB.

## 5. Demo pass

Demo pass khi endpoint cuối trả:

```text
applicationState = APP_SUBMITTED
approvalCaseCode = APR-2026-000001
```
