# Frontend Guide - Upload Chung Tu Len S3

Tai lieu nay huong dan frontend trien khai man hinh **Buoc 4: Upload chung tu & Hoan tat** voi cac API backend dang co.

Muc tieu:

- FE upload file len S3 theo tung danh muc chung tu.
- BE tra ve `fileUrl` de FE preview ngay tren man hinh.
- FE xoa anh khi user bam nut X.
- FE gui danh sach documents cuoi cung de BE luu metadata vao `loan_application_document`.
- Ho tro nhieu anh trong cung mot danh muc chung tu.

## 1. Nguyen tac chung

Man hinh nay lam viec voi **ho so vay chinh thuc**, khong dung `draftCode`.

```text
Dung:  applicationCode, vi du APP-2026-000006
Khong dung: draftCode
```

Flow frontend:

```text
User chon file
-> FE goi API upload theo documentCode
-> BE upload file len S3 va tra fileUrl
-> FE luu fileUrl vao state de preview
-> User co the xoa file bang fileUrl
-> User bam Gui di de phe duyet
-> FE goi API complete documents
-> BE insert metadata vao loan_application_document theo loan_application_id
```

API upload chi upload file len S3 va tra URL. Metadata chi duoc luu vao `loan_application_document` khi goi API complete.

## 2. Danh sach documentCode frontend can dung

Frontend dung `documentCode` trong URL upload. Backend se map sang `documentTypeCode` de luu DB.

| Nhom UI | Label tren UI | Required | documentCode FE gui | documentTypeCode BE luu |
| --- | --- | --- | --- | --- |
| CCCD | CCCD mat truoc | Yes | `CITIZEN_ID_FRONT` | `CITIZEN_ID_FRONT` |
| CCCD | CCCD mat sau | Yes | `CITIZEN_ID_BACK` | `CITIZEN_ID_BACK` |
| Ca vet xe | Ca vet mat truoc | Yes | `VEHICLE_REGISTRATION_FRONT` | `VEHICLE_REGISTRATION_FRONT` |
| Ca vet xe | Ca vet mat sau | Yes | `VEHICLE_REGISTRATION_BACK` | `VEHICLE_REGISTRATION_BACK` |
| Anh tai san | Anh xe - Goc truoc | Yes | `ASSET_FRONT` | `ASSET_FRONT_IMAGE` |
| Anh tai san | Anh xe - Goc sau | Yes | `ASSET_REAR` | `ASSET_BACK_IMAGE` |
| Anh tai san | Anh xe - Goc trai | Yes | `ASSET_LEFT` | `ASSET_LEFT_IMAGE` |
| Anh tai san | Anh xe - Goc phai | Yes | `ASSET_RIGHT` | `ASSET_RIGHT_IMAGE` |
| Anh tai san | Anh so khung | No | `ASSET_FRAME_NUMBER` | `ASSET_FRAME_NUMBER_IMAGE` |
| Anh tai san | Anh so may | No | `ASSET_ENGINE_NUMBER` | `ASSET_ENGINE_NUMBER_IMAGE` |
| Anh tai san | Anh dong ho ODO | No | `ASSET_ODO` | `ASSET_ODOMETER_IMAGE` |
| Chan dung Khach hang | Anh chan dung khach hang | Yes | `CUSTOMER_PORTRAIT` | `BORROWER_PORTRAIT_IMAGE` |
| Chan dung Khach hang | Anh chan dung cam CCCD | No | `CUSTOMER_HOLDING_ID` | `BORROWER_HOLDING_CITIZEN_ID_IMAGE` |
| Chan dung Khach hang | Video chan dung Khach hang | Yes | `CUSTOMER_PORTRAIT_VIDEO` | `BORROWER_PORTRAIT_VIDEO` |
| Chung tu khac | Chung minh thu nhap | No | `INCOME_PROOF` | `INCOME_PROOF` |
| Chung tu khac | Chung minh noi cu tru | No | `RESIDENCE_PROOF` | `RESIDENCE_PROOF_DOCUMENT` |
| Chung tu khac | Hop dong khach hang da ky | No | `SIGNED_CUSTOMER_CONTRACT` | `SIGNED_CUSTOMER_CONTRACT` |
| Chung tu khac | Phieu xac minh tham chieu | No | `REFERENCE_VERIFICATION_FORM` | `REFERENCE_VERIFICATION_FORM` |

