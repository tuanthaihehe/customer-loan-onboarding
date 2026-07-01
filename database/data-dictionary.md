# Data Dictionary - Customer Loan Onboarding

## 1. Phạm vi thiết kế

Tài liệu này mô tả các bảng dữ liệu hiện tại của module Customer Loan Onboarding.

Phạm vi hiện tại gồm:

- Lưu trữ và tra cứu khách hàng.
- Tạo và quản lý hồ sơ vay.
- Quản lý lifecycle của hồ sơ vay bằng state, transition và history.
- Quản lý danh mục mục đích vay để frontend hiển thị dropdown.
- Quản lý danh mục xe phục vụ chọn dropdown trên giao diện.
- Lưu tài sản là xe ở mức tối giản và gắn tài sản vào hồ sơ vay.
- Lưu giá thị trường theo từng biến thể xe và thời điểm hiệu lực.
- Lưu kết quả định giá tài sản theo dạng snapshot.
- Lưu các yếu tố giảm trừ cố định được áp dụng trong một lần định giá.

Quy ước chung:

- `id` là khóa kỹ thuật dùng để liên kết giữa các bảng.
- Các trường dạng `*_code` là mã nghiệp vụ dùng cho hiển thị, tra cứu và đối soát.
- Không dùng `*_code` làm foreign key.
- Các bảng danh mục dùng `is_active` để ẩn/hiện dữ liệu trên dropdown mà không cần xóa dữ liệu.
- Các giá trị snapshot trong bảng định giá không bị thay đổi khi bảng danh mục hoặc giá thị trường thay đổi sau này.

---

## 2. Bảng `customer`

### Mục đích

Lưu thông tin cơ bản của khách hàng phục vụ tra cứu và tạo hồ sơ vay. Bảng này không quản lý lifecycle chi tiết của khách hàng.

### Cấu trúc trường

| Column | Type | Required | Constraint | Ý nghĩa |
|---|---|---:|---|---|
| `id` | `uuid` | Yes | Primary key | Khóa kỹ thuật của khách hàng. |
| `customer_code` | `varchar(50)` | Yes | Unique | Mã nghiệp vụ của khách hàng. Dùng để hiển thị và tra cứu. |
| `full_name` | `varchar(255)` | Yes | Index | Họ tên khách hàng. |
| `phone_number` | `varchar(20)` | No | Unique | Số điện thoại khách hàng. |
| `identity_number` | `varchar(20)` | No | Unique | Số CCCD/CMND hoặc giấy tờ định danh tương đương. |
| `date_of_birth` | `date` | No | Index | Ngày sinh của khách hàng. |
| `status` | `varchar(30)` | Yes | Check enum | Trạng thái khách hàng ở mức đơn giản. |

### Giá trị hợp lệ của `customer.status`

| Value | Ý nghĩa |
|---|---|
| `ACTIVE` | Khách hàng đang hoạt động. |
| `INACTIVE` | Khách hàng không còn hoạt động hoặc không tiếp tục phục vụ. |
| `BLACKLIST` | Khách hàng thuộc danh sách blacklist hoặc không đủ điều kiện phục vụ. |

---

## 3. Bảng `loan_purpose`

### Mục đích

Lưu danh mục mục đích vay để frontend hiển thị cho nhân viên chọn khi tạo hoặc cập nhật hồ sơ vay.

Bảng này thay thế cột `loan_application.loan_purpose` dạng text/check enum trước đó. `loan_application` sẽ tham chiếu đến bảng này bằng `loan_purpose_id`.

### Cấu trúc trường

| Column | Type | Required | Constraint | Ý nghĩa |
|---|---|---:|---|---|
| `id` | `uuid` | Yes | Primary key | Khóa kỹ thuật của mục đích vay. |
| `code` | `varchar(50)` | Yes | Unique | Mã mục đích vay dùng cho xử lý nghiệp vụ. |
| `name` | `varchar(100)` | Yes |  | Tên hiển thị trên giao diện. |
| `description` | `text` | No |  | Mô tả mục đích vay. |
| `is_active` | `boolean` | Yes | Default `true` | Có hiển thị cho frontend chọn hay không. |
| `sort_order` | `int` | Yes | Default `0` | Thứ tự hiển thị. |

