# Nghiệm thu flow mới hồ sơ vay nháp - 2026-07-06

## 1. Bối cảnh

BA/DA đã xác nhận lại hướng xử lý hồ sơ nháp:

- Không có trạng thái `VERIFIED` cho `customer` hoặc `kyc_profile`.
- Giữ flow cũ tạm thời để không làm vỡ các API/FE đang chạy.
- Xây flow mới riêng dưới endpoint `/api/v1/loan-application-drafts`.
- Khi flow draft mới đủ màn 1-5 và convert ổn định thì mới chuyển FE sang flow mới.
- Dữ liệu nhập trong các màn của hồ sơ tư vấn/hồ sơ nháp được lưu vào bảng draft, cụ thể là `loan_application_draft_step_data.payload`, chưa ghi trực tiếp vào các bảng nghiệp vụ thật.

Tài liệu/dataflow đã được đọc và dùng làm căn cứ:

- `Dataflow tạo hồ sơ vay nháp & Lấy dữ liệu của bước hiện tại của hồ sơ tư vấn.drawio.xml`
- `Dataflow Lấy dữ liệu toàn bộ của hồ sơ tư vấn.drawio.xml`
- `Dataflow Màn 2.drawio.xml`
- `Dataflow Màn 3.drawio.xml`
- `Dataflow Màn 4.drawio.xml`

## 2. Điểm đã làm

### 2.1. Giữ flow cũ

Đã giữ nguyên các flow cũ đang tồn tại trong project.

Các API cũ liên quan tới customer, OCR, loan application, asset, valuation, product recommendation vẫn không bị chuyển toàn bộ sang draft trong lần sửa này.

Mục tiêu của việc này là đúng theo hướng BA/DA: chưa thay FE sang flow mới ngay, tránh ảnh hưởng các phần đang test được.

### 2.2. Tạo flow mới riêng cho hồ sơ vay nháp

Đã bổ sung và chuẩn hóa flow mới dưới:

```text
/api/v1/loan-application-drafts
```

Các API hiện có cho flow draft:

```text
POST /api/v1/loan-application-drafts
GET  /api/v1/loan-application-drafts/{draftId}
GET  /api/v1/loan-application-drafts/{draftId}/data
GET  /api/v1/loan-application-drafts/{draftId}/current-step
GET  /api/v1/loan-application-drafts/{draftId}/steps/{stepCode}
PUT  /api/v1/loan-application-drafts/{draftId}/steps/{stepCode}
```

### 2.3. Tạo hồ sơ vay nháp

API:

```text
POST /api/v1/loan-application-drafts
```

Request chính:

```json
{
  "customerId": "UUID_CUSTOMER"
}
```

Logic đã làm:

- Tìm customer theo `customerId`.
- Không kiểm tra `VERIFIED`.
- Tạo bản ghi trong `loan_application_draft`.
- Set trạng thái draft là `DRAFT`.
- Set `expired_at = thời điểm tạo + 30 ngày`.
- Set `current_step = PRELIMINARY_INFO`.
- Tạo dữ liệu step trong `loan_application_draft_step_data`.
- Ghi lịch sử vào `loan_application_draft_history`.

### 2.4. Khởi tạo trạng thái các step theo dataflow BA/DA

Khi tạo draft mới, hệ thống tạo đủ các step đang active.

Trạng thái khởi tạo:

| Step | Trạng thái |
| --- | --- |
| `CUSTOMER_IDENTIFY` | `COMPLETED` |
| `PRELIMINARY_INFO` | `IN_PROGRESS` |
| `CUSTOMER_DETAIL` | `NOT_STARTED` |
| `ASSET_DETAIL` | `NOT_STARTED` |
| `FINAL_LOAN_PROPOSAL` | `NOT_STARTED` |
| `UPLOAD_COMPLETE` | `NOT_STARTED` |

Payload của `CUSTOMER_IDENTIFY` được khởi tạo từ thông tin customer đã định danh:

```json
{
  "initialized": true,
  "identity_verified": true,
  "customer": {
    "customerId": "...",
    "customerCode": "...",
    "fullName": "...",
    "identityNumber": "...",
    "phoneNumber": "...",
    "dateOfBirth": "...",
    "status": "..."
  }
}
```

### 2.5. Lưu dữ liệu từng màn vào payload draft

API dùng chung:

```text
PUT /api/v1/loan-application-drafts/{draftId}/steps/{stepCode}
```

