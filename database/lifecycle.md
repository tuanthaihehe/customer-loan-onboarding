# Loan Application Lifecycle

## 1. Mục đích

Lifecycle của hồ sơ vay được thiết kế để quản lý trạng thái xử lý của `loan_application` một cách rõ ràng và có thể truy vết.

Thiết kế hiện tại sử dụng 3 bảng đi kèm với `loan_application`:

- `loan_application_state`
- `loan_application_state_transition`
- `loan_application_state_history`

Trong đó:

- `loan_application_state` định nghĩa các state hợp lệ.
- `loan_application_state_transition` định nghĩa state nào được phép chuyển sang state nào.
- `loan_application_state_history` ghi nhận lịch sử chuyển state thực tế của từng hồ sơ vay.

---

## 2. Vai trò của từng bảng

### 2.1. `loan_application`

Bảng `loan_application` là hồ sơ vay nghiệp vụ.

Các trường liên quan đến lifecycle:

| Field | Ý nghĩa |
|---|---|
| `current_state_id` | State hiện tại của hồ sơ vay. |
| `asset_id` | Tài sản được gắn với hồ sơ vay. Nullable khi hồ sơ đang ở giai đoạn nháp. |

Khi cần biết hồ sơ đang ở đâu trong quy trình, backend đọc `loan_application.current_state_id`.

---

### 2.2. `loan_application_state`

Bảng này là danh mục state của hồ sơ vay.

Ví dụ:

| Code | Ý nghĩa |
|---|---|
| `APP_DRAFT` | Hồ sơ nháp. |
| `APP_SUBMITTED` | Hồ sơ đã được nộp. |
| `APP_NEEDS_SUPPLEMENT` | Hồ sơ cần bổ sung. |
| `APP_IN_REVIEW` | Hồ sơ đang thẩm định/phê duyệt. |
| `APP_READY_FOR_CONTRACT` | Hồ sơ sẵn sàng lập hợp đồng. |
| `APP_CONTRACTED` | Hồ sơ đã có hợp đồng. |
| `APP_CANCELLED` | Hồ sơ bị hủy. |

Hai cột quan trọng:

| Field | Ý nghĩa |
|---|---|
| `is_initial` | Đánh dấu state khởi tạo của hồ sơ vay. Hiện tại là `APP_DRAFT`. |
| `is_terminal` | Đánh dấu state kết thúc lifecycle. Hiện tại gồm `APP_CONTRACTED`, `APP_CANCELLED`. |

---

### 2.3. `loan_application_state_transition`

Bảng này định nghĩa các chuyển đổi state hợp lệ.

Ví dụ:

| From | Action | To |
|---|---|---|
| `APP_DRAFT` | `SUBMIT` | `APP_SUBMITTED` |
| `APP_DRAFT` | `CANCEL` | `APP_CANCELLED` |
| `APP_SUBMITTED` | `START_REVIEW` | `APP_IN_REVIEW` |
| `APP_SUBMITTED` | `REQUEST_SUPPLEMENT` | `APP_NEEDS_SUPPLEMENT` |
| `APP_NEEDS_SUPPLEMENT` | `RESUBMIT` | `APP_SUBMITTED` |
| `APP_IN_REVIEW` | `APPROVE_FOR_CONTRACT` | `APP_READY_FOR_CONTRACT` |
| `APP_READY_FOR_CONTRACT` | `CREATE_CONTRACT` | `APP_CONTRACTED` |

Backend sử dụng bảng này để kiểm tra một hành động chuyển state có hợp lệ hay không.

Ví dụ:

- `APP_DRAFT -> APP_SUBMITTED` là hợp lệ.
- `APP_DRAFT -> APP_CONTRACTED` là không hợp lệ vì không có transition tương ứng.

---

### 2.4. `loan_application_state_history`

Bảng này ghi nhận lịch sử chuyển state thực tế.

Ví dụ khi tạo hồ sơ vay nháp:

| from_state_id | to_state_id | action_code |
|---|---|---|
| `NULL` | `APP_DRAFT` | `CREATE` |

`from_state_id = NULL` vì trước khi tạo, hồ sơ chưa tồn tại trong hệ thống.

Ví dụ khi nộp hồ sơ:

| from_state_id | to_state_id | action_code |
|---|---|---|
| `APP_DRAFT` | `APP_SUBMITTED` | `SUBMIT` |

---

## 3. Luồng tạo hồ sơ vay nháp

Khi nhân viên tạo hồ sơ vay nháp, hệ thống thực hiện:

1. Tìm state khởi tạo trong `loan_application_state`, hiện tại là `APP_DRAFT`.
2. Insert một record vào `loan_application`.
3. Gán `current_state_id = APP_DRAFT`.
4. Để `asset_id = NULL` nếu chưa chọn tài sản.
5. Insert một record vào `loan_application_state_history` với:
   - `from_state_id = NULL`
   - `to_state_id = APP_DRAFT`
   - `action_code = CREATE`

Các bảng thay đổi:

| Bảng | Thay đổi |
|---|---|
| `loan_application` | Insert hồ sơ mới. |
| `loan_application_state_history` | Insert lịch sử tạo hồ sơ. |
| `loan_application_state` | Không đổi. |
| `loan_application_state_transition` | Không đổi. |

---

## 4. Luồng gắn tài sản vào hồ sơ vay