### Dữ liệu seed hiện tại

| Code | Name | Ý nghĩa |
|---|---|---|
| `BUSINESS` | Kinh doanh | Vay phục vụ hoạt động kinh doanh. |
| `PERSONAL_CONSUMPTION` | Tiêu dùng cá nhân | Vay phục vụ nhu cầu tiêu dùng cá nhân. |
| `VEHICLE_REPAIR` | Sửa chữa xe | Vay phục vụ sửa chữa, bảo dưỡng phương tiện. |
| `MEDICAL` | Y tế | Vay phục vụ chi phí khám chữa bệnh hoặc nhu cầu y tế. |
| `EDUCATION` | Giáo dục | Vay phục vụ chi phí học tập hoặc giáo dục. |
| `HOME_REPAIR` | Sửa chữa nhà | Vay phục vụ sửa chữa hoặc cải tạo nhà cửa. |
| `DEBT_REPAYMENT` | Thanh toán nợ | Vay để thanh toán khoản nợ khác. |
| `OTHER` | Khác | Mục đích vay khác. |

---

## 4. Bảng `loan_application`

### Mục đích

Lưu hồ sơ vay chính của module. Một hồ sơ vay luôn thuộc về một khách hàng. Tài sản và mục đích vay có thể được gắn sau khi hồ sơ vay nháp đã được tạo.

### Cấu trúc trường

| Column | Type | Required | Constraint | Ý nghĩa |
|---|---|---:|---|---|
| `id` | `uuid` | Yes | Primary key | Khóa kỹ thuật của hồ sơ vay. |
| `loan_application_code` | `varchar(50)` | Yes | Unique | Mã nghiệp vụ của hồ sơ vay. Format đề xuất: `APP-YYYY-NNNNNN`. |
| `customer_id` | `uuid` | Yes | FK → `customer.id` | Khách hàng đứng tên hồ sơ vay. |
| `current_state_id` | `uuid` | Yes | FK → `loan_application_state.id` | State hiện tại của hồ sơ vay. |
| `asset_id` | `uuid` | No | FK → `asset.id` | Tài sản được gắn với hồ sơ vay. Nullable vì hồ sơ nháp có thể tạo trước khi chọn tài sản. |
| `loan_purpose_id` | `uuid` | No | FK → `loan_purpose.id` | Mục đích vay. Nullable vì hồ sơ nháp có thể tạo trước khi chọn mục đích vay. |
| `requested_amount` | `numeric(18,2)` | No | Check > 0 nếu có giá trị | Số tiền khách hàng muốn vay. Nullable ở giai đoạn nháp. |
| `loan_term_months` | `int` | No | Check > 0 nếu có giá trị | Kỳ hạn vay theo tháng. Nullable ở giai đoạn nháp. |

### Ghi chú thiết kế

- `asset_id` không bắt buộc khi tạo hồ sơ nháp.
- `loan_purpose_id` không bắt buộc khi tạo hồ sơ nháp.
- Mục đích vay được quản lý bằng bảng `loan_purpose`, không còn lưu trực tiếp bằng text/check enum trong `loan_application`.
- Bảng `loan_application` chỉ giữ state hiện tại qua `current_state_id`.
- Lịch sử chuyển state được lưu trong `loan_application_state_history`.

---

## 5. Bảng `loan_application_state`

### Mục đích

Lưu danh sách state hợp lệ của hồ sơ vay.

### Cấu trúc trường

| Column | Type | Required | Constraint | Ý nghĩa |
|---|---|---:|---|---|
| `id` | `uuid` | Yes | Primary key | Khóa kỹ thuật của state. |
| `code` | `varchar(50)` | Yes | Unique | Mã state dùng trong hệ thống. |
| `name` | `varchar(100)` | Yes |  | Tên hiển thị của state. |
| `description` | `text` | No |  | Mô tả nghiệp vụ của state. |
| `is_initial` | `boolean` | Yes | Unique partial index khi true | Đánh dấu state khởi tạo. |
| `is_terminal` | `boolean` | Yes |  | Đánh dấu state kết thúc lifecycle. |
| `sort_order` | `int` | Yes |  | Thứ tự hiển thị. |

