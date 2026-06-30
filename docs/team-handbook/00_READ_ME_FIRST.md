# Team Handbook - Đọc trước

Handbook này thống nhất cách team làm backend demo Flow 1.

## 1. Mục tiêu chung

```text
Có backend chạy được, có Swagger, có API sequence rõ ràng để demo tạo hồ sơ vay và gửi đi phê duyệt.
```

## 2. Không làm quá phạm vi

Không biến project thành production backend khi chưa có yêu cầu.

Chưa làm sâu:

- unit test rule chi tiết;
- controller test chi tiết;
- rule registry;
- entity/repository thật;
- approval/contract/disbursement full.

## 3. Nguyên tắc team

- Cùng dùng một API sequence trong `docs/01_DEMO_FLOW_1.md`.
- Sửa API thì cập nhật tài liệu.
- Không tự đổi scope.
- Không hard-code dữ liệu database/seed trong service nếu có thể tách provider.
- Không viết business rule trong controller.

