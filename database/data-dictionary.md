# Data Dictionary - Customer Loan Onboarding

Tài liệu này mô tả các bảng dữ liệu vật lý hiện tại của module Customer Loan Onboarding. Thiết kế hiện tại tập trung vào việc lưu thông tin cơ bản để tạo và quản lý hồ sơ vay. Hệ thống chưa tách riêng các bảng định giá tài sản, kiểm tra điều kiện vay hoặc phê duyệt độc lập. Các phần đó có thể được bổ sung ở phase sau nếu nghiệp vụ yêu cầu lưu lịch sử/kết quả riêng.

## Nguyên tắc thiết kế

- `id` là khóa kỹ thuật dùng để tham chiếu giữa các bảng.
- Các trường dạng `*_code` là mã nghiệp vụ dùng để hiển thị, tra cứu hoặc trao đổi với backend/frontend. Các trường này không dùng làm khóa ngoại.
- `customer` và `asset` chỉ dùng `status` đơn giản dạng enum/check constraint.
- Chỉ `loan_application` có lifecycle đầy đủ thông qua 3 bảng đi kèm:
  - `loan_application_state`
  - `loan_application_state_transition`
  - `loan_application_state_history`
- `loan_application.asset_id` cho phép `NULL` để phù hợp với flow tạo hồ sơ nháp trước, gắn tài sản sau.

---

# 1. customer

## Mục đích

Bảng `customer` lưu thông tin cơ bản của khách hàng phục vụ tra cứu và tạo hồ sơ vay. Module hiện tại không quản lý vòng đời khách hàng, vì vậy trạng thái khách hàng được lưu trực tiếp bằng trường `status`.

## Field dictionary

| Column | Type | Required | Constraint | Ý nghĩa | Ví dụ |
|---|---:|:---:|---|---|---|
| `id` | `uuid` | Yes | PK | Khóa kỹ thuật của khách hàng, dùng để FK từ bảng khác. | `10000000-0000-0000-0000-000000000001` |
| `customer_code` | `varchar(50)` | Yes | Unique | Mã nghiệp vụ của khách hàng, dùng để tra cứu/hiển thị. Không dùng làm FK. | `CUS-2026-000001` |
| `full_name` | `varchar(255)` | Yes | - | Họ tên khách hàng. | `Nguyễn Văn An` |
| `phone_number` | `varchar(20)` | Yes | Unique | Số điện thoại chính của khách hàng. | `0901000001` |
| `identity_number` | `varchar(20)` | Yes | Unique | Số CCCD/CMND hoặc giấy tờ định danh tương đương. | `079095000001` |
| `date_of_birth` | `date` | No | - | Ngày sinh của khách hàng. | `1995-01-15` |
| `status` | `varchar(30)` | Yes | Check | Trạng thái đơn giản của khách hàng. | `ACTIVE` |
| `created_at` | `timestamp` | Yes | Default current timestamp | Thời điểm tạo bản ghi. | `2026-06-01 09:00:00` |
| `updated_at` | `timestamp` | Yes | Default current timestamp | Thời điểm cập nhật gần nhất. | `2026-06-01 09:00:00` |
| `deleted_at` | `timestamp` | No | - | Thời điểm xóa mềm. `NULL` nghĩa là bản ghi còn hiệu lực. | `NULL` |

## Giá trị của `customer.status`

| Value | Ý nghĩa |
|---|---|
| `ACTIVE` | Khách hàng đang hoạt động, có thể tạo hồ sơ vay. |
| `INACTIVE` | Khách hàng không còn hoạt động hoặc không còn được ưu tiên xử lý. |
| `RESTRICTED` | Khách hàng bị hạn chế, ví dụ nằm trong blacklist hoặc cần kiểm tra thêm. |

## Index / constraint chính

| Constraint / Index | Ý nghĩa |
|---|---|
| PK `id` | Định danh kỹ thuật duy nhất của khách hàng. |
| Unique `customer_code` | Mã nghiệp vụ khách hàng không trùng. |
| Unique `phone_number` | Không cho phép hai khách hàng dùng cùng một số điện thoại chính. |
| Unique `identity_number` | Không cho phép hai khách hàng dùng cùng một số định danh. |
| Check `status IN ('ACTIVE', 'INACTIVE', 'RESTRICTED')` | Giới hạn giá trị trạng thái khách hàng. |
| GIN trigram index trên `full_name` | Hỗ trợ tìm kiếm gần đúng theo họ tên. |
| Index `date_of_birth` | Hỗ trợ lọc theo ngày sinh. |