### State hiện tại

| Code | Ý nghĩa | Initial | Terminal |
|---|---|---:|---:|
| `APP_DRAFT` | Hồ sơ nháp | Yes | No |
| `APP_SUBMITTED` | Đã nộp hồ sơ | No | No |
| `APP_NEEDS_SUPPLEMENT` | Cần bổ sung hồ sơ | No | No |
| `APP_IN_REVIEW` | Đang thẩm định/phê duyệt | No | No |
| `APP_READY_FOR_CONTRACT` | Sẵn sàng lập hợp đồng | No | No |
| `APP_CONTRACTED` | Đã có hợp đồng | No | Yes |
| `APP_CANCELLED` | Hồ sơ bị hủy | No | Yes |

---

## 6. Bảng `loan_application_state_transition`

### Mục đích

Định nghĩa các đường chuyển state hợp lệ của hồ sơ vay.

Bảng này là dữ liệu cấu hình. Khi một hành động chuyển state được yêu cầu, backend kiểm tra trong bảng này để biết chuyển đổi đó có hợp lệ hay không.

### Cấu trúc trường

| Column | Type | Required | Constraint | Ý nghĩa |
|---|---|---:|---|---|
| `id` | `uuid` | Yes | Primary key | Khóa kỹ thuật của transition. |
| `from_state_id` | `uuid` | Yes | FK → `loan_application_state.id` | State nguồn. |
| `to_state_id` | `uuid` | Yes | FK → `loan_application_state.id` | State đích. |
| `action_code` | `varchar(50)` | Yes | Unique cùng `from_state_id`, `to_state_id` | Mã hành động chuyển state. |
| `action_name` | `varchar(100)` | Yes |  | Tên hành động hiển thị. |
| `description` | `text` | No |  | Mô tả hành động. |

---

## 7. Bảng `loan_application_state_history`

### Mục đích

Lưu lịch sử lifecycle của từng hồ sơ vay.

Bảng này ghi lại hồ sơ đã chuyển từ state nào sang state nào, vào lúc nào, bởi ai và ghi chú gì. Đây là bảng audit cho lifecycle, không phải audit toàn bộ thay đổi dữ liệu field.

### Cấu trúc trường

| Column | Type | Required | Constraint | Ý nghĩa |
|---|---|---:|---|---|
| `id` | `uuid` | Yes | Primary key | Khóa kỹ thuật của history record. |
| `loan_application_id` | `uuid` | Yes | FK → `loan_application.id` | Hồ sơ vay được ghi lịch sử. |
| `from_state_id` | `uuid` | No | FK → `loan_application_state.id` | State trước khi chuyển. `NULL` với sự kiện tạo mới. |
| `to_state_id` | `uuid` | Yes | FK → `loan_application_state.id` | State sau khi chuyển. |
| `action_code` | `varchar(50)` | Yes |  | Hành động gây ra chuyển state. |
| `changed_at` | `timestamp` | Yes | Default `CURRENT_TIMESTAMP` | Thời điểm ghi nhận sự kiện lifecycle. |
| `changed_by` | `varchar(100)` | No |  | Người hoặc hệ thống thực hiện hành động. |
| `note` | `text` | No |  | Ghi chú nghiệp vụ. |

---

## 8. Bảng `vehicle_type`

### Mục đích

Lưu loại xe phục vụ dropdown đầu tiên trên giao diện chọn tài sản.

Ví dụ: xe máy, ô tô.

| Column | Type | Required | Constraint | Ý nghĩa |
|---|---|---:|---|---|
| `id` | `uuid` | Yes | Primary key | Khóa kỹ thuật của loại xe. |
| `code` | `varchar(50)` | Yes | Unique | Mã loại xe. |
| `name` | `varchar(100)` | Yes |  | Tên hiển thị. |
| `description` | `text` | No |  | Mô tả nếu cần. |
| `is_active` | `boolean` | Yes | Default true | Có hiển thị trên dropdown hay không. |
| `sort_order` | `int` | Yes | Default 0 | Thứ tự hiển thị. |

