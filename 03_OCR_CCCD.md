# OCR CCCD — Tài liệu kỹ thuật

## 1. Mục tiêu tính năng

Trong WF-01 (Định danh Khách hàng), nhân viên PGD không cần nhập tay 4 trường thông tin định danh. Thay vào đó, nhân viên upload ảnh CCCD/CMT lên hệ thống. BE gọi **FPT AI ID Recognition** để trích xuất thông tin và trả về cho FE tự động điền vào form.

Các trường được điền tự động qua OCR:

| Trường trên form | Nguồn dữ liệu |
|---|---|
| Họ và tên | OCR mặt trước |
| Ngày sinh | OCR mặt trước |
| Số giấy tờ định danh | OCR mặt trước |
| **Số điện thoại** | **Không có trong OCR — Staff nhập tay** |

---

## 2. Những gì đã làm

### 2.1. File mới tạo

| File | Vai trò |
|---|---|
| `dto/response/customer/OcrExtractResponse.java` | DTO response trả về FE sau khi OCR xong |
| `service/OcrService.java` | Interface định nghĩa contract cho OCR |
| `service/impl/FptAiOcrServiceImpl.java` | Implementation gọi FPT AI, parse kết quả, xử lý lỗi |
| `config/RestTemplateConfig.java` | Cấu hình `RestTemplate` bean để gọi HTTP ra ngoài |

### 2.2. File đã sửa

| File | Thay đổi |
|---|---|
| `common/error/ErrorCode.java` | Thêm 4 error code: `OCR_ID_NOT_FOUND`, `OCR_CROP_FAILED`, `OCR_INVALID_IMAGE`, `OCR_SERVICE_ERROR` |
| `exception/GlobalExceptionHandler.java` | Thêm handler cho `MaxUploadSizeExceededException` (ảnh > 5MB → HTTP 413) |
| `application.properties` | Thêm config FPT AI (`api-url`, `api-key`) và giới hạn multipart 5MB/12MB |
| `controller/CustomerController.java` | Thêm endpoint `POST /api/v1/customers/ocr/extract` |

### 2.3. Luồng xử lý trong BE

```
FE upload ảnh (multipart/form-data)
    │
    ▼
CustomerController.extractOcr()
    │
    ▼
FptAiOcrServiceImpl.extract()
    │
    ├── callFptAi(frontImage) → POST https://api.fpt.ai/vision/idr/vnm/
    │       Header: api-key: ***
    │       Body: multipart image
    │       Response: { errorCode, errorMessage, data: [...] }
    │
    ├── [nếu có backImage] callFptAi(backImage) → lấy issueDate (ngày cấp)
    │
    └── Map kết quả → OcrExtractResponse → ApiResponse<OcrExtractResponse>
```

---

## 3. API Specification

### Endpoint

```
POST /api/v1/customers/ocr/extract
Content-Type: multipart/form-data
```

### Input (Request)

| Field | Kiểu | Bắt buộc | Mô tả |
|---|---|:---:|---|
| `frontImage` | file | ✅ | Ảnh mặt trước CCCD/CMT. JPG, PNG, WEBP. Tối đa 5MB |
| `backImage` | file | ❌ | Ảnh mặt sau CCCD/CMT. Nếu có sẽ lấy thêm ngày cấp |

**Yêu cầu chất lượng ảnh (theo FPT AI):**
- Đủ 4 góc giấy tờ, rõ ràng
- Không tẩy xoá, nhoè nước
- Độ phân giải tối thiểu 640×480
- Diện tích CCCD chiếm ít nhất ¼ tổng ảnh

### Output — Thành công (HTTP 200)

```json
{
  "success": true,
  "message": "OCR extraction completed",
  "data": {
    "fullName": "NGUYEN VAN AN",
    "dateOfBirth": "15/01/1995",
    "identityNumber": "079195000001",
    "documentType": "cccd_12_front",
    "sex": "Nam",
    "nationality": "Việt Nam",
    "expiryDate": "15/01/2035",
    "issueDate": "20/06/2021",
    "frontImageProcessed": true,
    "backImageProcessed": true
  },
  "errorCode": null,
  "timestamp": "2026-06-29T10:30:00"
}
```

### Giá trị của `documentType`

| Giá trị | Ý nghĩa |
|---|---|
| `cccd_12_front` | Căn cước công dân mặt trước |
| `cmnd_12_front` | Chứng minh nhân dân 12 số mặt trước |
| `cmnd_09_front` | Chứng minh nhân dân 9 số (cũ) mặt trước |

> `issueDate` chỉ có giá trị khi upload cả mặt sau. Nếu chỉ upload mặt trước, `issueDate = null`.

---

## 4. Xử lý lỗi

### Lỗi từ FPT AI (ánh xạ sang error code nội bộ)

