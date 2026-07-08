# Document Upload S3 API

Ngày cập nhật: 2026-07-08

## Tổng quan

Luồng upload chứng từ hiện tại dùng S3 làm nơi lưu file vật lý và bảng `loan_application_document` làm nơi lưu reference theo hồ sơ vay.

Base URL local:

```text
http://localhost:8080
```

Khi chạy qua Docker frontend/proxy:

```text
http://localhost:5173
```

Base path API onboarding:

```text
/api/v1/loan-applications/onboarding
```

## Cấu hình S3

Backend đọc cấu hình S3 từ biến môi trường:

```text
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
AWS_REGION
AWS_S3_BUCKET
AWS_S3_ENDPOINT
AWS_S3_PUBLIC_URL_BASE
AWS_S3_PATH_STYLE_ACCESS_ENABLED
```

Các biến bắt buộc khi upload lên AWS S3 thật:

```text
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
AWS_REGION
AWS_S3_BUCKET
```

Các biến optional:

```text
AWS_S3_ENDPOINT
AWS_S3_PUBLIC_URL_BASE
AWS_S3_PATH_STYLE_ACCESS_ENABLED
```

`AWS_S3_ENDPOINT` và `AWS_S3_PATH_STYLE_ACCESS_ENABLED` thường dùng cho S3-compatible storage như MinIO. Với AWS S3 thật có thể để trống endpoint và path style là `false`.

## S3 object key

File được upload vào S3 theo format:

```text
loan-applications/{APPLICATION_CODE}/documents/{DOCUMENT_TYPE_CODE}/{UUID}-{FILENAME}
```

Ví dụ:

```text
loan-applications/APP-2026-6B07A687/documents/CITIZEN_ID_FRONT/46714774-aa6d-4ea2-ab4d-bbf77945beee-front.jpg
```

Ý nghĩa:

- `APPLICATION_CODE`: mã hồ sơ vay.
- `DOCUMENT_TYPE_CODE`: mã loại chứng từ trong bảng `document_type`.
- `UUID`: mã sinh tự động để tránh trùng tên file.
- `FILENAME`: tên file gốc đã được sanitize.

## Bảng dữ liệu liên quan

### `document_type`

Lưu danh mục loại chứng từ.

Các field chính:

```text
id
code
name
description
is_required
is_active
sort_order
```

Một số document type đang được dùng cho màn upload:

```text
CUSTOMER_SIGNED_CONTRACT
REFERENCE_VERIFICATION_FORM
```

Ngoài ra hệ thống có thể có các document type khác từ seed trước đó, ví dụ:

```text
CITIZEN_ID_FRONT
CITIZEN_ID_BACK
```

### `loan_application_document`

Lưu reference file đã upload theo hồ sơ.

Các field chính:

```text
id
loan_application_id
document_type_id
file_url
file_name
uploaded_at
uploaded_by
note
```

Lưu ý quan trọng: logic hiện tại đang lưu theo hướng mỗi cặp `loan_application + document_type` chỉ giữ một reference mới nhất. Nếu upload nhiều file cùng một `documentTypeCode`, S3 vẫn có thể nhận nhiều object, nhưng DB sẽ chỉ giữ reference cuối cùng cho document type đó.

## API 1. Upload chứng từ lên S3

```http
POST /api/v1/loan-applications/onboarding/{applicationCode}/documents
Content-Type: multipart/form-data
```

API này làm 3 việc:

1. Validate hồ sơ còn được phép chỉnh sửa.
2. Upload file lên S3.
3. Lưu hoặc cập nhật reference trong `loan_application_document`.

### Path params

| Tên | Bắt buộc | Mô tả |
| --- | --- | --- |
| `applicationCode` | Có | Mã hồ sơ vay, ví dụ `APP-2026-6B07A687`. |

### Form data