Nguyên tắc đã làm:

- FE gửi payload dạng JSON object.
- Backend không ghi dữ liệu màn 2-5 trực tiếp vào bảng thật.
- Backend lưu nguyên payload vào `loan_application_draft_step_data.payload`.
- Step vừa lưu được chuyển sang `COMPLETED`.
- Step tiếp theo được mở sang `IN_PROGRESS`.
- `loan_application_draft.current_step` được cập nhật sang step tiếp theo.
- Ghi lịch sử thao tác vào `loan_application_draft_history`.

Các step đang hỗ trợ lưu:

```text
PRELIMINARY_INFO
CUSTOMER_DETAIL
ASSET_DETAIL
FINAL_LOAN_PROPOSAL
UPLOAD_COMPLETE
```

### 2.6. Màn 2 - Thông tin sơ bộ & Gói vay

Step:

```text
PRELIMINARY_INFO
```

Dữ liệu màn 2 được lưu vào:

```text
loan_application_draft_step_data.payload
```

Payload kỳ vọng gồm các nhóm dữ liệu theo dataflow:

- `customer_preliminary`
- `loan_preliminary`
- `collateral_preliminary`
- `preliminary_deductions`
- `summary`

Sau khi lưu thành công:

- `PRELIMINARY_INFO` -> `COMPLETED`
- `CUSTOMER_DETAIL` -> `IN_PROGRESS`
- `current_step` của draft -> `CUSTOMER_DETAIL`

### 2.7. Màn 3 - Chi tiết khách hàng

Step:

```text
CUSTOMER_DETAIL
```

Dữ liệu màn 3 được lưu vào:

```text
loan_application_draft_step_data.payload
```

Payload kỳ vọng gồm:

- `customer_detail`
- `reference_persons`
- `summary`

Sau khi lưu thành công:

- `CUSTOMER_DETAIL` -> `COMPLETED`
- `ASSET_DETAIL` -> `IN_PROGRESS`
- `current_step` của draft -> `ASSET_DETAIL`

### 2.8. Màn 4 - Chi tiết tài sản

Step:

```text
ASSET_DETAIL
```

Dữ liệu màn 4 được lưu vào:

```text
loan_application_draft_step_data.payload
```

Payload kỳ vọng gồm:

- `vehicle_legal_info`
- `vehicle_registration_document`
- Các thông tin tài sản/giấy tờ xe theo dataflow màn 4

Sau khi lưu thành công:

- `ASSET_DETAIL` -> `COMPLETED`
- `FINAL_LOAN_PROPOSAL` -> `IN_PROGRESS`
- `current_step` của draft -> `FINAL_LOAN_PROPOSAL`

### 2.9. Màn 5 - Đề xuất gói vay cuối cùng

Step:

```text
FINAL_LOAN_PROPOSAL
```

Flow draft mới hiện cho phép lưu payload màn 5 vào:

```text
loan_application_draft_step_data.payload
```

Payload có thể chứa các nhóm dữ liệu:

- scoring
- nhu cầu vay điều chỉnh
- thông tin khoản vay
- gói vay cuối cùng được chọn
- lịch trả nợ dự kiến
- summary

Sau khi lưu thành công:

- `FINAL_LOAN_PROPOSAL` -> `COMPLETED`
- `UPLOAD_COMPLETE` -> `IN_PROGRESS`
- `current_step` của draft -> `UPLOAD_COMPLETE`

### 2.10. API lấy dữ liệu bước hiện tại

API:

```text
GET /api/v1/loan-application-drafts/{draftId}/current-step
```

Mục đích:

- FE biết draft đang ở step nào.
- FE lấy payload của step hiện tại để render màn tương ứng.

Dữ liệu trả về gồm:

- `draftId`
- `stepCode`
- `stepName`
- `stepOrder`
- `status`
- `payload`
- `completedAt`
- `updatedAt`

### 2.11. API lấy dữ liệu toàn bộ hồ sơ tư vấn

API:

```text
GET /api/v1/loan-application-drafts/{draftId}/data
```

API này trả về overview của draft và toàn bộ step data.

Mục đích:

- FE có thể lấy toàn bộ dữ liệu của hồ sơ tư vấn/hồ sơ nháp.
- Dùng khi cần khôi phục trạng thái màn hình, preview, hoặc debug dữ liệu đã nhập.

### 2.12. Đánh dấu dữ liệu downstream cần review

