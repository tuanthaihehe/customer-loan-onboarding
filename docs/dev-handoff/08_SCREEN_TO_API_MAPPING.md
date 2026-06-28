# Screen to API Mapping

| Screen | API | Ghi chú |
|---|---|---|
| Health/Dev check | `GET /api/v1/health` | Kiểm tra backend |
| Customer lookup | `POST /api/v1/customers/lookup` | Tra cứu/định danh sơ bộ KH |
| Create application | `POST /api/v1/loan-applications` | Tạo hồ sơ nháp |
| Application form | `PATCH /api/v1/loan-applications/{applicationCode}/draft` | Lưu thông tin KH/khoản vay |
| Application detail | `GET /api/v1/loan-applications/{applicationCode}` | Xem tổng hợp mock |
| Asset lookup | `POST /api/v1/assets/lookup` | Kiểm tra tài sản |
| Asset form | `PATCH /api/v1/loan-applications/{applicationCode}/asset-snapshot` | Gắn tài sản vào hồ sơ |
| Valuation preview | `POST /api/v1/loan-applications/{applicationCode}/asset-valuations/preview` | Tính thử định giá |
| Save valuation | `PATCH /api/v1/loan-applications/{applicationCode}/valuation-preview` | Lưu kết quả định giá |
| Eligibility | `POST /api/v1/loan-applications/{applicationCode}/eligibility-checks` | Kiểm tra điều kiện |
| Submit | `POST /api/v1/loan-applications/{applicationCode}/submit-for-approval` | Kết thúc Flow 1 |
| Dropdowns | `GET /api/v1/reference-data/...` | Danh mục FE |

## Chưa mapping

| Screen/Module | Lý do |
|---|---|
| Approval decision | Ngoài Flow 1 |
| Contract | Ngoài Flow 1 |
| Disbursement | Ngoài Flow 1 |
