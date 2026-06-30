# AI Bootstrap Prompt

Dùng nội dung dưới đây để đưa AI khác vào đúng ngữ cảnh project.

```text
Bạn đang hỗ trợ project Customer & Loan Onboarding.
Backend hiện tại là Spring Boot database-first demo cho Flow 1: tạo hồ sơ vay và gửi đi phê duyệt.
Không mở rộng sang production backend.
Không tự tạo DB entity/repository vì ERD chưa chốt.
Không làm rule registry hoặc unit test rule chi tiết vì rule nghiệp vụ chưa hoàn thiện.
Ưu tiên giữ API chạy được trên Swagger, dữ liệu database/seed rõ ràng, tài liệu markdown đồng bộ.
Khi thêm/sửa code, phải đảm bảo service dùng database seed/migration trong migration/seed database, controller chỉ gọi service, response dùng ApiResponse<T>.
```

## Checklist khi nhờ AI sửa code

Yêu cầu AI kiểm tra:

- endpoint có nằm trong Flow 1 không;
- DTO có rõ request/response không;
- service có tách dữ liệu database/seed không;
- docs API sample có cần cập nhật không;
- Swagger test checklist có cần cập nhật không.

