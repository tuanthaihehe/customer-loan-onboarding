# Project Review and Next Steps

## 1. Kết luận review

Project hiện tại phù hợp với yêu cầu mentor ở giai đoạn chuẩn bị phát triển:

```text
Có backend chạy được, có Swagger, có API skeleton, có database service, có rule skeleton tối giản, có tài liệu cho AI/DEV đọc.
```

Sau khi điều chỉnh, project đã được đưa về đúng scope:

```text
Demo Flow 1: tạo hồ sơ vay và gửi đi phê duyệt.
```

## 2. Điểm tốt hiện tại

| Điểm tốt | Nhận xét |
|---|---|
| API-first | Có Swagger để FE/BA/DEV review sớm |
| Database-first | Không bị chặn bởi ERD/DB |
| Có endpoint cuối Flow 1 | `submit-for-approval` đã có |
| Có response chuẩn | Dùng `ApiResponse<T>` |
| Có exception handling | Validation/business error có format chung |
| Database seed/migration đã tách | Service sạch hơn |
| Docs đã thống nhất lại | Không còn đẩy project theo hướng quá phức tạp |

## 3. Những điểm đã chỉnh theo thảo luận

| Nội dung | Kết quả |
|---|---|
| Unit test rule | Chuyển thành `Not now`, vì rule chưa chốt |
| Controller test | Chuyển thành `Not now`, ưu tiên Swagger manual test |
| Database seed/migration | Đã tách thành migration/seed database |
| Rule registry | Chuyển thành `Not now`, chưa cần cho demo |
| Scope | Chốt lại chỉ demo Flow 1 |
| Submit for approval | Đã bổ sung endpoint/response database |
| Docs | Viết lại theo hướng AI/DEV đọc được, không over-engineering |

## 4. Việc cần làm ngay trước khi push Git

```text
1. Chạy backend local.
2. Mở Swagger.
3. Test checklist trong docs/api-test/01_API_SWAGGER_TEST_REPORT.md.
4. Đảm bảo API cuối trả APP_SUBMITTED.
5. Commit code + docs.
```

## 5. Việc nên làm tiếp sau khi mentor/BA chốt thêm

| Điều kiện | Việc làm tiếp |
|---|---|
| FE có prototype ổn định | Điều chỉnh DTO/request theo màn hình |
| BA chốt rule | Bổ sung unit test rule |
| API ổn định | Bổ sung controller test cơ bản |
| ERD chốt | Tạo entity/repository/migration |
| Scope mở rộng | Bổ sung approval/contract/disbursement |

## 6. Việc không nên làm hiện tại

Không nên làm ngay:

- rule registry;
- rule engine production;
- database schema tự thiết kế;
- approval/contract/disbursement full;
- security/JWT;
- test automation quá chi tiết.

Lý do: không phục vụ trực tiếp mục tiêu demo Flow 1 và có thể làm project phức tạp không cần thiết.