---

# 2. asset

## Mục đích

Bảng `asset` lưu thông tin cơ bản của tài sản là phương tiện/xe thuộc về khách hàng. Module hiện tại không quản lý vòng đời tài sản đầy đủ, vì vậy trạng thái tài sản được lưu trực tiếp bằng trường `status`.

## Field dictionary

| Column | Type | Required | Constraint | Ý nghĩa | Ví dụ |
|---|---:|:---:|---|---|---|
| `id` | `uuid` | Yes | PK | Khóa kỹ thuật của tài sản, dùng để FK từ hồ sơ vay. | `20000000-0000-0000-0000-000000000001` |
| `asset_code` | `varchar(50)` | Yes | Unique | Mã nghiệp vụ của tài sản, dùng để tra cứu/hiển thị. Không dùng làm FK. | `AST-2026-000001` |
| `customer_id` | `uuid` | Yes | FK to `customer.id` | Khách hàng sở hữu tài sản. | `10000000-0000-0000-0000-000000000001` |
| `license_plate` | `varchar(20)` | Yes | Unique | Biển số xe. | `30A-12345` |
| `vehicle_brand` | `varchar(100)` | Yes | - | Hãng xe. | `Toyota` |
| `vehicle_model` | `varchar(100)` | Yes | - | Dòng xe/model. | `Vios` |
| `vehicle_version` | `varchar(100)` | No | - | Phiên bản xe. | `1.5G CVT` |
| `manufacture_year` | `integer` | No | Check | Năm sản xuất. | `2020` |
| `status` | `varchar(30)` | Yes | Check | Trạng thái đơn giản của tài sản. | `AVAILABLE` |
| `created_at` | `timestamp` | Yes | Default current timestamp | Thời điểm tạo bản ghi. | `2026-06-01 09:10:00` |
| `updated_at` | `timestamp` | Yes | Default current timestamp | Thời điểm cập nhật gần nhất. | `2026-06-01 09:10:00` |
| `deleted_at` | `timestamp` | No | - | Thời điểm xóa mềm. `NULL` nghĩa là bản ghi còn hiệu lực. | `NULL` |

## Giá trị của `asset.status`

| Value | Ý nghĩa |
|---|---|
| `AVAILABLE` | Tài sản sẵn sàng, chưa bị gắn vào hồ sơ vay đang mở. |
| `PLEDGED` | Tài sản đang được cầm cố/gắn với hồ sơ vay đang xử lý. |
| `RELEASED` | Tài sản đã giải chấp hoặc từng được dùng nhưng hiện không còn cầm cố. |

## Index / constraint chính

| Constraint / Index | Ý nghĩa |
|---|---|
| PK `id` | Định danh kỹ thuật duy nhất của tài sản. |
| Unique `asset_code` | Mã nghiệp vụ tài sản không trùng. |
| Unique `license_plate` | Không cho phép trùng biển số xe. |
| FK `customer_id -> customer.id` | Một tài sản thuộc về một khách hàng. |
| Check `status IN ('AVAILABLE', 'PLEDGED', 'RELEASED')` | Giới hạn giá trị trạng thái tài sản. |
| Check `manufacture_year BETWEEN 1980 AND 2100` | Kiểm soát giá trị năm sản xuất ở mức cơ bản. |
| Index `customer_id` | Hỗ trợ lấy danh sách tài sản theo khách hàng. |
| Index `status` | Hỗ trợ lọc tài sản theo trạng thái. |

---

# 3. loan_application

## Mục đích

Bảng `loan_application` là bảng trung tâm của module. Bảng này lưu hồ sơ vay, liên kết với khách hàng, có thể liên kết với một tài sản và lưu trạng thái hiện tại của hồ sơ.

Lifecycle của hồ sơ vay không lưu bằng một trường enum đơn giản, mà được quản lý bằng `current_state_id` trỏ tới bảng `loan_application_state`. Lịch sử chuyển trạng thái được lưu trong `loan_application_state_history`.