---

## 9. Bảng `vehicle_brand`

### Mục đích

Lưu hãng xe theo từng loại xe.

Quan hệ: `vehicle_type 1 - N vehicle_brand`.

| Column | Type | Required | Constraint | Ý nghĩa |
|---|---|---:|---|---|
| `id` | `uuid` | Yes | Primary key | Khóa kỹ thuật của hãng xe. |
| `vehicle_type_id` | `uuid` | Yes | FK → `vehicle_type.id` | Loại xe chứa hãng này. |
| `code` | `varchar(50)` | Yes | Unique theo `vehicle_type_id` | Mã hãng xe. |
| `name` | `varchar(100)` | Yes |  | Tên hãng xe. |
| `is_active` | `boolean` | Yes | Default true | Có hiển thị hay không. |
| `sort_order` | `int` | Yes | Default 0 | Thứ tự hiển thị. |

---

## 10. Bảng `vehicle_model`

### Mục đích

Lưu dòng xe/model theo từng hãng xe.

Quan hệ: `vehicle_brand 1 - N vehicle_model`.

| Column | Type | Required | Constraint | Ý nghĩa |
|---|---|---:|---|---|
| `id` | `uuid` | Yes | Primary key | Khóa kỹ thuật của dòng xe. |
| `vehicle_brand_id` | `uuid` | Yes | FK → `vehicle_brand.id` | Hãng xe chứa dòng xe này. |
| `code` | `varchar(50)` | Yes | Unique theo `vehicle_brand_id` | Mã dòng xe. |
| `name` | `varchar(100)` | Yes |  | Tên dòng xe. |
| `is_active` | `boolean` | Yes | Default true | Có hiển thị hay không. |
| `sort_order` | `int` | Yes | Default 0 | Thứ tự hiển thị. |

---

## 11. Bảng `vehicle_version`

### Mục đích

Lưu phiên bản xe theo từng dòng xe.

Quan hệ: `vehicle_model 1 - N vehicle_version`.

| Column | Type | Required | Constraint | Ý nghĩa |
|---|---|---:|---|---|
| `id` | `uuid` | Yes | Primary key | Khóa kỹ thuật của phiên bản xe. |
| `vehicle_model_id` | `uuid` | Yes | FK → `vehicle_model.id` | Dòng xe chứa phiên bản này. |
| `code` | `varchar(50)` | Yes | Unique theo `vehicle_model_id` | Mã phiên bản. |
| `name` | `varchar(100)` | Yes |  | Tên phiên bản. |
| `is_active` | `boolean` | Yes | Default true | Có hiển thị hay không. |
| `sort_order` | `int` | Yes | Default 0 | Thứ tự hiển thị. |

---

## 12. Bảng `vehicle_year`

### Mục đích

Lưu năm sản xuất hợp lệ theo từng phiên bản xe.

Quan hệ: `vehicle_version 1 - N vehicle_year`.

| Column | Type | Required | Constraint | Ý nghĩa |
|---|---|---:|---|---|
| `id` | `uuid` | Yes | Primary key | Khóa kỹ thuật của năm sản xuất. |
| `vehicle_version_id` | `uuid` | Yes | FK → `vehicle_version.id` | Phiên bản xe tương ứng. |
| `manufacture_year` | `int` | Yes | Check 1980-2100 | Năm sản xuất. |
| `is_active` | `boolean` | Yes | Default true | Có hiển thị hay không. |
| `sort_order` | `int` | Yes | Default 0 | Thứ tự hiển thị. |

---

## 13. Bảng `vehicle_color`

### Mục đích

Lưu danh mục màu xe.

| Column | Type | Required | Constraint | Ý nghĩa |
|---|---|---:|---|---|
| `id` | `uuid` | Yes | Primary key | Khóa kỹ thuật của màu xe. |
| `code` | `varchar(50)` | Yes | Unique | Mã màu. |
| `name` | `varchar(100)` | Yes |  | Tên màu hiển thị. |
| `is_active` | `boolean` | Yes | Default true | Có hiển thị hay không. |
| `sort_order` | `int` | Yes | Default 0 | Thứ tự hiển thị. |

