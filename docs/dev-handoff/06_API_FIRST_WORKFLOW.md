# API-first Workflow

## 1. Quy trình thêm/sửa API

```text
Xác định màn hình/flow cần API
→ Xác định request/response
→ Tạo DTO
→ Tạo service method
→ Tạo mock data provider nếu cần
→ Tạo controller endpoint
→ Test Swagger
→ Cập nhật tài liệu API sample và test checklist
```

## 2. Không làm ngược

Không bắt đầu từ database/entity khi ERD chưa chốt.

Sai hướng:

```text
Tự thiết kế table → tạo entity → ép API theo DB
```

Đúng hướng hiện tại:

```text
Luồng demo → API contract → DTO → mock service → Swagger
```