## Field dictionary

| Column | Type | Required | Constraint | Ý nghĩa | Ví dụ |
|---|---:|:---:|---|---|---|
| `id` | `uuid` | Yes | PK | Khóa kỹ thuật của hồ sơ vay. | `30000000-0000-0000-0000-000000000001` |
| `loan_application_code` | `varchar(50)` | Yes | Unique | Mã nghiệp vụ của hồ sơ vay, dùng để tra cứu/hiển thị. | `APP-2026-000001` |
| `customer_id` | `uuid` | Yes | FK to `customer.id` | Khách hàng đứng tên hồ sơ vay. | `10000000-0000-0000-0000-000000000001` |
| `asset_id` | `uuid` | No | FK to `asset.id` | Tài sản được gắn với hồ sơ vay. Cho phép `NULL` khi hồ sơ vẫn là nháp và chưa chọn tài sản. | `20000000-0000-0000-0000-000000000001` |
| `current_state_id` | `uuid` | Yes | FK to `loan_application_state.id` | Trạng thái hiện tại của hồ sơ vay. | `APP_DRAFT` thông qua FK |
| `requested_amount` | `numeric(18,2)` | Yes | Check `> 0` | Số tiền khách hàng đề nghị vay. | `50000000.00` |
| `loan_purpose` | `text` | No | - | Mục đích vay hoặc ghi chú nghiệp vụ. | `Vay cầm cố xe ô tô phục vụ nhu cầu cá nhân.` |
| `submitted_at` | `timestamp` | No | - | Thời điểm hồ sơ được nộp vào luồng xử lý. | `2026-06-10 10:30:00` |
| `closed_at` | `timestamp` | No | - | Thời điểm hồ sơ kết thúc lifecycle. Chỉ set khi state là terminal. | `2026-06-13 16:30:00` |
| `created_at` | `timestamp` | Yes | Default current timestamp | Thời điểm tạo hồ sơ. | `2026-06-10 10:00:00` |
| `updated_at` | `timestamp` | Yes | Default current timestamp | Thời điểm cập nhật gần nhất. | `2026-06-10 11:00:00` |
| `deleted_at` | `timestamp` | No | - | Thời điểm xóa mềm. `NULL` nghĩa là bản ghi còn hiệu lực. | `NULL` |

## Điểm thiết kế quan trọng

### `asset_id` cho phép NULL

Trong flow hiện tại, hồ sơ nháp có thể được tạo trước khi chọn tài sản. Vì vậy `loan_application.asset_id` không được đặt `NOT NULL`.

Flow hợp lệ:

1. Tạo `loan_application` với `asset_id = NULL`, state `APP_DRAFT`.
2. Sau đó nhân viên chọn/gắn tài sản vào hồ sơ.
3. Khi gắn tài sản, backend cập nhật `loan_application.asset_id`.

### Một tài sản không được dùng bởi nhiều hồ sơ đang mở

Constraint nghiệp vụ này được enforce bằng partial unique index:

```sql
CREATE UNIQUE INDEX uq_active_loan_application_asset
ON loan_application(asset_id)
WHERE asset_id IS NOT NULL
  AND closed_at IS NULL
  AND deleted_at IS NULL;
```

Ý nghĩa:

- Một tài sản chỉ được gắn với tối đa một hồ sơ vay đang mở.
- Hồ sơ nháp chưa có tài sản không bị ảnh hưởng.
- Hồ sơ đã đóng (`closed_at IS NOT NULL`) không chặn tài sản được dùng lại nếu nghiệp vụ cho phép.

## Index / constraint chính

| Constraint / Index | Ý nghĩa |
|---|---|
| PK `id` | Định danh kỹ thuật duy nhất của hồ sơ vay. |
| Unique `loan_application_code` | Mã nghiệp vụ hồ sơ không trùng. |
| FK `customer_id -> customer.id` | Một hồ sơ vay thuộc về một khách hàng. |
| FK `asset_id -> asset.id` | Một hồ sơ vay có thể gắn một tài sản. Nullable khi draft. |
| FK `current_state_id -> loan_application_state.id` | Hồ sơ luôn có một state hiện tại. |
| Check `requested_amount > 0` | Số tiền đề nghị vay phải lớn hơn 0. |
| Partial unique index `uq_active_loan_application_asset` | Chặn một tài sản bị gắn vào nhiều hồ sơ đang mở. |

