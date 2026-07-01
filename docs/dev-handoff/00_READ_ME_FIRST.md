# Dev Handoff - Đọc trước

Bộ tài liệu này dành cho DEV/AI tiếp tục phát triển backend.

## 1. Tình trạng hiện tại

Project đang ở trạng thái **demo-ready baseline** cho Flow 1.

Đã có:

- Spring Boot backend;
- Swagger/OpenAPI;
- API skeleton theo Flow 1;
- DTO request/response;
- database service;
- database seed/migration;
- rule skeleton tối giản;
- tài liệu API/test/handoff.

## 2. Scope cần giữ

Chỉ phát triển để demo:

```text
Tạo hồ sơ vay và gửi đi phê duyệt
```

Không phát triển sâu:

- approve/reject;
- contract;
- disbursement;
- DB persistence;
- security;
- rule engine.

## 3. Thứ tự đọc

| Thứ tự | File |
|---:|---|
| 1 | `docs/00_READ_ME_FIRST.md` |
| 2 | `docs/01_DEMO_FLOW_1.md` |
| 3 | `docs/api/01_API_REQUEST_RESPONSE_SAMPLES.md` |
| 4 | `docs/api-test/01_API_SWAGGER_TEST_REPORT.md` |
| 5 | `docs/backend/01_RULE_SKELETON.md` |
| 6 | `docs/dev-handoff/03_CURRENT_BASELINE.md` |
| 7 | `docs/dev-handoff/07_BACKEND_TASK_BOARD.md` |

## 4. Quy tắc làm tiếp

- Không đổi scope nếu chưa có yêu cầu mới.
- Không tạo entity/repository thật khi chưa có ERD.
- Không đưa dữ liệu database/seed vào service.
- Không viết business logic trong controller.
- Khi thêm API phải cập nhật sample và test checklist.