Tài sản được tạo/lưu trong bảng `asset`. Sau đó hồ sơ vay được cập nhật để tham chiếu đến tài sản.

Khi gắn tài sản:

```sql
UPDATE loan_application
SET asset_id = :asset_id
WHERE loan_application_code = :loan_application_code;
```

Việc gắn tài sản không bắt buộc phải tạo state mới. Đây là thay đổi dữ liệu của hồ sơ vay, không nhất thiết là một bước lifecycle riêng.

Nếu nghiệp vụ sau này yêu cầu theo dõi việc gắn/gỡ tài sản như event, có thể bổ sung action trong `loan_application_state_history` hoặc thiết kế bảng event riêng. Ở phiên bản hiện tại, `loan_application_state_history` chỉ dùng cho chuyển state chính của hồ sơ vay.

---

## 5. Kiểm tra tài sản có đang được dùng ở hồ sơ chưa kết thúc hay không

Vì `loan_application.asset_id` nullable và không có unique constraint cứng, backend cần kiểm tra trước khi gắn tài sản vào một hồ sơ khác.

Query kiểm tra:

```sql
SELECT la.*
FROM loan_application la
JOIN loan_application_state s ON s.id = la.current_state_id
WHERE la.asset_id = :asset_id
  AND s.is_terminal = FALSE;
```

Nếu query trả về record, tài sản đang được gắn với một hồ sơ vay chưa kết thúc. Backend không nên cho phép gắn tài sản đó vào hồ sơ vay active khác.

Lý do không dùng `UNIQUE(asset_id)`:

- Một tài sản có thể từng được sử dụng trong hồ sơ cũ đã kết thúc.
- Rule cần phụ thuộc vào lifecycle state của hồ sơ vay.
- `is_terminal` nằm trong `loan_application_state`, nên kiểm tra ở tầng service rõ ràng hơn trong giai đoạn hiện tại.

---

## 6. Luồng nộp hồ sơ

Điều kiện tối thiểu trước khi nộp hồ sơ có thể gồm:

- Hồ sơ đang ở `APP_DRAFT`.
- Hồ sơ thuộc về một khách hàng hợp lệ.
- Các thông tin cần thiết của hồ sơ đã được nhập.
- Nếu nghiệp vụ yêu cầu tài sản trước khi nộp, `asset_id` phải khác `NULL`.

Khi nộp hồ sơ:

1. Backend đọc state hiện tại của hồ sơ.
2. Backend kiểm tra transition `APP_DRAFT -> APP_SUBMITTED` với `action_code = SUBMIT`.
3. Backend cập nhật `loan_application.current_state_id = APP_SUBMITTED`.
4. Backend insert một record vào `loan_application_state_history`.

---

## 7. Luồng yêu cầu bổ sung

Khi hồ sơ cần bổ sung thông tin:

1. Backend kiểm tra transition hiện tại có cho phép `REQUEST_SUPPLEMENT` hay không.
2. Backend cập nhật `current_state_id = APP_NEEDS_SUPPLEMENT`.
3. Backend ghi lịch sử vào `loan_application_state_history`.

State `APP_NEEDS_SUPPLEMENT` cho phép hồ sơ quay lại `APP_SUBMITTED` bằng action `RESUBMIT`.

---

## 8. Luồng hoàn tất hoặc hủy hồ sơ

Các state kết thúc hiện tại:

| State | Ý nghĩa |
|---|---|
| `APP_CONTRACTED` | Hồ sơ đã được tạo hợp đồng. |
| `APP_CANCELLED` | Hồ sơ bị hủy. |

Khi hồ sơ vào state có `is_terminal = TRUE`, hồ sơ được xem là đã kết thúc lifecycle trong phạm vi module hiện tại.

---

## 9. Quan hệ giữa lifecycle hồ sơ vay và asset status

`loan_application` quản lý lifecycle của hồ sơ vay.

`asset.status` quản lý trạng thái tài sản ở mức đơn giản:

| Status | Ý nghĩa |
|---|---|
| `AVAILABLE` | Tài sản sẵn sàng. |
| `PLEDGED` | Tài sản đang cầm cố. |
| `RELEASED` | Tài sản đã giải chấp/giải phóng. |
| `SETTLED` | Tài sản liên quan khoản vay đã tất toán/thanh toán xong. |

Hai loại trạng thái này không thay thế nhau:

- `loan_application.current_state_id` cho biết hồ sơ vay đang ở bước nào.
- `asset.status` cho biết tài sản đang ở tình trạng nghiệp vụ nào.

Ví dụ:

- Khi hồ sơ được nộp và tài sản được dùng làm tài sản đảm bảo, backend có thể cập nhật `asset.status = PLEDGED`.
- Khi hồ sơ bị hủy trước khi cầm cố, asset có thể vẫn là `AVAILABLE`.
- Khi khoản vay tất toán, asset có thể chuyển sang `SETTLED` hoặc `RELEASED` tùy quy ước nghiệp vụ.

---

## 10. Ghi chú về audit

`loan_application_state_history` chỉ audit sự kiện lifecycle của hồ sơ vay.

Bảng này không audit các thay đổi field như:

- `requested_amount`
- `loan_term_months`
- `loan_purpose`
- `asset_id`

Nếu sau này cần truy vết đầy đủ các thay đổi field, nên bổ sung cơ chế audit riêng.
