# Loan Application Onboarding Redesign

Ngày cập nhật: 2026-07-07

## Kết luận thiết kế

Luồng khởi tạo hồ sơ vay không dùng `Loan Application Draft` nữa.

Từ thời điểm tạo hồ sơ đầu tiên, backend tạo trực tiếp bản ghi trong `loan_application`. Bảng này là nguồn dữ liệu chuẩn cho toàn bộ lifecycle:

```text
APP_CREATED -> APP_IN_PROGRESS -> APP_COMPLETED -> APP_SUBMITTED
```

Các bảng draft cũ đã được đưa vào migration drop để tránh nhiều bảng cùng mô tả một nghiệp vụ.

## Vì sao bỏ Loan Application Draft

Cách tách `loan_application_draft` và `loan_application` làm phát sinh hai nguồn dữ liệu:

- Dữ liệu đang nhập nằm ở draft.
- Trạng thái hồ sơ thật lại nằm ở application.
- Khi hoàn thiện draft xong mới tạo application thì các state như `APP_CREATED`, `APP_IN_PROGRESS`, `APP_COMPLETED` không còn phản ánh đúng lịch sử hồ sơ.

Thiết kế mới coi `loan_application` là aggregate root ngay từ đầu. Dữ liệu từng bước được lưu vào bảng phụ theo application, không cần bảng draft riêng.

## Database chuẩn sau thay đổi

Migration chính:

```text
backend/loan-onboarding/src/main/resources/db/migration/V11__move_draft_flow_to_loan_application.sql
```

Migration này:

- Bổ sung/cập nhật state: `APP_CREATED`, `APP_IN_PROGRESS`, `APP_COMPLETED`, `APP_SUBMITTED`, `APP_CANCELLED`, `APP_EXPIRED`.
- Bổ sung transition: `IDENTIFY_COMPLETE`, `COMPLETE_APPLICATION`, `SUBMIT`, `CANCEL`, `EXPIRE`, `REOPEN`.
- Thêm cột vào `loan_application`: `current_step_code`, `expired_at`, `created_at`, `updated_at`.
- Tạo bảng `loan_application_step_data`.
- Tạo bảng `loan_application_step_history`.
- Map dữ liệu cũ từ `APP_DRAFT` sang `APP_CREATED`.
- Xóa state `APP_DRAFT` khỏi lookup.
- Drop bảng cũ: `loan_application_draft_history`, `loan_application_draft_step_data`, `loan_application_draft`.

Nguồn dữ liệu chuẩn:

- Hồ sơ vay: `loan_application`.
- Trạng thái hiện tại: `loan_application.current_state_id`.
- Bước hiện tại: `loan_application.current_step_code`.
- Payload từng bước: `loan_application_step_data.payload`.
- Audit state: `loan_application_state_history`.
- Audit step: `loan_application_step_history`.

## Code đã chuẩn hóa

Đã bỏ các file draft cũ khỏi code:

- `LoanApplicationDraft`
- `LoanApplicationDraftStepData`
- `LoanApplicationDraftHistory`
- `LoanApplicationDraftRepository`
- `LoanApplicationDraftStepDataRepository`
- `LoanApplicationDraftHistoryRepository`
- `LoanApplicationDraftHistoryAction`
- `SaveLoanApplicationDraftStepRequest`

Đã thay bằng luồng onboarding:

- `LoanApplicationOnboardingController`
- `LoanApplicationOnboardingService`
- `LoanApplicationOnboardingServiceImpl`
- `CreateLoanApplicationOnboardingRequest`
- `CompleteLoanApplicationStepRequest`
- `CancelLoanApplicationOnboardingRequest`
- `LoanApplicationOnboardingDetailResponse`
- `LoanApplicationOnboardingSummaryResponse`
- `LoanApplicationStepActionResponse`
- `LoanApplicationSubmitResponse`
- `LoanApplicationStepData`
- `LoanApplicationStepHistory`

## API chuẩn cho Frontend

Base path mới:

```text
/api/v1/loan-applications/onboarding
```

Path cũ đã bỏ:

```text
/api/v1/loan-application-drafts
```

Các API cần dùng:

```text
POST /api/v1/loan-applications/onboarding
GET  /api/v1/loan-applications/onboarding
GET  /api/v1/loan-applications/onboarding/{applicationCode}
POST /api/v1/loan-applications/onboarding/{applicationCode}/steps/{stepCode}/complete
POST /api/v1/loan-applications/onboarding/{applicationCode}/submit
POST /api/v1/loan-applications/onboarding/{applicationCode}/cancel
```

Không còn API autosave `PUT`.

## Logic hoạt động

### Tạo hồ sơ

Frontend gọi Customer API của team Customer trước để có `customerId` hoặc `customerCode`.

Sau đó gọi:

```text
POST /api/v1/loan-applications/onboarding
```

Backend tạo `loan_application`.

- Nếu chưa có `customerIdentifyPayload`: state là `APP_CREATED`.
- Nếu có `customerIdentifyPayload`: step `CUSTOMER_IDENTIFY` được hoàn thành và state là `APP_IN_PROGRESS`.

Response trả `applicationCode`. Frontend chỉ lưu `applicationCode`, không còn `draftCode`.

### Hoàn thành từng bước

Frontend gọi:

```text
POST /api/v1/loan-applications/onboarding/{applicationCode}/steps/{stepCode}/complete
```

Backend:

- Lưu payload vào `loan_application_step_data`.
- Set step thành `COMPLETED`.
- Cập nhật `loan_application.current_step_code`.
- Ghi audit vào `loan_application_step_history`.
- Nếu tất cả step hoàn thành, chuyển hồ sơ sang `APP_COMPLETED`.

### Nộp hồ sơ

Frontend chỉ hiển thị nút submit khi hồ sơ ở `APP_COMPLETED`.

Backend chỉ cho submit khi:

- State hiện tại là `APP_COMPLETED`.
- Tất cả step là `COMPLETED`.
- Không có step nào `requiresReview = true`.

Submit thành công thì state chuyển sang `APP_SUBMITTED`.

## Mapping trạng thái cho UI

- `APP_CREATED`: hồ sơ vừa tạo, có thể tiếp tục nhập hoặc hủy.
- `APP_IN_PROGRESS`: hồ sơ đang hoàn thiện, có thể tiếp tục nhập hoặc hủy.
- `APP_COMPLETED`: hồ sơ đã đủ thông tin, có thể nộp sang thẩm định.
- `APP_SUBMITTED`: hồ sơ đã nộp, không chỉnh sửa trong onboarding.
- `APP_CANCELLED`: hồ sơ đã hủy, chỉ xem.
- `APP_EXPIRED`: hồ sơ hết hạn, chỉ xem.

## Ghi chú kiểm thử

Đã kiểm tra compile:

```text
mvn compile
```

Kết quả:

```text
BUILD SUCCESS
```

Khi chạy:

```text
mvn test
```

Test DMN chạy pass, nhưng Spring context test bị chặn do database local trả lỗi:

```text
FATAL: password authentication failed for user "postgres"
```

Muốn test API thật qua Spring context hoặc Swagger cần đảm bảo Postgres đang chạy đúng `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.