## 3. Gioi han file

Backend dang validate:

```text
Max size: 5MB / file
Anh hoac PDF: jpg, jpeg, png, webp, pdf
Video chan dung: mp4, webm, mov
```

Rieng `CUSTOMER_PORTRAIT_VIDEO` chi chap nhan:

```text
mp4, webm, mov
```

Cac documentCode con lai chap nhan:

```text
jpg, jpeg, png, webp, pdf
```

Frontend nen validate truoc de tranh user upload sai dinh dang.

## 4. State frontend de quan ly file

Goi y state theo `documentCode`:

```ts
type UploadedDocumentFile = {
  applicationCode: string;
  documentCode: string;
  documentTypeCode: string;
  fileId: string;
  fileName: string;
  contentType: string;
  size: number;
  fileUrl: string;
  uploadedAt: string;
};

type DocumentUploadState = Record<string, UploadedDocumentFile[]>;
```

Vi du:

```ts
const documentsByCode: DocumentUploadState = {
  CITIZEN_ID_FRONT: [
    {
      applicationCode: "APP-2026-000006",
      documentCode: "CITIZEN_ID_FRONT",
      documentTypeCode: "CITIZEN_ID_FRONT",
      fileId: "uuid",
      fileName: "cccd-front.jpg",
      contentType: "image/jpeg",
      size: 12345,
      fileUrl: "https://bucket.s3.ap-southeast-1.amazonaws.com/...",
      uploadedAt: "2026-07-07T09:41:28.511"
    }
  ],
  ASSET_FRONT: []
};
```

Voi preview:

- Neu `contentType` la image, hien thi bang `<img src={fileUrl} />`.
- Neu la PDF, hien thi link mo file hoac thumbnail PDF rieng cua FE.
- Neu la video, hien thi bang `<video src={fileUrl} controls />`.

## 5. API upload file len S3

```http
POST /api/v1/loan-applications/{applicationCode}/documents/{documentCode}
Content-Type: multipart/form-data
```

Path params:

| Field | Bat buoc | Mo ta |
| --- | --- | --- |
| `applicationCode` | Yes | Ma ho so vay chinh thuc, vi du `APP-2026-000006` |
| `documentCode` | Yes | Ma danh muc FE gui, vi du `ASSET_FRONT` |

Form data:

| Field | Bat buoc | Kieu | Mo ta |
| --- | --- | --- | --- |
| `files` | Yes | File[] | Dung khi upload mot hoac nhieu file |
| `file` | No | File | Field cu, chi nen dung khi upload mot file |

Frontend nen uu tien dung `files`, ke ca khi chi upload 1 file.

Vi du upload 1 file:

```ts
async function uploadOneDocumentFile(
  applicationCode: string,
  documentCode: string,
  file: File
) {
  const formData = new FormData();
  formData.append("files", file);

  const response = await fetch(
    `/api/v1/loan-applications/${applicationCode}/documents/${documentCode}`,
    {
      method: "POST",
      body: formData
    }
  );

  return response.json();
}
```

Vi du upload nhieu file trong cung mot danh muc:

```ts
async function uploadManyDocumentFiles(
  applicationCode: string,
  documentCode: string,
  files: File[]
) {
  const formData = new FormData();

  for (const file of files) {
    formData.append("files", file);
  }

  const response = await fetch(
    `/api/v1/loan-applications/${applicationCode}/documents/${documentCode}`,
    {
      method: "POST",
      body: formData
    }
  );

  return response.json();
}
```

Response thanh cong:

```json
{
  "success": true,
  "message": "Upload chứng từ hồ sơ vay thành công",
  "data": [
    {
      "applicationCode": "APP-2026-000006",
      "documentCode": "ASSET_FRONT",
      "documentTypeCode": "ASSET_FRONT_IMAGE",
      "fileId": "2f9c261d-94ee-4385-aebb-1728d9883a34",
      "fileName": "asset-front-1.jpg",
      "contentType": "image/jpeg",
      "size": 210000,
      "fileUrl": "https://loan-document-bucket.s3.ap-southeast-1.amazonaws.com/loan-applications/APP-2026-000006/ASSET_FRONT/2f9c261d-94ee-4385-aebb-1728d9883a34.jpg",
      "uploadedAt": "2026-07-07T09:41:28.511300219"
    }
  ],
  "errorCode": null,
  "timestamp": "2026-07-07T09:41:28.600"
}
```

