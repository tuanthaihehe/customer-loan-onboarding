# DEV Start Here

## 1. Chạy project

```powershell
cd backend/loan-onboarding
.\mvnw clean test
.\mvnw spring-boot:run
```

Mở:

```text
http://localhost:8080/swagger-ui/index.html
```

## 2. Test nhanh

1. Gọi `GET /api/v1/health`.
2. Làm theo `docs/01_DEMO_FLOW_1.md`.
3. Bước cuối phải trả `APP_SUBMITTED`.

## 3. Package cần biết

| Package | Ý nghĩa |
|---|---|
| `controller` | REST API |
| `service` | Interface |
| `service.impl` | Database service hiện tại |
| `database` | Dữ liệu giả demo |
| `dto` | Request/response |
| `rule` | Rule skeleton |
| `entity/repository` | Placeholder, chưa dùng thật |

## 4. Không làm khi chưa hỏi team

- Không tạo DB schema riêng.
- Không đổi endpoint đã có nếu FE đang dùng.
- Không thêm rule engine.
- Không mở rộng sang disbursement.

