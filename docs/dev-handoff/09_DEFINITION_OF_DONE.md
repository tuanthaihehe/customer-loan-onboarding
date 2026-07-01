# Definition of Done

Một thay đổi backend được xem là xong khi đạt các tiêu chí sau.

## Code

- Controller chỉ gọi service.
- Service dùng database/migration/seed thật, không tự tạo dữ liệu giả.
- Nếu schema chưa có, trả lỗi nghiệp vụ rõ ràng bằng tiếng Việt.
- Response dùng `ApiResponse<T>`.
- Không sửa `.env`, `docker`, hoặc `db/migration` nếu không có yêu cầu rõ.

## Swagger

- Endpoint hiển thị trên Swagger.
- Request body có schema rõ.
- Response success/error đúng format.

## Database

- Flyway validate/migrate thành công.
- Không tự thiết kế bảng ngoài migration BA/DA đã đưa.
- Khi BA/DA thêm migration/seed mới, cập nhật service và tài liệu theo schema đó.

## Test

Tối thiểu cần chạy:

```powershell
cd backend/loan-onboarding
.\mvnw clean test
```

Với workflow hiện tại, demo pass khi customer/loan APIs đọc ghi database thật và submit chuyển state sang `APP_SUBMITTED`.