Sau khi upload thanh cong, FE them cac item trong `data[]` vao state theo `documentCode`.

## 6. API xoa file khi bam nut X

```http
DELETE /api/v1/loan-applications/{applicationCode}/documents?fileUrl={fileUrl}
```

Query params:

| Field | Bat buoc | Mo ta |
| --- | --- | --- |
| `fileUrl` | Yes | URL da nhan tu response upload |

Vi du:

```ts
async function deleteUploadedDocument(
  applicationCode: string,
  fileUrl: string
) {
  const params = new URLSearchParams({ fileUrl });

  const response = await fetch(
    `/api/v1/loan-applications/${applicationCode}/documents?${params.toString()}`,
    {
      method: "DELETE"
    }
  );

  return response.json();
}
```

Response thanh cong:

```json
{
  "success": true,
  "message": "Xóa ảnh chứng từ thành công",
  "data": {
    "applicationCode": "APP-2026-000006",
    "fileUrl": "https://loan-document-bucket.s3.ap-southeast-1.amazonaws.com/...",
    "deleted": true,
    "message": "Đã xóa ảnh chứng từ"
  },
  "errorCode": null,
  "timestamp": "2026-07-07T09:42:28.600"
}
```

Sau khi API tra `success = true`, FE remove item co `fileUrl` tu state.

API nay co the xoa:

- File vua upload len S3 nhung chua complete.
- File da complete va da co metadata trong `loan_application_document`.

## 7. API complete upload va luu metadata

```http
POST /api/v1/loan-applications/{applicationCode}/documents/complete
Content-Type: application/json
```

Dung khi user bam nut **Gui di de phe duyet** hoac nut hoan tat upload cua buoc 4.

Request body:

```json
{
  "documents": [
    {
      "documentCode": "ASSET_FRONT",
      "documentTypeCode": "ASSET_FRONT_IMAGE",
      "fileUrl": "https://loan-document-bucket.s3.ap-southeast-1.amazonaws.com/loan-applications/APP-2026-000006/ASSET_FRONT/uuid-1.jpg",
      "fileName": "asset-front-1.jpg",
      "uploadedAt": "2026-07-07T09:41:28.511300219",
      "uploadedBy": "NV001",
      "note": null
    },
    {
      "documentCode": "ASSET_FRONT",
      "documentTypeCode": "ASSET_FRONT_IMAGE",
      "fileUrl": "https://loan-document-bucket.s3.ap-southeast-1.amazonaws.com/loan-applications/APP-2026-000006/ASSET_FRONT/uuid-2.jpg",
      "fileName": "asset-front-2.jpg",
      "uploadedAt": "2026-07-07T09:41:29.100000000",
      "uploadedBy": "NV001",
      "note": null
    }
  ]
}
```

Field trong `documents[]`:

| Field | Bat buoc | Mo ta |
| --- | --- | --- |
| `documentCode` | Yes neu khong gui `documentTypeCode` | Ma danh muc FE, vi du `ASSET_FRONT` |
| `documentTypeCode` | No | Ma loai chung tu BE tra tu API upload. FE nen gui lai field nay de ro rang |
| `fileUrl` | Yes | URL S3 BE tra tu API upload |
| `fileName` | No | Ten file hien thi |
| `uploadedAt` | No | Thoi diem upload BE tra ve |
| `uploadedBy` | No | Ma nhan vien dang thao tac, neu FE co |
| `note` | No | Ghi chu neu co |

Response thanh cong:

```json
{
  "success": true,
  "message": "Hoàn tất upload chứng từ thành công",
  "data": {
    "applicationCode": "APP-2026-000006",
    "documentCount": 2,
    "message": "Đã lưu metadata chứng từ vào hồ sơ vay"
  },
  "errorCode": null,
  "timestamp": "2026-07-07T09:43:28.600"
}
```

Luu y quan trong:

