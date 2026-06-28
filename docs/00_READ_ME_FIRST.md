# Đọc trước khi phát triển

Tài liệu này giải thích nhanh repository hiện tại dùng để làm gì, phạm vi nằm ở đâu và nên đọc file nào trước.

## 1. Mục tiêu repository

Repository này phục vụ demo backend cho đề tài **Customer & Loan Onboarding**.

Mục tiêu hiện tại:

```text
Tạo được một hồ sơ vay có đủ thông tin cơ bản và gửi hồ sơ đó sang bước phê duyệt.
```

Đây là baseline **API-first + Mock-first**, không phải hệ thống production.

## 2. Phạm vi demo Flow 1

Flow 1 gồm các bước:

| Bước | Ý nghĩa | Endpoint chính |
|---:|---|---|
| 1 | Tra cứu khách hàng | `POST /api/v1/customers/lookup` |
| 2 | Tạo hồ sơ vay nháp | `POST /api/v1/loan-applications` |
| 3 | Lưu thông tin khách hàng/khoản vay | `PATCH /api/v1/loan-applications/{applicationCode}/draft` |
| 4 | Tra cứu tài sản | `POST /api/v1/assets/lookup` |
| 5 | Lưu tài sản vào hồ sơ | `PATCH /api/v1/loan-applications/{applicationCode}/asset-snapshot` |
| 6 | Tính định giá thử | `POST /api/v1/loan-applications/{applicationCode}/asset-valuations/preview` |
| 7 | Lưu kết quả định giá thử | `PATCH /api/v1/loan-applications/{applicationCode}/valuation-preview` |
| 8 | Chạy eligibility check | `POST /api/v1/loan-applications/{applicationCode}/eligibility-checks` |
| 9 | Gửi hồ sơ đi phê duyệt | `POST /api/v1/loan-applications/{applicationCode}/submit-for-approval` |

## 3. Những điểm đã thống nhất

| Chủ đề | Quyết định |
|---|---|
| Unit test rule | Chưa cần làm chi tiết vì rule nghiệp vụ chưa chốt |
| Controller test | Chưa cần làm chi tiết; ưu tiên Swagger manual test checklist |
| Mock data provider | Cần tách riêng khỏi service |
| Rule registry | Chưa cần; giữ rule skeleton đơn giản |
| Entity/Repository | Chưa làm thật; chờ ERD |
| Approval/Contract/Disbursement | Chưa làm sâu; ngoài Flow 1 |

## 4. Cách đọc tài liệu

| Nhu cầu | File cần đọc |
|---|---|
| Muốn hiểu nhanh project | `README.md` và file này |
| Muốn chạy demo flow | `docs/01_DEMO_FLOW_1.md` |
| Muốn test API bằng Swagger | `docs/api-test/01_API_SWAGGER_TEST_REPORT.md` |
| Muốn copy request/response mẫu | `docs/api/01_API_REQUEST_RESPONSE_SAMPLES.md` |
| Muốn hiểu rule hiện tại | `docs/backend/01_RULE_SKELETON.md` |
| AI/DEV muốn tiếp tục code | `docs/ai-context/01_AI_CONTEXT.md` |
| Dev mới vào project | `docs/dev-handoff/00_READ_ME_FIRST.md` |

## 5. Lưu ý khi phát triển tiếp

Không mở rộng bài toán theo hướng production quá sớm. Hiện tại chỉ cần chứng minh backend đủ khả năng phục vụ demo Flow 1.

Nên làm tiếp:

```text
1. Chạy lại Swagger theo đúng Flow 1.
2. Đảm bảo endpoint submit-for-approval hoạt động.
3. Giữ mock data trong package mock.
4. Đồng bộ API sample với FE prototype.
5. Chờ ERD trước khi tạo entity/repository thật.
```
