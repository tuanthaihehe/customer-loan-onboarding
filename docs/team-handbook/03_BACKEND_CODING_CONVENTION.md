# Backend Coding Convention

## Layering

```text
Controller → Service Interface → DB Service Implementation → Database
```

Controller chỉ nhận request, gọi service và bọc response bằng `ApiResponse<T>`.

## Service

Implementation dùng hậu tố:

```text
XxxServiceDbImpl
```

Service không hard-code dữ liệu thay database/seed. Nếu schema chưa hỗ trợ một nghiệp vụ, service trả lỗi nghiệp vụ rõ ràng bằng tiếng Việt thay vì tự giả lập dữ liệu.

## Database

- Migration chạy thật nằm trong `backend/loan-onboarding/src/main/resources/db/migration`.
- Tài liệu BA/DA nằm trong `database/`.
- Không sửa `.env`, `docker`, hoặc `db/migration` nếu không có yêu cầu rõ.
- Khi BA/DA thêm migration/seed mới, cập nhật service và tài liệu theo schema mới.

## DTO

- Request DTO nằm trong `dto.request`.
- Response DTO nằm trong `dto.response`.
- Không dùng entity làm request/response.
- `Map<String,Object>` chỉ dùng tạm cho detail tổng hợp khi schema còn nhỏ và chưa cần DTO con riêng.
