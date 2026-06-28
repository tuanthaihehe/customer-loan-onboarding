# AI Context - Customer & Loan Onboarding Demo Backend

Tài liệu này dùng để cung cấp ngữ cảnh cho AI hoặc DEV mới khi tiếp tục làm trên repository.

## 1. Vai trò của project

Project hiện tại là backend demo cho đề tài **Customer & Loan Onboarding**.

Không được hiểu đây là production backend hoàn chỉnh. Đây là baseline để:

- chứng minh backend chạy được;
- có Swagger cho FE/BA/DEV review;
- có API skeleton cho Flow 1;
- có mock service khi chưa có ERD/DB;
- có tài liệu để AI/DEV tiếp tục làm đúng hướng.

## 2. Scope bắt buộc giữ

Chỉ tập trung Flow 1:

```text
Tạo hồ sơ vay → bổ sung thông tin → định giá/eligibility mock → gửi đi phê duyệt
```

Không tự mở rộng sang:

- approval decision đầy đủ;
- contract;
- disbursement;
- core banking integration;
- rule engine production;
- DB entity/repository thật.

## 3. Nguyên tắc code

| Nguyên tắc | Cách áp dụng |
|---|---|
| API-first | Tạo/đọc API contract trước khi nghĩ DB |
| Mock-first | Chạy được khi chưa có DB |
| Không viết logic trong controller | Controller chỉ nhận request và gọi service |
| Mock data tách riêng | Dữ liệu giả nằm trong package `mock` |
| Rule đơn giản | Chỉ dùng demo guard/rule skeleton, chưa làm rule registry |
| Không over-engineering | Không thêm framework/lớp phức tạp nếu không phục vụ Flow 1 |

## 4. Package quan trọng

| Package | Ý nghĩa |
|---|---|
| `controller` | REST API endpoint |
| `service` | Interface nghiệp vụ |
| `service.impl` | Mock implementation hiện tại |
| `mock` | Dữ liệu giả dùng cho demo |
| `dto.request` | Request body |
| `dto.response` | Response data |
| `rule` | Rule skeleton/demo guard |
| `common.response` | `ApiResponse<T>` chuẩn |
| `exception` | Exception handling |
| `entity`, `repository`, `mapper` | Placeholder, chưa dùng thật khi ERD chưa chốt |

## 5. Việc nên làm tiếp

Nên làm:

```text
- Đồng bộ docs/api sample với Swagger.
- Đồng bộ API với màn hình FE demo.
- Giữ mock data provider sạch, dễ chỉnh scenario.
- Cập nhật docs khi thêm/sửa endpoint.
```

Chưa nên làm:

```text
- Unit test chi tiết cho rule.
- Controller test chi tiết theo từng rule.
- Rule registry.
- Entity/repository thật.
- Migration DB.
```

Lý do: rule và ERD chưa chốt, scope hiện tại chỉ là demo Flow 1.