| FPT AI errorCode | Error code nội bộ | HTTP | Ý nghĩa |
|:---:|---|:---:|---|
| `2` | `OCR_CROP_FAILED` | 400 | Ảnh bị thiếu góc, không thể crop |
| `3` | `OCR_ID_NOT_FOUND` | 400 | Không tìm thấy giấy tờ trong ảnh |
| `7` | `OCR_INVALID_IMAGE` | 400 | File không phải ảnh hợp lệ |
| `8` | `OCR_INVALID_IMAGE` | 400 | File ảnh bị hỏng hoặc format không hỗ trợ |
| Còn lại | `OCR_SERVICE_ERROR` | 400 | Lỗi không xác định từ FPT AI |

### Lỗi từ BE

| Tình huống | Error code | HTTP | Message |
|---|---|:---:|---|
| Ảnh vượt 5MB | `OCR_INVALID_IMAGE` | 413 | Kích thước ảnh vượt quá 5MB |
| FPT AI không phản hồi | `OCR_SERVICE_ERROR` | 400 | Lỗi kết nối dịch vụ OCR |
| `data[]` rỗng trong response | `OCR_ID_NOT_FOUND` | 400 | Không tìm thấy thông tin giấy tờ trong ảnh |

### Cấu trúc response lỗi

```json
{
  "success": false,
  "message": "Không tìm thấy giấy tờ trong ảnh, vui lòng chụp lại",
  "data": null,
  "errorCode": "OCR_ID_NOT_FOUND",
  "timestamp": "2026-06-29T10:30:00"
}
```

---

## 5. Cách test

### Cách 1 — Swagger UI (khuyến nghị)

1. Chạy backend:
   ```powershell
   cd backend/loan-onboarding
   .\mvnw spring-boot:run
   ```
2. Mở trình duyệt: `http://localhost:8080/swagger-ui/index.html`
3. Tìm section **Customer** → `POST /api/v1/customers/ocr/extract`
4. Bấm **Try it out**
5. Ở field `frontImage` → **Choose File** → chọn ảnh CCCD mặt trước
6. Ở field `backImage` → có thể để trống
7. Bấm **Execute** → xem kết quả trong **Response body**

### Cách 2 — curl (Windows PowerShell)

```powershell
# Chỉ mặt trước
curl -X POST http://localhost:8080/api/v1/customers/ocr/extract `
  -F "frontImage=@C:\path\to\cccd_mat_truoc.jpg"

# Cả mặt trước và mặt sau
curl -X POST http://localhost:8080/api/v1/customers/ocr/extract `
  -F "frontImage=@C:\path\to\cccd_mat_truoc.jpg" `
  -F "backImage=@C:\path\to\cccd_mat_sau.jpg"
```

### Cách 3 — Postman

1. Tạo request mới: `POST http://localhost:8080/api/v1/customers/ocr/extract`
2. Tab **Body** → chọn **form-data**
3. Thêm key `frontImage`, đổi type sang **File** → chọn file ảnh
4. (Tuỳ chọn) Thêm key `backImage`, đổi type sang **File**
5. Bấm **Send**

---

## 6. Test case gợi ý

| Case | Input | Expected output |
|---|---|---|
| CCCD mặt trước, chất lượng tốt | 1 ảnh CCCD rõ | `fullName`, `dateOfBirth`, `identityNumber` đầy đủ; `documentType = cccd_12_front` |
| CCCD cả 2 mặt | 2 ảnh CCCD | Đầy đủ như trên + có `issueDate`; `backImageProcessed = true` |
| CMT cũ 9 số | Ảnh CMT cũ | `documentType = cmnd_09_front`; `expiryDate = null` (CMT cũ không có) |
| Ảnh mờ/thiếu góc | Ảnh chất lượng kém | Error `OCR_ID_NOT_FOUND` hoặc `OCR_CROP_FAILED` |
| File không phải ảnh | File `.pdf` hoặc `.txt` | Error `OCR_INVALID_IMAGE` |
| Ảnh > 5MB | File lớn | HTTP 413 + message kích thước vượt giới hạn |

---

## 7. Cấu hình

File `application.properties`:

```properties
# FPT AI - ID Recognition (OCR)
fptai.ocr.api-url=https://api.fpt.ai/vision/idr/vnm/
fptai.ocr.api-key=${FPTAI_API_KEY:your_key_here}

# Giới hạn kích thước upload
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=12MB
```

> **Lưu ý bảo mật:** Không commit API key thật vào Git. Khi deploy production, set biến môi trường `FPTAI_API_KEY` thay vì để giá trị trực tiếp trong file properties.

---

## 8. Những gì chưa làm / bước tiếp theo

| Việc | Lý do chưa làm |
|---|---|
| Lưu ảnh CCCD vào storage (S3/local) | Chưa có yêu cầu lưu trữ ảnh |
| Validate xác thực chéo (tên OCR vs tên KH nhập) | Chờ BA xác nhận rule |
| OCR database cho unit test | Rule và flow WF-01 chưa ổn định |
| Tích hợp kết quả OCR vào `CustomerLookupRequest` | FE tự điền form, BE nhận qua lookup |