| Tên | Kiểu | Bắt buộc | Mô tả |
| --- | --- | --- | --- |
| `documentTypeCodes` | string[] | Có | Danh sách mã loại chứng từ. Số lượng phải bằng số lượng `files`. |
| `files` | file[] | Có | Danh sách file upload. |
| `uploadedBy` | string | Không | Người upload, ví dụ username/staff code. |

Mapping giữa `documentTypeCodes` và `files` theo thứ tự index:

```text
documentTypeCodes[0] -> files[0]
documentTypeCodes[1] -> files[1]
documentTypeCodes[2] -> files[2]
```

### Điều kiện hồ sơ

Không được upload chứng từ nếu hồ sơ đã ở một trong các trạng thái:

```text
APP_SUBMITTED
APP_CANCELLED
APP_EXPIRED
APP_CLOSED
```

### Giới hạn file

Dung lượng tối đa mỗi file:

```text
10MB
```

Tổng request multipart mặc định:

```text
150MB
```

Content type được hỗ trợ:

```text
image/jpeg
image/png
image/webp
application/pdf
video/mp4
video/webm
video/quicktime
```

### Ví dụ curl: upload một file

```bash
curl -X POST "http://localhost:5173/api/v1/loan-applications/onboarding/APP-2026-6B07A687/documents" \
  -F "documentTypeCodes=CITIZEN_ID_FRONT" \
  -F "files=@/path/to/front.jpg;type=image/jpeg" \
  -F "uploadedBy=staff_001"
```

### Ví dụ curl: upload nhiều file khác loại chứng từ

```bash
curl -X POST "http://localhost:5173/api/v1/loan-applications/onboarding/APP-2026-6B07A687/documents" \
  -F "documentTypeCodes=CITIZEN_ID_FRONT" \
  -F "documentTypeCodes=CITIZEN_ID_BACK" \
  -F "documentTypeCodes=CUSTOMER_SIGNED_CONTRACT" \
  -F "files=@/path/to/front.jpg;type=image/jpeg" \
  -F "files=@/path/to/back.jpg;type=image/jpeg" \
  -F "files=@/path/to/contract.pdf;type=application/pdf" \
  -F "uploadedBy=staff_001"
```

### Ví dụ curl: nhiều file cùng một loại chứng từ

```bash
curl -X POST "http://localhost:5173/api/v1/loan-applications/onboarding/APP-2026-6B07A687/documents" \
  -F "documentTypeCodes=REFERENCE_VERIFICATION_FORM" \
  -F "documentTypeCodes=REFERENCE_VERIFICATION_FORM" \
  -F "files=@/path/to/ref-1.jpg;type=image/jpeg" \
  -F "files=@/path/to/ref-2.jpg;type=image/jpeg" \
  -F "uploadedBy=staff_001"
```

Lưu ý: với logic hiện tại, ví dụ này upload được nhiều object lên S3 nhưng DB chỉ giữ reference cuối cùng của `REFERENCE_VERIFICATION_FORM`.

### Response thành công

```json
{
  "success": true,
  "message": "Documents uploaded",
  "data": {
    "applicationCode": "APP-2026-6B07A687",
    "uploadedCount": 1,
    "documents": [
      {
        "documentId": "7ffd2075-a994-42fd-bb37-562f49a914d3",
        "documentTypeCode": "CITIZEN_ID_FRONT",
        "documentTypeName": "CCCD mặt trước",
        "fileUrl": "https://bucket-name.s3.ap-southeast-1.amazonaws.com/loan-applications/APP-2026-6B07A687/documents/CITIZEN_ID_FRONT/46714774-aa6d-4ea2-ab4d-bbf77945beee-front.jpg",
        "fileName": "front.jpg",
        "uploadedAt": "2026-07-07T18:57:24.503892488"
      }
    ]
  },
  "errorCode": null,
  "timestamp": "2026-07-07T18:57:26.910670826"
}
```

### Response lỗi thường gặp

Sai số lượng `documentTypeCodes` và `files`:

```json
{
  "success": false,
  "message": "Số lượng documentTypeCodes phải khớp với số lượng files.",
  "data": null,
  "errorCode": "VALIDATION_ERROR",
  "timestamp": "2026-07-08T10:00:00"
}
```

File rỗng:

```json
{
  "success": false,
  "message": "File chứng từ không được rỗng.",
  "data": null,
  "errorCode": "VALIDATION_ERROR",
  "timestamp": "2026-07-08T10:00:00"
}
```

File quá dung lượng:

```json
{
  "success": false,
  "message": "Dung lượng file chứng từ tối đa là 10MB.",
  "data": null,
  "errorCode": "VALIDATION_ERROR",
  "timestamp": "2026-07-08T10:00:00"
}
```

Sai content type:

```json
{
  "success": false,
  "message": "File chứng từ chỉ hỗ trợ JPG, PNG, WEBP, PDF hoặc video MP4/WEBM/MOV.",
  "data": null,
  "errorCode": "VALIDATION_ERROR",
  "timestamp": "2026-07-08T10:00:00"
}
```

Document type không tồn tại hoặc inactive:

```json
{
  "success": false,
  "message": "Loại chứng từ không tồn tại hoặc không còn hoạt động: UNKNOWN_TYPE",
  "data": null,
  "errorCode": "RESOURCE_NOT_FOUND",
  "timestamp": "2026-07-08T10:00:00"
}
```

Hồ sơ không còn được chỉnh sửa:

```json
{
  "success": false,
  "message": "Không được upload chứng từ khi hồ sơ đã nộp, đã hủy hoặc đã hết hạn.",
  "data": null,
  "errorCode": "INVALID_LOAN_APPLICATION_STATE",
  "timestamp": "2026-07-08T10:00:00"
}
```

Lỗi S3:

```json
{
  "success": false,
  "message": "Không upload được chứng từ lên S3: Access Denied",
  "data": null,
  "errorCode": "VALIDATION_ERROR",
  "timestamp": "2026-07-08T10:00:00"
}
```

## API 2. Lấy danh sách chứng từ của hồ sơ vay

```http
GET /api/v1/loan-applications/onboarding/{applicationCode}/documents
```

API này trả về danh sách chứng từ đã upload và đang được lưu reference trong `loan_application_document`.

### Path params

| Tên | Bắt buộc | Mô tả |
| --- | --- | --- |
| `applicationCode` | Có | Mã hồ sơ vay, ví dụ `APP-2026-6B07A687`. |