- FE phai gui **toan bo danh sach file dang con tren man hinh** trong `documents[]`.
- Neu mot danh muc co nhieu anh, gui nhieu object cung `documentCode` hoac cung `documentTypeCode`.
- Backend se luu moi anh thanh mot dong trong `loan_application_document`.
- Backend dang xu ly complete theo kieu replace metadata cua ho so: danh sach FE gui len la source of truth.

## 8. Goi API submit sau khi complete

Sau khi complete documents thanh cong, FE co the goi API submit ho so:

```http
POST /api/v1/loan-applications/{applicationCode}/submit-for-approval
```

Khuyen nghi flow nut **Gui di de phe duyet**:

```text
Validate required documents tren FE
-> Goi /documents/complete
-> Neu complete success, goi /submit-for-approval
-> Dieu huong sang man hinh ket qua hoac danh sach ho so
```

## 9. Validation tren frontend truoc khi complete

Frontend nen validate cac document bat buoc theo bang documentCode o muc 2.

Vi du:

```ts
const requiredDocumentCodes = [
  "CITIZEN_ID_FRONT",
  "CITIZEN_ID_BACK",
  "VEHICLE_REGISTRATION_FRONT",
  "VEHICLE_REGISTRATION_BACK",
  "ASSET_FRONT",
  "ASSET_REAR",
  "ASSET_LEFT",
  "ASSET_RIGHT",
  "CUSTOMER_PORTRAIT",
  "CUSTOMER_PORTRAIT_VIDEO"
];

function validateRequiredDocuments(documentsByCode: DocumentUploadState) {
  return requiredDocumentCodes.filter((code) => {
    return !documentsByCode[code] || documentsByCode[code].length === 0;
  });
}
```

Neu ket qua co missing codes, FE chan submit va highlight cac dong tuong ung.

## 10. Xu ly loi thuong gap

Response loi chung:

```json
{
  "success": false,
  "message": "File chứng từ không được vượt quá 5MB",
  "data": null,
  "errorCode": "VALIDATION_ERROR",
  "timestamp": "2026-07-07T09:44:28.600"
}
```

Cac loi FE can hien thi:

| Tinh huong | Nguyen nhan | Cach xu ly FE |
| --- | --- | --- |
| File rong | User chon file loi hoac empty file | Bao user chon lai file |
| File qua 5MB | Vuot gioi han backend | Bao user nen nen anh hoac chon file khac |
| Sai dinh dang | Extension khong nam trong danh sach ho tro | Chan ngay tren FE neu co the |
| Document type mapping not configured | Gui sai `documentCode` | Kiem tra lai mapping o muc 2 |
| Chua cau hinh AWS_S3_BUCKET | Backend/env chua cau hinh S3 | Bao backend/devops, FE khong tu khac phuc duoc |

## 11. API lay lai danh sach documents

Hien tai backend chua co API rieng de lay lai danh sach documents da complete cho ho so.

Neu FE can restore preview sau khi refresh trang, backend nen bo sung:

```http
GET /api/v1/loan-applications/{applicationCode}/documents
```

Response de xuat:

```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "applicationCode": "APP-2026-000006",
      "documentCode": "ASSET_FRONT",
      "documentTypeCode": "ASSET_FRONT_IMAGE",
      "fileName": "asset-front-1.jpg",
      "fileUrl": "https://loan-document-bucket.s3.ap-southeast-1.amazonaws.com/...",
      "uploadedAt": "2026-07-07T09:41:28.511300219",
      "uploadedBy": "NV001",
      "note": null
    }
  ],
  "errorCode": null,
  "timestamp": "2026-07-07T09:45:28.600"
}
```

Trong luc chua co API nay, FE can giu state upload trong man hinh hien tai. Khi user refresh trang, preview cac file chua complete co the mat khoi UI.

## 12. Checklist frontend

- Dung `applicationCode` thay vi `draftCode`.
- Upload bang multipart field `files`.
- Cho phep nhieu file trong cung mot `documentCode`.
- Luu `fileUrl` tu response upload de preview.
- Nut X goi DELETE theo `fileUrl`.
- Nut Gui di de phe duyet goi `/documents/complete` truoc.
- `documents[]` gui len complete phai la toan bo file con lai tren UI.
- Sau complete thanh cong moi goi `/submit-for-approval`.
- Validate required documents, file size va file extension tren FE.