---

# 4. Lifecycle tables for loan_application

## 4.1. loan_application_state

Bảng `loan_application_state` lưu danh sách state hợp lệ của hồ sơ vay.

| Column | Type | Required | Constraint | Ý nghĩa |
|---|---:|:---:|---|---|
| `id` | `uuid` | Yes | PK | Khóa kỹ thuật của state. |
| `code` | `varchar(50)` | Yes | Unique | Mã state dùng trong hệ thống. |
| `name` | `varchar(100)` | Yes | - | Tên hiển thị của state. |
| `description` | `text` | No | - | Mô tả ý nghĩa state. |
| `is_initial` | `boolean` | Yes | Default false | Đánh dấu state bắt đầu. |
| `is_terminal` | `boolean` | Yes | Default false | Đánh dấu state kết thúc. |
| `sort_order` | `integer` | Yes | - | Thứ tự hiển thị. |
| `created_at` | `timestamp` | Yes | Default current timestamp | Thời điểm tạo cấu hình state. |
| `updated_at` | `timestamp` | Yes | Default current timestamp | Thời điểm cập nhật cấu hình state. |

## 4.2. loan_application_state_transition

Bảng `loan_application_state_transition` định nghĩa state nào được phép chuyển sang state nào và action nào thực hiện chuyển trạng thái đó.

| Column | Type | Required | Constraint | Ý nghĩa |
|---|---:|:---:|---|---|
| `id` | `uuid` | Yes | PK | Khóa kỹ thuật của transition. |
| `from_state_id` | `uuid` | Yes | FK | State nguồn. |
| `to_state_id` | `uuid` | Yes | FK | State đích. |
| `action_code` | `varchar(50)` | Yes | Unique with from/to | Mã hành động chuyển state. |
| `action_name` | `varchar(100)` | Yes | - | Tên hành động hiển thị. |
| `description` | `text` | No | - | Mô tả nghiệp vụ của transition. |
| `created_at` | `timestamp` | Yes | Default current timestamp | Thời điểm tạo cấu hình transition. |
| `updated_at` | `timestamp` | Yes | Default current timestamp | Thời điểm cập nhật cấu hình transition. |

## 4.3. loan_application_state_history

Bảng `loan_application_state_history` lưu lịch sử chuyển trạng thái thực tế của từng hồ sơ vay.

| Column | Type | Required | Constraint | Ý nghĩa |
|---|---:|:---:|---|---|
| `id` | `uuid` | Yes | PK | Khóa kỹ thuật của bản ghi lịch sử. |
| `loan_application_id` | `uuid` | Yes | FK | Hồ sơ vay được chuyển trạng thái. |
| `from_state_id` | `uuid` | No | FK | State trước khi chuyển. `NULL` khi tạo hồ sơ ban đầu. |
| `to_state_id` | `uuid` | Yes | FK | State sau khi chuyển. |
| `action_code` | `varchar(50)` | Yes | - | Action đã thực hiện, ví dụ `SUBMIT`. |
| `changed_at` | `timestamp` | Yes | Default current timestamp | Thời điểm chuyển trạng thái. |
| `changed_by` | `varchar(100)` | No | - | Người hoặc hệ thống thực hiện chuyển trạng thái. |
| `note` | `text` | No | - | Ghi chú nghiệp vụ. |

---

# 5. Relationship summary

| Source | Relationship | Target | Ý nghĩa |
|---|---|---|---|
| `customer` | 1 - N | `asset` | Một khách hàng có thể có nhiều tài sản. |
| `customer` | 1 - N | `loan_application` | Một khách hàng có thể có nhiều hồ sơ vay. |
| `asset` | 1 - 0..1 active | `loan_application` | Một tài sản có thể được gắn với tối đa một hồ sơ vay đang mở. |
| `loan_application_state` | 1 - N | `loan_application` | Nhiều hồ sơ có thể cùng ở một state hiện tại. |
| `loan_application` | 1 - N | `loan_application_state_history` | Một hồ sơ có nhiều bản ghi lịch sử chuyển state. |