Đã bổ sung logic:

- Nếu người dùng sửa lại một step trước đó.
- Các step phía sau đã hoàn thành có thể bị đánh dấu `requires_review = true`.
- Có lưu thông tin step nào làm dữ liệu phía sau bị ảnh hưởng qua `invalidated_by_step_code`.

Mục tiêu:

- Tránh trường hợp sửa dữ liệu màn trước nhưng màn sau vẫn bị hiểu là chắc chắn hợp lệ.
- Phù hợp với hướng lưu từng bước trong draft.

### 2.13. Đồng bộ entity với database hiện tại

Đã chỉnh các entity draft để khớp schema mới từ migration của BA/DA.

Các entity đã chỉnh:

- `LoanApplicationDraft`
- `LoanApplicationDraftStepData`
- `LoanApplicationDraftHistory`

Điểm chính:

- Bỏ field không còn trong DB như `cancelledAt`, `convertedAt`.
- Bổ sung field mới như `requiresReview`, `reviewedAt`, `invalidatedByStep`.
- Bổ sung `metadata` cho history.

### 2.14. Bổ sung response customer trong overview draft

Đã thêm DTO:

```text
LoanApplicationDraftCustomerResponse
```

Mục đích:

- Khi FE lấy draft overview thì có sẵn thông tin customer cơ bản.
- Không cần FE tự join hoặc gọi lại API customer chỉ để hiển thị thông tin định danh.

Thông tin gồm:

- `customerId`
- `customerCode`
- `fullName`
- `identityNumber`
- `phoneNumber`
- `dateOfBirth`
- `status`

## 3. Kết quả test

### 3.1. Unit/integration test

Đã chạy:

```bash
./mvnw test
```

Kết quả:

```text
BUILD SUCCESS
```

### 3.2. Docker

Đã build và chạy lại backend bằng Docker.

Container hiện chạy:

```text
los-backend   Up, port 8080
los-postgres  Up healthy, port 5432
```

### 3.3. Smoke test API

Đã test các bước chính:

1. Tạo draft bằng customer seed.
2. Kiểm tra draft tạo ra có `currentStep = PRELIMINARY_INFO`.
3. Kiểm tra `CUSTOMER_IDENTIFY = COMPLETED`.
4. Kiểm tra `PRELIMINARY_INFO = IN_PROGRESS`.
5. Lưu payload màn 2.
6. Sau màn 2, draft chuyển sang `CUSTOMER_DETAIL`.
7. Lưu payload màn 3.
8. Sau màn 3, draft chuyển sang `ASSET_DETAIL`.
9. Lưu payload màn 4.
10. Sau màn 4, draft chuyển sang `FINAL_LOAN_PROPOSAL`.
11. Lưu payload màn 5.
12. Sau màn 5, draft chuyển sang `UPLOAD_COMPLETE`.

Kết quả đúng với hướng flow mới.

### 3.4. Kiểm tra không ghi nhầm bảng thật

Đã kiểm tra flow mới không tạo nhầm hồ sơ thật trong `loan_application` khi chỉ đang lưu draft.

Điều này đúng với yêu cầu:

- Màn 1-5 của flow draft mới chỉ lưu dữ liệu nháp.
- Chưa convert sang bảng thật nếu chưa có mapping convert chính thức.

## 4. Điểm chưa làm

### 4.1. Chưa làm convert draft sang hồ sơ thật

Chưa implement API convert từ draft sang các bảng nghiệp vụ thật.

Lý do:

- Các dataflow lần này mô tả rõ tạo draft, lấy step hiện tại, lấy toàn bộ dữ liệu, lưu dữ liệu màn 2, 3, 4.
- Chưa có dataflow/mapping cuối cùng cho bước convert từ `loan_application_draft_step_data.payload` sang các bảng thật.
- Nếu tự map khi chưa có tài liệu chốt, rủi ro cao sẽ ghi sai vào bảng thật.

Các bảng có thể liên quan khi convert nhưng cần BA/DA chốt mapping:

- `loan_application`
- `customer`
- `customer_detail` hoặc bảng tương ứng nếu có
- `customer_reference` hoặc bảng tương ứng nếu có
- `asset`
- `asset_legal`
- `asset_document`
- `loan_application_*` liên quan tới product/final offer nếu có

### 4.2. Chưa thay FE sang flow mới

Hiện tại mới có backend API cho flow draft mới.

FE chưa được chuyển sang gọi toàn bộ flow:

```text
/api/v1/loan-application-drafts
```

Theo hướng BA/DA, việc chuyển FE nên làm sau khi:

- Flow màn 1-5 đã ổn.
- API convert đã rõ.
- Mapping payload đã được thống nhất.

### 4.3. Chưa validate sâu từng field trong payload

API lưu step hiện tại kiểm tra payload phải là JSON object.

Chưa validate sâu từng field như:

- Màn 2 bắt buộc có `requested_amount`.
- Màn 3 bắt buộc có đủ 3 người tham chiếu.
- Màn 4 bắt buộc có số khung, số máy, số đăng ký xe.
- Màn 5 bắt buộc có product cuối cùng, kỳ hạn, lịch trả nợ.

Lý do:

- Payload đang được thiết kế linh hoạt theo hướng draft.
- Cần BA/DA chốt schema payload cuối cùng cho từng màn.

### 4.4. Chưa tách DTO request riêng cho từng màn

Hiện API lưu step đang dùng request chung:

```text
SaveLoanApplicationDraftStepRequest
```

Điều này phù hợp giai đoạn đầu vì payload của từng màn còn có thể thay đổi.

Khi schema từng màn được chốt, nên bổ sung DTO riêng:

- `SavePreliminaryInfoDraftRequest`
- `SaveCustomerDetailDraftRequest`
- `SaveAssetDetailDraftRequest`
- `SaveFinalLoanProposalDraftRequest`

### 4.5. Chưa có API submit/complete cuối cùng cho draft

Hiện sau khi lưu màn 5, hệ thống mở step:

```text
UPLOAD_COMPLETE
```

Chưa có API riêng để:

- Upload chứng từ.
- Hoàn tất hồ sơ nháp.
- Convert hồ sơ nháp sang hồ sơ thật.
- Khóa payload sau khi submit.

Phần này cần dataflow màn 6 hoặc tài liệu convert cuối cùng.

### 4.6. Chưa xử lý đầy đủ rule rollback/reopen step

Đã có logic đánh dấu step sau cần review khi sửa step trước.

Chưa làm đầy đủ các nghiệp vụ nâng cao như:

- Reopen step đã completed.
- Bắt buộc người dùng xác nhận lại các step downstream.
- Chặn submit nếu có step `requires_review = true`.
- Audit chi tiết field nào thay đổi.

Các phần này nên làm sau khi BA/DA chốt rule sửa dữ liệu giữa các bước.

## 5. File code đã thay đổi chính

```text
backend/loan-onboarding/src/main/java/com/f88/loanonboarding/controller/LoanApplicationDraftController.java
backend/loan-onboarding/src/main/java/com/f88/loanonboarding/service/LoanApplicationDraftService.java
backend/loan-onboarding/src/main/java/com/f88/loanonboarding/service/impl/LoanApplicationDraftServiceImpl.java
backend/loan-onboarding/src/main/java/com/f88/loanonboarding/entity/LoanApplicationDraft.java
backend/loan-onboarding/src/main/java/com/f88/loanonboarding/entity/LoanApplicationDraftStepData.java
backend/loan-onboarding/src/main/java/com/f88/loanonboarding/entity/LoanApplicationDraftHistory.java
backend/loan-onboarding/src/main/java/com/f88/loanonboarding/dto/response/draft/LoanApplicationDraftOverviewResponse.java
backend/loan-onboarding/src/main/java/com/f88/loanonboarding/dto/response/draft/LoanApplicationDraftCustomerResponse.java
```

## 6. Kết luận nghiệm thu

Flow mới `/api/v1/loan-application-drafts` đã được dựng theo hướng BA/DA:

- Có thể tạo hồ sơ vay nháp.
- Có thể lưu dữ liệu từng màn vào bảng draft.
- Có thể lấy dữ liệu bước hiện tại.
- Có thể lấy toàn bộ dữ liệu hồ sơ tư vấn.
- Có thể đi lần lượt từ màn 2 đến màn 5.
- Chưa ghi trực tiếp vào các bảng nghiệp vụ thật.
- Chưa làm convert sang hồ sơ thật do thiếu mapping/dataflow convert chính thức.

Trạng thái hiện tại phù hợp để FE bắt đầu tích hợp thử flow nháp mới ở môi trường dev, nhưng chưa nên thay thế hoàn toàn flow cũ cho tới khi BA/DA chốt phần convert và schema payload cuối cùng.
