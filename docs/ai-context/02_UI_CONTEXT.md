# UI Context - Mapping màn hình demo với backend

Tài liệu này giúp FE/AI hiểu backend hiện tại phục vụ UI demo nào.

## 1. Màn hình nên có trong demo

| Màn hình | Backend API liên quan |
|---|---|
| Tra cứu khách hàng | `POST /api/v1/customers/lookup` |
| Tạo hồ sơ vay | `POST /api/v1/loan-applications` |
| Nhập thông tin khoản vay | `PATCH /api/v1/loan-applications/{applicationCode}/draft` |
| Nhập/tra cứu tài sản | `POST /api/v1/assets/lookup`, `PATCH /asset-snapshot` |
| Định giá thử tài sản | `POST /asset-valuations/preview`, `PATCH /valuation-preview` |
| Kiểm tra điều kiện vay | `POST /eligibility-checks` |
| Gửi phê duyệt | `POST /submit-for-approval` |

## 2. Dropdown/reference data

Các danh mục FE có thể gọi:

- genders;
- occupations;
- loan purposes;
- asset types;
- vehicle brands;
- vehicle models;
- vehicle variants;
- manufacture years;
- vehicle colors;
- valuation deduction factors.

Base path:

```text
GET /api/v1/reference-data/...
```

## 3. Lưu ý cho FE

- Backend đang chạy dữ liệu database/seed, chưa lưu DB thật.
- Một số API trả dữ liệu cố định để phục vụ demo.
- CORS local đã mở cho `localhost:3000`, `localhost:5173`, `localhost:8081`.
- FE nên demo theo đúng thứ tự Flow 1 để dữ liệu dễ hiểu.

