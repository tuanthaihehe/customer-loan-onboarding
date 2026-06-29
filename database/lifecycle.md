# Loan Application Lifecycle

Tài liệu này mô tả cách hệ thống quản lý vòng đời của hồ sơ vay (`loan_application`). Trong phạm vi hiện tại, chỉ hồ sơ vay có lifecycle đầy đủ. Khách hàng (`customer`) và tài sản (`asset`) chỉ có trường `status` đơn giản.

## 1. Mục tiêu thiết kế

Lifecycle của hồ sơ vay cần trả lời được các câu hỏi sau:

1. Hồ sơ hiện tại đang ở trạng thái nào?
2. Trạng thái nào là trạng thái bắt đầu?
3. Trạng thái nào là trạng thái kết thúc?
4. Từ trạng thái hiện tại, hồ sơ được phép chuyển sang trạng thái nào?
5. Hồ sơ đã chuyển trạng thái lúc nào, bởi ai và bằng hành động nào?

Thiết kế hiện tại sử dụng 4 thành phần:

| Thành phần | Vai trò |
|---|---|
| `loan_application.current_state_id` | Lưu trạng thái hiện tại của hồ sơ. |
| `loan_application_state` | Lưu danh sách trạng thái hợp lệ. |
| `loan_application_state_transition` | Lưu các transition hợp lệ giữa các trạng thái. |
| `loan_application_state_history` | Lưu lịch sử chuyển trạng thái thực tế của từng hồ sơ. |

---

## 2. Danh sách state

| Code | Tên | Initial | Terminal | Ý nghĩa |
|---|---|:---:|:---:|---|
| `APP_DRAFT` | Hồ sơ nháp | Yes | No | Hồ sơ mới được tạo, chưa nộp vào luồng xử lý. |
| `APP_SUBMITTED` | Đã nộp hồ sơ | No | No | Hồ sơ đã được gửi vào luồng xử lý. |
| `APP_NEEDS_SUPPLEMENT` | Cần bổ sung hồ sơ | No | No | Hồ sơ thiếu thông tin hoặc giấy tờ và cần được bổ sung. |
| `APP_IN_REVIEW` | Đang thẩm định/phê duyệt | No | No | Hồ sơ đang được kiểm tra, thẩm định hoặc phê duyệt. |
| `APP_READY_FOR_CONTRACT` | Sẵn sàng lập hợp đồng | No | No | Hồ sơ đã đủ điều kiện để tạo hợp đồng. |
| `APP_CONTRACTED` | Đã có hợp đồng | No | Yes | Hồ sơ đã được tạo hợp đồng và kết thúc lifecycle trong module hiện tại. |
| `APP_CANCELLED` | Hồ sơ bị hủy | No | Yes | Hồ sơ bị hủy và không tiếp tục xử lý. |

State bắt đầu là `APP_DRAFT`. State kết thúc hiện tại gồm `APP_CONTRACTED` và `APP_CANCELLED`.

---

## 3. Allowed transitions

Bảng `loan_application_state_transition` định nghĩa đường đi hợp lệ giữa các state. Backend nên kiểm tra bảng này trước khi cập nhật `loan_application.current_state_id`.

| From | Action | To | Ý nghĩa |
|---|---|---|---|
| `APP_DRAFT` | `SUBMIT` | `APP_SUBMITTED` | Nộp hồ sơ nháp vào luồng xử lý. |
| `APP_DRAFT` | `CANCEL` | `APP_CANCELLED` | Hủy hồ sơ khi còn ở trạng thái nháp. |
| `APP_SUBMITTED` | `START_REVIEW` | `APP_IN_REVIEW` | Bắt đầu thẩm định/phê duyệt. |
| `APP_SUBMITTED` | `REQUEST_SUPPLEMENT` | `APP_NEEDS_SUPPLEMENT` | Yêu cầu bổ sung sau khi hồ sơ đã nộp. |
| `APP_SUBMITTED` | `CANCEL` | `APP_CANCELLED` | Hủy hồ sơ sau khi đã nộp. |
| `APP_NEEDS_SUPPLEMENT` | `RESUBMIT` | `APP_SUBMITTED` | Nộp lại hồ sơ sau khi bổ sung. |
| `APP_IN_REVIEW` | `REQUEST_SUPPLEMENT` | `APP_NEEDS_SUPPLEMENT` | Yêu cầu bổ sung trong quá trình thẩm định/phê duyệt. |
| `APP_IN_REVIEW` | `APPROVE_FOR_CONTRACT` | `APP_READY_FOR_CONTRACT` | Duyệt hồ sơ để lập hợp đồng. |
| `APP_READY_FOR_CONTRACT` | `CREATE_CONTRACT` | `APP_CONTRACTED` | Tạo hợp đồng từ hồ sơ đủ điều kiện. |
| `APP_READY_FOR_CONTRACT` | `CANCEL` | `APP_CANCELLED` | Hủy hồ sơ trước khi lập hợp đồng. |

Ví dụ transition không hợp lệ:

- `APP_DRAFT -> APP_CONTRACTED`
- `APP_CANCELLED -> APP_SUBMITTED`
- `APP_CONTRACTED -> APP_IN_REVIEW`

Các transition này không tồn tại trong bảng `loan_application_state_transition`, nên backend không nên cho phép cập nhật state theo các hướng đó.

---

## 4. Quy trình cập nhật state

Khi hệ thống cần chuyển trạng thái một hồ sơ vay, backend nên thực hiện theo thứ tự sau:

1. Lấy hồ sơ vay hiện tại từ `loan_application`.
2. Đọc `current_state_id` của hồ sơ.
3. Kiểm tra transition hợp lệ trong `loan_application_state_transition` bằng `from_state_id`, `to_state_id` và `action_code`.
4. Nếu transition không hợp lệ, trả lỗi nghiệp vụ.
5. Nếu transition hợp lệ:
   - cập nhật `loan_application.current_state_id`
   - cập nhật các timestamp liên quan nếu cần, ví dụ `submitted_at`, `closed_at`
   - insert một dòng vào `loan_application_state_history`

### Ví dụ: nộp hồ sơ

Điều kiện:

- Hồ sơ hiện tại đang ở `APP_DRAFT`.
- Action là `SUBMIT`.
- Transition `APP_DRAFT -> APP_SUBMITTED` tồn tại trong bảng transition.

Khi nộp thành công:

- `loan_application.current_state_id` được đổi sang `APP_SUBMITTED`.
- `loan_application.submitted_at` được set thời điểm hiện tại.
- `loan_application_state_history` thêm một bản ghi:
  - `from_state_id = APP_DRAFT`
  - `to_state_id = APP_SUBMITTED`
  - `action_code = SUBMIT`
  - `changed_at = thời điểm chuyển`

### Ví dụ: tạo hồ sơ nháp

Khi tạo hồ sơ mới:

- `current_state_id` phải trỏ tới state `APP_DRAFT`.
- `asset_id` có thể `NULL` vì hồ sơ nháp có thể được tạo trước khi chọn tài sản.
- `loan_application_state_history` thêm một bản ghi đầu tiên:
  - `from_state_id = NULL`
  - `to_state_id = APP_DRAFT`
  - `action_code = CREATE`

---

## 5. Gắn tài sản vào hồ sơ vay

Trong Flow 1, hồ sơ vay nháp có thể được tạo trước, sau đó mới gắn tài sản. Vì vậy `loan_application.asset_id` là nullable.

Khi gắn tài sản vào hồ sơ, backend cần kiểm tra tối thiểu:

1. Hồ sơ vay tồn tại và chưa bị xóa mềm.
2. Tài sản tồn tại và chưa bị xóa mềm.
3. Tài sản thuộc đúng khách hàng của hồ sơ hoặc được nghiệp vụ cho phép sử dụng.
4. Tài sản không bị gắn vào một hồ sơ vay đang mở khác.

Điều kiện số 4 được enforce ở database bằng partial unique index:

```sql
CREATE UNIQUE INDEX uq_active_loan_application_asset
ON loan_application(asset_id)
WHERE asset_id IS NOT NULL
  AND closed_at IS NULL
  AND deleted_at IS NULL;
```

Index này bảo vệ dữ liệu ở mức database. Backend vẫn nên kiểm tra trước để trả lỗi nghiệp vụ dễ hiểu hơn.

---

## 6. Trạng thái kết thúc và `closed_at`

Khi hồ sơ chuyển sang state terminal, hệ thống nên set `loan_application.closed_at`.

State terminal hiện tại:

- `APP_CONTRACTED`
- `APP_CANCELLED`

Ý nghĩa của `closed_at`:

- Đánh dấu hồ sơ đã kết thúc trong module hiện tại.
- Giúp partial unique index xác định hồ sơ nào còn đang mở.
- Cho phép một tài sản đã từng dùng trong hồ sơ cũ có thể được dùng lại trong hồ sơ mới nếu nghiệp vụ cho phép.

---

## 7. Vì sao không dùng enum trực tiếp cho loan_application state?

`customer.status` và `asset.status` chỉ là trạng thái đơn giản, nên dùng enum/check constraint là đủ.

`loan_application` cần lifecycle rõ ràng hơn vì cần quản lý:

- state hiện tại
- state bắt đầu/kết thúc
- transition hợp lệ
- lịch sử chuyển trạng thái
- thời điểm chuyển
- người thực hiện
- ghi chú nghiệp vụ

Vì vậy loan application không dùng một cột `status` dạng string đơn giản, mà dùng bộ bảng lifecycle riêng.

---

## 8. Những phần chưa tách bảng ở phase hiện tại

Hiện tại schema chưa tách riêng các bảng sau:

- `asset_valuation`
- `eligibility_check`
- `approval_case`

Lý do: scope hiện tại tập trung vào việc tạo và quản lý hồ sơ vay ở mức cơ bản. Nếu hệ thống sau này cần lưu lịch sử định giá, kết quả kiểm tra điều kiện hoặc quy trình phê duyệt độc lập, các bảng này có thể được bổ sung bằng migration sau.
