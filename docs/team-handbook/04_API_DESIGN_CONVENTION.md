# API Design Convention

## 1. Base path

```text
/api/v1
```

## 2. Response format

Tất cả API trả:

```json
{
  "success": true,
  "message": "...",
  "data": {},
  "errorCode": null,
  "timestamp": "..."
}
```

## 3. Naming endpoint

| Hành động | Method | Ví dụ |
|---|---|---|
| Lấy dữ liệu | GET | `/loan-applications/{applicationCode}` |
| Tạo mới | POST | `/loan-applications` |
| Lưu/cập nhật một phần | PATCH | `/loan-applications/{applicationCode}/draft` |
| Chạy hành động nghiệp vụ | POST | `/loan-applications/{applicationCode}/submit-for-approval` |

## 4. API phải phục vụ Flow 1

Endpoint mới chỉ nên thêm khi phục vụ trực tiếp Flow 1 hoặc được mentor yêu cầu.

## 5. Swagger

Mỗi controller cần có:

- `@Tag`;
- `@Operation(summary = "...")`;
- request body có validation nếu cần.