---

## 14. Bảng `vehicle_variant`

### Mục đích

Đại diện cho tổ hợp xe có thể định giá, được tạo từ `vehicle_year` và `vehicle_color`.

Ví dụ: Yamaha Exciter 155 ABS 2023 màu xanh.

Quan hệ:

- `vehicle_year 1 - N vehicle_variant`
- `vehicle_color 1 - N vehicle_variant`

| Column | Type | Required | Constraint | Ý nghĩa |
|---|---|---:|---|---|
| `id` | `uuid` | Yes | Primary key | Khóa kỹ thuật của biến thể xe. |
| `vehicle_year_id` | `uuid` | Yes | FK → `vehicle_year.id` | Phiên bản + năm sản xuất. |
| `vehicle_color_id` | `uuid` | Yes | FK → `vehicle_color.id` | Màu xe. |
| `code` | `varchar(100)` | Yes | Unique | Mã biến thể xe. |
| `name` | `varchar(255)` | Yes |  | Tên biến thể hiển thị. |
| `is_active` | `boolean` | Yes | Default true | Có cho phép chọn hay không. |
| `sort_order` | `int` | Yes | Default 0 | Thứ tự hiển thị. |

---

## 15. Bảng `vehicle_market_price`

### Mục đích

Lưu giá thị trường theo từng biến thể xe và thời điểm hiệu lực.

Màu xe có ảnh hưởng đến giá, vì vậy giá được gắn với `vehicle_variant` thay vì chỉ gắn với model hoặc năm sản xuất.

| Column | Type | Required | Constraint | Ý nghĩa |
|---|---|---:|---|---|
| `id` | `uuid` | Yes | Primary key | Khóa kỹ thuật của giá thị trường. |
| `vehicle_variant_id` | `uuid` | Yes | FK → `vehicle_variant.id` | Biến thể xe được định giá. |
| `price_amount` | `numeric(18,2)` | Yes | Check > 0 | Giá thị trường. |
| `currency_code` | `varchar(3)` | Yes | Default `VND` | Đơn vị tiền tệ. |
| `price_source` | `varchar(100)` | No |  | Nguồn giá: import, pricing service, manual... |
| `effective_from` | `date` | Yes |  | Ngày bắt đầu hiệu lực của giá. |
| `effective_to` | `date` | No | Check >= `effective_from` nếu có | Ngày kết thúc hiệu lực. `NULL` nghĩa là còn hiệu lực. |
| `note` | `text` | No |  | Ghi chú. |

---

## 16. Bảng `asset`

### Mục đích

Lưu tài sản là xe ở mức tối giản. Tài sản được gắn vào hồ sơ vay qua `loan_application.asset_id`.

Bảng `asset` không chứa `customer_id`. Khách hàng của tài sản được xác định qua hồ sơ vay:

`asset -> loan_application -> customer`.

### Cấu trúc trường

| Column | Type | Required | Constraint | Ý nghĩa |
|---|---|---:|---|---|
| `id` | `uuid` | Yes | Primary key | Khóa kỹ thuật của tài sản. |
| `asset_code` | `varchar(50)` | Yes | Unique | Mã nghiệp vụ của tài sản. Format đề xuất: `AST-YYYY-NNNNNN`. |
| `vehicle_variant_id` | `uuid` | Yes | FK → `vehicle_variant.id` | Biến thể xe của tài sản. |
| `license_plate` | `varchar(20)` | No | Unique | Biển số xe. |
| `status` | `varchar(30)` | Yes | Check enum | Trạng thái tài sản trong nghiệp vụ cầm cố. |

### Giá trị hợp lệ của `asset.status`

| Value | Ý nghĩa |
|---|---|
| `AVAILABLE` | Tài sản sẵn sàng, chưa bị gắn vào khoản cầm cố đang xử lý. |
| `PLEDGED` | Tài sản đang được cầm cố. |
| `RELEASED` | Tài sản đã được giải chấp/giải phóng. |
| `SETTLED` | Tài sản liên quan khoản vay đã tất toán/thanh toán xong. |

