# Backend Coding Convention

## 1. Layering

```text
Controller → Service → Mock Data Provider / Rule Skeleton → Response DTO
```

## 2. Controller

Controller chỉ làm:

- nhận request;
- gọi service;
- bọc response bằng `ApiResponse<T>`;
- khai báo Swagger annotation.

Controller không làm:

- tính toán nghiệp vụ;
- tạo mock data dài dòng;
- gọi repository trực tiếp;
- xử lý rule phức tạp.

## 3. Service

Service xử lý flow demo và gọi rule/mock provider khi cần.

Tên class mock service:

```text
XxxServiceMockImpl
```

## 4. Mock data provider

Mock data đặt trong package:

```text
com.f88.loanonboarding.mock
```

Tên class:

```text
DemoXxxMockDataProvider
```

Mục đích: giúp service sạch, dễ đổi dữ liệu demo, dễ đồng bộ với FE.

## 5. DTO

- Request DTO nằm trong `dto.request`.
- Response DTO nằm trong `dto.response`.
- Không dùng entity làm request/response.
- `Map<String,Object>` chỉ chấp nhận tạm ở detail mock nếu cần linh hoạt trước ERD.

## 6. Entity/Repository

Chỉ tạo thật sau khi ERD chính thức được chốt.