### Response thành công

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "applicationCode": "APP-2026-6B07A687",
    "documentCount": 2,
    "documents": [
      {
        "documentId": "7ffd2075-a994-42fd-bb37-562f49a914d3",
        "documentTypeCode": "CITIZEN_ID_FRONT",
        "documentTypeName": "CCCD mặt trước",
        "fileUrl": "https://bucket-name.s3.ap-southeast-1.amazonaws.com/loan-applications/APP-2026-6B07A687/documents/CITIZEN_ID_FRONT/46714774-aa6d-4ea2-ab4d-bbf77945beee-front.jpg",
        "fileName": "front.jpg",
        "uploadedAt": "2026-07-08T10:00:00",
        "uploadedBy": "staff_001"
      },
      {
        "documentId": "6cb9a7cb-14a2-4c4d-91ea-87f4bd02f72e",
        "documentTypeCode": "CITIZEN_ID_BACK",
        "documentTypeName": "CCCD mặt sau",
        "fileUrl": "https://bucket-name.s3.ap-southeast-1.amazonaws.com/loan-applications/APP-2026-6B07A687/documents/CITIZEN_ID_BACK/8cf144b2-back.jpg",
        "fileName": "back.jpg",
        "uploadedAt": "2026-07-08T10:01:00",
        "uploadedBy": "staff_001"
      }
    ]
  },
  "errorCode": null,
  "timestamp": "2026-07-08T10:02:00"
}
```

### Response khi hồ sơ không tồn tại

```json
{
  "success": false,
  "message": "Không tìm thấy hồ sơ vay",
  "data": null,
  "errorCode": "APP_404",
  "timestamp": "2026-07-08T10:00:00"
}
```

## API 3. Hoàn thành bước upload chứng từ

```http
POST /api/v1/loan-applications/onboarding/{applicationCode}/steps/UPLOAD_COMPLETE/complete
Content-Type: application/json
```

API này đánh dấu step upload chứng từ là `COMPLETED`. File chứng từ nên được upload bằng API `/documents` trước khi gọi API này.

### Request body

```json
{
  "payload": {
    "documents": [
      {
        "documentTypeCode": "CITIZEN_ID_FRONT",
        "fileUrl": "https://bucket-name.s3.ap-southeast-1.amazonaws.com/loan-applications/APP-2026-6B07A687/documents/CITIZEN_ID_FRONT/46714774-aa6d-4ea2-ab4d-bbf77945beee-front.jpg"
      }
    ]
  }
}
```

Payload được lưu vào `loan_application_step_data`. Reference chính thức của file vẫn là bảng `loan_application_document`.

### Response thành công

Response phụ thuộc DTO `LoanApplicationStepActionResponse`, dạng tổng quát:

```json
{
  "success": true,
  "message": "Application step completed",
  "data": {
    "applicationCode": "APP-2026-6B07A687",
    "currentStepCode": "NEXT_STEP_CODE",
    "applicationState": "APP_IN_PROGRESS"
  },
  "errorCode": null,
  "timestamp": "2026-07-08T10:00:00"
}
```

## API 4. Submit hồ sơ

```http
POST /api/v1/loan-applications/onboarding/{applicationCode}/submit
```

API này nộp hồ sơ sang thẩm định. Sau khi hồ sơ chuyển sang `APP_SUBMITTED`, API upload chứng từ sẽ bị chặn.

### Response thành công

Response phụ thuộc DTO `LoanApplicationSubmitResponse`, dạng tổng quát:

```json
{
  "success": true,
  "message": "Application submitted",
  "data": {
    "applicationCode": "APP-2026-6B07A687",
    "applicationState": "APP_SUBMITTED"
  },
  "errorCode": null,
  "timestamp": "2026-07-08T10:00:00"
}
```

## Luồng tích hợp frontend đề xuất

Thứ tự gọi API khi người dùng nhấn gửi phê duyệt:

```text
1. POST /{applicationCode}/documents
2. POST /{applicationCode}/steps/UPLOAD_COMPLETE/complete
3. POST /{applicationCode}/submit
```

Không nên gọi submit trước khi upload document, vì sau submit hồ sơ không còn được phép upload thêm chứng từ.

## Kiểm tra dữ liệu sau upload

Kiểm tra DB:

```bash
docker exec los-postgres psql -U postgres -d loan_onboarding -c "
select
  lad.id,
  la.loan_application_code,
  dt.code,
  lad.file_name,
  lad.file_url,
  lad.uploaded_by,
  lad.uploaded_at
from loan_application_document lad
join loan_application la on la.id = lad.loan_application_id
join document_type dt on dt.id = lad.document_type_id
order by lad.uploaded_at desc
limit 10;
"
```

Kiểm tra log upload:

```bash
docker compose logs -f backend frontend
```

## Giới hạn hiện tại

1. API đã nhận được nhiều file trong một request.
2. API đã upload file vào thư mục S3 theo từng `documentTypeCode`.
3. DB hiện chỉ giữ một reference mới nhất cho mỗi cặp `loan_application + document_type`.
4. Nếu nghiệp vụ cần một document type có nhiều ảnh và DB lưu đủ từng ảnh, cần đổi logic repository/service để mỗi file tạo một dòng `loan_application_document` mới thay vì update dòng cũ.