---

## 17. Bảng `asset_deduction_type`

### Mục đích

Lưu danh mục các yếu tố giảm trừ dùng trong định giá tài sản.

Mỗi loại giảm trừ có một số tiền giảm trừ cố định. Khi định giá, hệ thống sẽ copy số tiền này sang `asset_valuation_deduction.deduction_amount_snapshot` để giữ snapshot.

### Cấu trúc trường

| Column | Type | Required | Constraint | Ý nghĩa |
|---|---|---:|---|---|
| `id` | `uuid` | Yes | Primary key | Khóa kỹ thuật của loại giảm trừ. |
| `code` | `varchar(50)` | Yes | Unique | Mã loại giảm trừ dùng trong hệ thống. |
| `name` | `varchar(100)` | Yes |  | Tên hiển thị của loại giảm trừ. |
| `description` | `text` | No |  | Mô tả loại giảm trừ. |
| `deduction_amount` | `numeric(18,2)` | Yes | Check >= 0 | Số tiền giảm trừ cố định của loại này. |
| `is_active` | `boolean` | Yes | Default `true` | Có hiển thị cho nhân viên chọn hay không. |
| `sort_order` | `int` | Yes | Default `0` | Thứ tự hiển thị. |

### Dữ liệu seed hiện tại

| Code | Ý nghĩa |
|---|---|
| `OLD_VEHICLE` | Xe cũ hoặc đời xe thấp hơn mặt bằng định giá. |
| `PHYSICAL_DAMAGE` | Hư hỏng ngoại thất, trầy xước, móp méo, va chạm. |
| `ENGINE_ISSUE` | Lỗi động cơ hoặc dấu hiệu đã sửa chữa lớn. |
| `MISSING_DOCUMENT` | Thiếu giấy tờ liên quan. |
| `HIGH_MILEAGE` | Số km sử dụng cao. |
| `LOW_LIQUIDITY_COLOR` | Màu xe khó thanh khoản. |
| `MODIFIED_VEHICLE` | Xe đã độ hoặc chỉnh sửa. |
| `OTHER` | Yếu tố giảm trừ khác. |

---

## 18. Bảng `asset_valuation`

### Mục đích

Lưu kết quả tổng của một lần định giá tài sản.

Bảng này là snapshot. Nếu giá thị trường hoặc giá trị giảm trừ thay đổi sau này, record định giá cũ vẫn giữ nguyên.

### Cấu trúc trường

| Column | Type | Required | Constraint | Ý nghĩa |
|---|---|---:|---|---|
| `id` | `uuid` | Yes | Primary key | Khóa kỹ thuật của lần định giá. |
| `asset_id` | `uuid` | Yes | FK → `asset.id` | Tài sản được định giá. |
| `market_price_amount` | `numeric(18,2)` | Yes | Check > 0 | Giá thị trường tại thời điểm định giá. |
| `total_deduction_amount` | `numeric(18,2)` | Yes | Check >= 0 | Tổng số tiền giảm trừ đã áp dụng. |
| `final_value_amount` | `numeric(18,2)` | Yes | Check >= 0 | Giá trị cuối cùng sau giảm trừ. |
| `currency_code` | `varchar(3)` | Yes | Default `VND` | Đơn vị tiền tệ. |
| `valuation_source` | `varchar(100)` | No |  | Nguồn định giá, ví dụ `MANUAL` hoặc `PRICING_SERVICE`. |
| `valued_at` | `timestamp` | Yes | Default `CURRENT_TIMESTAMP` | Thời điểm định giá. |
| `valued_by` | `varchar(100)` | No |  | Nhân viên hoặc hệ thống thực hiện định giá. |
| `note` | `text` | No |  | Ghi chú định giá. |

### Ghi chú tính toán

```text
final_value_amount = market_price_amount - total_deduction_amount
```

Backend tính đủ trước khi insert `asset_valuation`, nên không cần tạo record tạm rồi update lại.

---

## 19. Bảng `asset_valuation_deduction`

### Mục đích

Lưu các yếu tố giảm trừ đã được áp dụng trong một lần định giá.

