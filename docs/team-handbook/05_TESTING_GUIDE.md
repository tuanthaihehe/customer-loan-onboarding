# Testing Guide

## 1. Chiến lược test hiện tại

Giai đoạn hiện tại ưu tiên:

```text
Swagger manual test checklist
```

Chưa ưu tiên:

```text
unit test chi tiết cho rule
controller test chi tiết
integration test database
```

Lý do: rule và ERD chưa chốt, API còn có thể thay đổi theo demo.

## 2. Cách test

1. Chạy backend.
2. Mở Swagger.
3. Làm theo `docs/api-test/01_API_SWAGGER_TEST_REPORT.md`.
4. Xác nhận endpoint cuối trả `APP_SUBMITTED`.

## 3. Test fail tối thiểu

Nên test một vài case thiếu field để chứng minh validation hoạt động:

- thiếu `fullName` ở Customer lookup;
- thiếu `customerCode` ở Create draft;
- thiếu `licensePlate` ở Asset lookup;
- `deductionItems.rate` âm ở valuation preview.

## 4. Khi nào thêm automated test?

Chỉ thêm khi:

- API/DTO ổn định;
- rule nghiệp vụ chốt;
- mentor yêu cầu nâng chất lượng kiểm thử;
- project vượt khỏi demo Flow 1.

