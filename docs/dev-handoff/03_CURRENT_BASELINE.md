# Current Baseline

## 1. Backend đã có

| Thành phần | Trạng thái |
|---|---|
| Health API | Hoàn thành |
| Swagger/OpenAPI | Hoàn thành |
| Customer API | Có skeleton/database |
| Loan Application API | Có skeleton/database |
| Asset API | Có skeleton/database |
| Asset Valuation API | Có skeleton/database |
| Eligibility API | Có skeleton/database |
| Submit for Approval API | Có skeleton/database |
| Reference Data API | Có skeleton/database |
| ApiResponse | Hoàn thành |
| Exception Handler | Hoàn thành |
| Database seed/migration | Đã tách |
| Rule Skeleton | Có, tối giản |

## 2. Endpoint quan trọng nhất

```text
POST /api/v1/loan-applications/{applicationCode}/submit-for-approval
```

Endpoint này đánh dấu demo Flow 1 hoàn thành.

## 3. Những gì đang chờ

| Chờ | Ảnh hưởng |
|---|---|
| BA chốt rule nghiệp vụ | Chưa viết unit test rule chi tiết |
| ERD/DB schema | Chưa tạo entity/repository thật |
| FE prototype hoàn thiện | Có thể cần chỉnh request/response |
| Mentor xác nhận scope tiếp theo | Chưa mở rộng approval/contract/disbursement |