Mỗi record tương ứng với một loại giảm trừ được chọn trong lần định giá đó.

### Cấu trúc trường

| Column | Type | Required | Constraint | Ý nghĩa |
|---|---|---:|---|---|
| `id` | `uuid` | Yes | Primary key | Khóa kỹ thuật của dòng giảm trừ. |
| `asset_valuation_id` | `uuid` | Yes | FK → `asset_valuation.id` | Lần định giá áp dụng giảm trừ. |
| `deduction_type_id` | `uuid` | Yes | FK → `asset_deduction_type.id` | Loại giảm trừ được chọn. |
| `deduction_amount_snapshot` | `numeric(18,2)` | Yes | Check >= 0 | Số tiền giảm trừ được copy từ `asset_deduction_type` tại thời điểm định giá. |
| `note` | `text` | No |  | Ghi chú riêng cho dòng giảm trừ nếu cần. |
| `created_at` | `timestamp` | Yes | Default `CURRENT_TIMESTAMP` | Thời điểm tạo dòng giảm trừ. |

### Ghi chú thiết kế

- Không dùng `deduction_method`, `deduction_value`, `deduction_amount` theo kiểu linh hoạt nữa.
- Hiện tại giảm trừ là số tiền cố định theo từng `asset_deduction_type`.
- Unique theo `(asset_valuation_id, deduction_type_id)` để một lần định giá không chọn trùng một loại giảm trừ.

---

## 20. Luồng dropdown chọn xe

Giao diện chọn xe nên dùng thứ tự sau:

1. Chọn `vehicle_type`.
2. Từ `vehicle_type_id`, lấy danh sách `vehicle_brand`.
3. Từ `vehicle_brand_id`, lấy danh sách `vehicle_model`.
4. Từ `vehicle_model_id`, lấy danh sách `vehicle_version`.
5. Từ `vehicle_version_id`, lấy danh sách `vehicle_year`.
6. Từ `vehicle_year_id`, lấy danh sách màu hợp lệ thông qua `vehicle_variant`.
7. Từ `vehicle_year_id` + `vehicle_color_id`, xác định `vehicle_variant`.
8. Từ `vehicle_variant_id`, lấy giá thị trường hiện hành trong `vehicle_market_price`.

---

## 21. Luồng chọn mục đích vay

Frontend lấy danh sách mục đích vay từ bảng `loan_purpose`:

```sql
SELECT
    id,
    code,
    name,
    description
FROM loan_purpose
WHERE is_active = TRUE
ORDER BY sort_order, name;
```

Khi tạo hoặc cập nhật hồ sơ vay, frontend gửi `loan_purpose_id`. Backend lưu vào `loan_application.loan_purpose_id`.

---

## 22. Luồng định giá tài sản và chọn giảm trừ

### Preview

Khi nhân viên chọn các yếu tố giảm trừ trên frontend, backend có thể tính preview bằng cách đọc:

- `asset`
- `vehicle_market_price`
- `asset_deduction_type`

Preview không tạo record trong database.

### Save

Khi nhân viên bấm lưu định giá, backend chạy trong một transaction:

1. Đọc `asset` để biết tài sản đang định giá.
2. Đọc `vehicle_market_price` để lấy giá thị trường hiện hành.
3. Đọc `asset_deduction_type` để lấy số tiền của các giảm trừ đã chọn.
4. Tính `total_deduction_amount`.
5. Tính `final_value_amount`.
6. Insert 1 record vào `asset_valuation`.
7. Insert N record vào `asset_valuation_deduction`, tương ứng N yếu tố giảm trừ được chọn.
8. Commit transaction.

Không cần update `asset_valuation` lần hai vì backend đã tính đủ trước khi insert.

---

## 23. Ghi chú về audit

`loan_application_state_history` audit lifecycle event của hồ sơ vay. Bảng này không audit mọi thay đổi field như `requested_amount`, `loan_purpose_id`, `loan_term_months` hoặc `asset_id`.

Nếu sau này cần audit chi tiết mọi thay đổi dữ liệu, cần thiết kế thêm bảng audit riêng hoặc cơ chế event log.
