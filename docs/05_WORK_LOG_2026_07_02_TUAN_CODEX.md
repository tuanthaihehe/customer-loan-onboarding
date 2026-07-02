# Work Log - 2026-07-02 - Tuan + Codex

Tai lieu nay ghi lai phan viec Tuan va Codex da lam trong ngay 2026-07-02, sau khi da merge va tham chieu log cua Khuong_dev.

File nay duoc tao moi de khong ghi de hoac tron noi dung vao:

```text
docs/04_WORK_LOG_2026_07_02.md
```

## 1. Boi canh dau ngay

Trong ngay 2026-07-02, project dang o trang thai sau merge voi nhanh `Khuong_Dev`.

Muc tieu lam viec chinh:

- Doc va giu lai cac thay doi dung tu nhanh Khuong_dev.
- Dong bo database trong `backend/loan-onboarding/src/main/resources/db` voi tai lieu database moi.
- Dam bao Docker PostgreSQL va PostgreSQL local ma pgAdmin dang ket noi deu chay cung version Flyway.
- Tiep tuc code cac API phuc vu luong tao ho so vay nhap theo workflow/dataflow BA/DA.

Rang buoc da tuan thu:

- Khong ghi de `docs/04_WORK_LOG_2026_07_02.md`.
- Khong sua truc tiep `.env`, docker file.
- Khong sua folder `database/migrations` va `database/seed` cua BA/DA.
- Phan backend chay DB that qua Flyway trong `backend/loan-onboarding/src/main/resources/db`.

## 2. Kiem tra va dong bo database resources

### 2.1. Trang thai truoc khi dong bo

Truoc khi dong bo, PostgreSQL local trong pgAdmin va Docker DB co luc khong cung version:

- Docker DB da co 35 bang.
- PostgreSQL local ma pgAdmin dang xem ban dau chua theo kip Docker.
- User thay pgAdmin co it bang hon nen can chay Flyway cho dung DB local.

### 2.2. Migration/seed da bo sung trong resources

Da bo sung va chay cac version moi trong:

```text
backend/loan-onboarding/src/main/resources/db/migration
backend/loan-onboarding/src/main/resources/db/seed
```

Migration/schema:

```text
V18__add_kyc_profile.sql
V19__update_loan_application_product_and_income_source.sql
V20__add_vehicle_registration_number_to_asset.sql
```

Seed:

```text
V11__seed_income_source.sql
```

### 2.3. Noi dung chinh cua V18

Them bang:

```text
kyc_profile
```

Muc dich:

- Luu thong tin KYC cua khach hang.
- Co the gan voi `customer`.
- Co the lien ket voi `loan_application` neu ho so vay da duoc tao.

Quan he:

- `customer` 1 - N `kyc_profile`
- `loan_application` 0/1 - 1 `kyc_profile`

### 2.4. Noi dung chinh cua V19

Dieu chinh `loan_application` de phu hop dataflow chi tiet khach hang va goi vay:

- Bo cac cot snapshot khach hang bi trung voi bang `customer`.
- Them `loan_product_id` vao `loan_application`.
- Tao catalog `income_source`.
- Them `income_source_id` vao `loan_application`.

Ly do:

- Thong tin on dinh cua khach hang nam o `customer`.
- Thong tin rieng theo ho so vay nam o `loan_application`.
- Nguon thu nhap can co catalog rieng de FE dung dropdown va backend validate theo DB.

### 2.5. Noi dung chinh cua V11 seed

Seed danh muc nguon thu nhap:

```text
SALARY
BUSINESS
SELF_EMPLOYED
COMMISSION
DRIVER_INCOME
RENTAL
FAMILY_SUPPORT
PENSION
AGRICULTURE
OTHER
```

### 2.6. Noi dung chinh cua V20

Them cot:

```text
asset.registration_number
```

Ly do:

- Man hinh "Thong tin giay to xe" co truong "So Dang ky xe".
- DB truoc do da co:
  - `frame_number`
  - `engine_number`
  - `registration_issue_date`
- Nhung DB chua co cot rieng de luu "So Dang ky xe".
- Khong the dung nham `license_plate` vi `license_plate` la bien so xe tu buoc thong tin so bo tai san.

Cot moi:

```sql
ALTER TABLE asset
ADD COLUMN IF NOT EXISTS registration_number VARCHAR(100);
```

Them unique index de tranh trung so dang ky xe:

```sql
CREATE UNIQUE INDEX IF NOT EXISTS uq_asset_registration_number
ON asset(registration_number)
WHERE registration_number IS NOT NULL;
```

Luu y:

- V20 chi them cot, khong tao bang moi.
- Vi vay so bang trong pgAdmin van la 35 bang, khong tang len 36.

## 3. Cap nhat DatabaseMigrationConfig

File da cap nhat:

```text
backend/loan-onboarding/src/main/java/com/f88/loanonboarding/config/DatabaseMigrationConfig.java
```

Da them buoc chay Flyway den V20 va seed V11:

```java
migrate(dataSource, MIGRATION_LOCATION, MIGRATION_HISTORY_TABLE, "20", false);
migrate(dataSource, SEED_LOCATION, SEED_HISTORY_TABLE, "11", true);
```

Muc dich:

- Backend khi chay profile `db` se tu dong apply schema/seed moi.
- Docker DB va PostgreSQL local pgAdmin co the dong bo cung version.

## 4. Dong bo Docker DB va pgAdmin local

### 4.1. Docker

Da build lai backend Docker:

```text
docker compose up -d --build backend
```

Ket qua container:

```text
los-backend   Up   0.0.0.0:8080->8080
los-postgres  Up healthy 0.0.0.0:5432->5432
```

Kiem tra Docker DB:

```sql
select count(*) as docker_tables
from information_schema.tables
where table_schema='public'
  and table_type='BASE TABLE';
```

Ket qua:

```text
docker_tables = 35
```

Kiem tra Flyway Docker:

```text
latest schema migration = V20__add_vehicle_registration_number_to_asset.sql
latest seed migration   = V11__seed_income_source.sql
```

Kiem tra cot trong bang `asset`:

```text
engine_number
frame_number
registration_issue_date
registration_number
```

### 4.2. PostgreSQL local ma pgAdmin dang xem

Da chay backend local tro vao DB:

```text
jdbc:postgresql://localhost:5432/loan_onboarding
```

Bang account local:

```text
username = postgres
password = 123456
```

Chay tam backend local tren port:

```text
18080
```

Ket qua:

```text
Successfully applied 1 migration to schema "public", now at version v20
```

Sau khi apply xong da dung process local nay de khong chiem port.

Kiem tra lai bang `psql` PostgreSQL 18 local:

```text
local_tables = 35
latest schema migration = V20__add_vehicle_registration_number_to_asset.sql
```

Cot trong bang `asset` local cung da co:

```text
engine_number
frame_number
registration_issue_date
registration_number
```

Ket luan:

- Docker DB da dong bo V20.
- DB local pgAdmin da dong bo V20.
- pgAdmin van hien 35 bang la dung vi V20 chi them cot, khong them bang.

## 5. API thong tin chi tiet khach hang

### 5.1. API luu thong tin chi tiet khach hang

Endpoint:

```http
PATCH /api/v1/loan-applications/{applicationCode}/customer-detail
```

File chinh:

```text
backend/loan-onboarding/src/main/java/com/f88/loanonboarding/controller/LoanApplicationController.java
backend/loan-onboarding/src/main/java/com/f88/loanonboarding/service/impl/LoanApplicationServiceImpl.java
backend/loan-onboarding/src/main/java/com/f88/loanonboarding/dto/request/loan/SaveCustomerDetailRequest.java
backend/loan-onboarding/src/main/java/com/f88/loanonboarding/dto/response/loan/CustomerDetailResponse.java
```

Muc dich:

- Luu cac thong tin chi tiet khach hang o buoc "Chi tiet khach hang".
- Cap nhat thong tin on dinh cua khach hang vao `customer`.
- Cap nhat thong tin theo tung ho so vay vao `loan_application`.

Nhom truong customer:

```text
gender
email
marital_status
permanent_address
```

Nhom truong loan_application:

```text
occupation_id
income_source_id
monthly_income_amount
disbursement_bank_id
disbursement_account_number
disbursement_account_name
workplace_name
workplace_address
current_address
```

Validation:

- Chi cho luu khi ho so dang o trang thai `APP_DRAFT`.
- Validate gioi tinh: `MALE`, `FEMALE`.
- Validate tinh trang hon nhan: `SINGLE`, `MARRIED`.
- Validate nghe nghiep theo catalog `occupation`.
- Validate nguon thu nhap theo catalog `income_source`.
- Validate ngan hang giai ngan theo catalog `bank`.

## 6. API nguoi tham chieu

### 6.1. API luu danh sach nguoi tham chieu

Endpoint:

```http
PUT /api/v1/loan-applications/{applicationCode}/reference-persons
```

File chinh:

```text
backend/loan-onboarding/src/main/java/com/f88/loanonboarding/controller/LoanApplicationController.java
backend/loan-onboarding/src/main/java/com/f88/loanonboarding/service/impl/LoanApplicationServiceImpl.java
backend/loan-onboarding/src/main/java/com/f88/loanonboarding/dto/request/loan/SaveReferencePersonsRequest.java
backend/loan-onboarding/src/main/java/com/f88/loanonboarding/dto/request/loan/ReferencePersonRequest.java
backend/loan-onboarding/src/main/java/com/f88/loanonboarding/dto/response/loan/ReferencePersonsResponse.java
backend/loan-onboarding/src/main/java/com/f88/loanonboarding/dto/response/loan/ReferencePersonResponse.java
backend/loan-onboarding/src/main/java/com/f88/loanonboarding/entity/LoanApplicationReferencePerson.java
```

Muc dich:

- Luu danh sach nguoi tham chieu cho mot ho so vay.
- Phu hop UI yeu cau toi thieu 3 nguoi tham chieu.

Logic luu:

- Tim ho so theo `applicationCode`.
- Chi cho luu khi ho so dang `APP_DRAFT`.
- Xoa danh sach nguoi tham chieu cu cua ho so.
- Luu lai danh sach moi.

Validation:

- So luong nguoi tham chieu theo DTO hien tai.
- Khong cho trung so dien thoai nguoi tham chieu trong cung mot ho so.
- Validate moi quan he theo danh sach:

```text
FATHER
MOTHER
SPOUSE
SIBLING
RELATIVE
FRIEND
COLLEAGUE
OTHER
```

## 7. API reference data phuc vu dropdown

Da bo sung hoac kiem tra cac API dropdown can cho FE:

```http
GET /api/v1/reference-data/genders
GET /api/v1/reference-data/marital-statuses
GET /api/v1/reference-data/occupations
GET /api/v1/reference-data/income-sources
GET /api/v1/reference-data/banks
GET /api/v1/reference-data/reference-person-relationships
GET /api/v1/reference-data/loan-purposes
GET /api/v1/reference-data/loan-terms
GET /api/v1/reference-data/asset-types
GET /api/v1/reference-data/vehicle-brands
GET /api/v1/reference-data/vehicle-models
GET /api/v1/reference-data/vehicle-versions
GET /api/v1/reference-data/manufacture-years
GET /api/v1/reference-data/vehicle-colors
GET /api/v1/reference-data/vehicle-variant
GET /api/v1/reference-data/valuation-deduction-factors
```

Muc dich:

- FE khong hardcode danh muc.
- Backend tra danh muc tu DB that voi cac bang seed cua BA/DA.
- Cac dropdown xe duoc filter theo luong chon: hang xe -> dong xe -> phien ban -> nam san xuat -> mau xe -> variant cuoi cung.

## 8. API thong tin phap ly xe va thong tin giay to xe

### 8.1. Boi canh

User gui dataflow:

```text
C:\Users\Admin\Downloads\Dataflow bo sung thong tin tai san.drawio.xml
```

Va workflow:

```text
C:\Users\Admin\Downloads\workFlow_man hinh chi tiet khach hang + man hinh chi tiet tai san + man hinh de xuat goi vay cuoi cung.drawio.xml
```

Man hinh "Chi tiet tai san" gom:

- Thong tin phap ly xe - Thong tin chung.
- Thong tin phap ly xe - Thong tin chi tiet.
- Thong tin giay to xe.

Phan "Thong tin chung" la du lieu da co tu buoc truoc:

```text
assetType
licensePlate
brand
model
manufactureYear
vehicleColor
deduction factors da chon
```

Phan can luu moi:

```text
frameNumber
engineNumber
registrationNumber
registrationIssueDate
```

### 8.2. API luu thong tin phap ly xe

Endpoint:

```http
PATCH /api/v1/loan-applications/{applicationCode}/asset-legal-info
```

Request:

```json
{
  "frameNumber": "FRAME114124",
  "engineNumber": "ENGINE114124"
}
```

Response thanh cong:

```json
{
  "success": true,
  "message": "Luu thong tin phap ly xe thanh cong",
  "data": {
    "applicationCode": "APP-2026-000014",
    "assetCode": "AST-2026-000006",
    "assetType": "MOTORBIKE",
    "licensePlate": "TEST104007",
    "brand": "YAMAHA",
    "model": "EXCITER_155",
    "vehicleVariant": "YAMAHA_EXCITER_155_ABS_2023_BLUE",
    "manufactureYear": 2023,
    "vehicleColor": "BLUE",
    "frameNumber": "FRAME114124",
    "engineNumber": "ENGINE114124",
    "registrationNumber": null,
    "registrationIssueDate": null
  }
}
```

Logic:

- Tim `loan_application` theo `applicationCode`.
- Chi cho luu khi ho so dang `APP_DRAFT`.
- Bat buoc ho so da co `asset`.
- Chuan hoa `frameNumber`, `engineNumber` ve uppercase.
- Kiem tra trung `frame_number`, `engine_number` voi asset khac.
- Luu vao bang `asset`.

### 8.3. API luu thong tin giay to xe

Endpoint:

```http
PATCH /api/v1/loan-applications/{applicationCode}/vehicle-registration
```

Request:

```json
{
  "registrationNumber": "REG114124",
  "registrationIssueDate": "2026-07-02"
}
```

Response thanh cong:

```json
{
  "success": true,
  "message": "Luu thong tin giay to xe thanh cong",
  "data": {
    "applicationCode": "APP-2026-000014",
    "assetCode": "AST-2026-000006",
    "assetType": "MOTORBIKE",
    "licensePlate": "TEST104007",
    "brand": "YAMAHA",
    "model": "EXCITER_155",
    "vehicleVariant": "YAMAHA_EXCITER_155_ABS_2023_BLUE",
    "manufactureYear": 2023,
    "vehicleColor": "BLUE",
    "frameNumber": "FRAME114124",
    "engineNumber": "ENGINE114124",
    "registrationNumber": "REG114124",
    "registrationIssueDate": "2026-07-02"
  }
}
```

Logic:

- Tim `loan_application` theo `applicationCode`.
- Chi cho luu khi ho so dang `APP_DRAFT`.
- Bat buoc ho so da co `asset`.
- Chuan hoa `registrationNumber` ve uppercase.
- Kiem tra trung `registration_number` voi asset khac.
- Luu `registration_number` va `registration_issue_date` vao bang `asset`.

### 8.4. File code chinh cua phan tai san

Controller:

```text
backend/loan-onboarding/src/main/java/com/f88/loanonboarding/controller/AssetController.java
```

Service:

```text
backend/loan-onboarding/src/main/java/com/f88/loanonboarding/service/AssetService.java
backend/loan-onboarding/src/main/java/com/f88/loanonboarding/service/impl/AssetServiceImpl.java
```

Repository:

```text
backend/loan-onboarding/src/main/java/com/f88/loanonboarding/repository/AssetRepository.java
```

Entity:

```text
backend/loan-onboarding/src/main/java/com/f88/loanonboarding/entity/Asset.java
```

DTO:

```text
backend/loan-onboarding/src/main/java/com/f88/loanonboarding/dto/request/asset/SaveAssetLegalInfoRequest.java
backend/loan-onboarding/src/main/java/com/f88/loanonboarding/dto/request/asset/SaveVehicleRegistrationRequest.java
backend/loan-onboarding/src/main/java/com/f88/loanonboarding/dto/response/asset/AssetLegalInfoResponse.java
```

## 9. Ket qua test API thuc te

Da test qua backend Docker:

```text
http://localhost:8080
```

Ho so dung de test:

```text
APP-2026-000014
```

Asset gan voi ho so:

```text
AST-2026-000006
licensePlate = TEST104007
```

### 9.1. Test API luu thong tin phap ly xe

Request:

```json
{
  "frameNumber": "FRAME114124",
  "engineNumber": "ENGINE114124"
}
```

Ket qua:

```text
success = true
message = Luu thong tin phap ly xe thanh cong
```

### 9.2. Test API luu thong tin giay to xe

Request:

```json
{
  "registrationNumber": "REG114124",
  "registrationIssueDate": "2026-07-02"
}
```

Ket qua:

```text
success = true
message = Luu thong tin giay to xe thanh cong
```

### 9.3. Kiem tra DB sau test

Query:

```sql
select
    la.loan_application_code,
    a.frame_number,
    a.engine_number,
    a.registration_number,
    a.registration_issue_date
from loan_application la
join asset a on a.id = la.asset_id
where la.loan_application_code = 'APP-2026-000014';
```

Ket qua:

```text
loan_application_code = APP-2026-000014
frame_number = FRAME114124
engine_number = ENGINE114124
registration_number = REG114124
registration_issue_date = 2026-07-02
```

## 10. Verification da chay

### 10.1. Kiem tra conflict marker

Lenh:

```text
rg -n "^(<<<<<<<|=======|>>>>>>>)" .
```

Ket qua:

```text
Khong tim thay conflict marker
```

### 10.2. Kiem tra whitespace diff

Lenh:

```text
git diff --check
```

Ket qua:

```text
Khong co loi whitespace.
Chi co warning LF se duoc Git doi sang CRLF o mot so file tren Windows.
```

### 10.3. Maven test

Lenh:

```text
./mvnw.cmd test "-Dapp.database.migration.enabled=false"
```

Ket qua:

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### 10.4. Docker build/run

Lenh:

```text
docker compose up -d --build backend
```

Ket qua:

```text
los-backend started
los-postgres healthy
```

## 11. Trang thai hien tai

### Da xong

- DB Docker da len V20.
- DB local pgAdmin da len V20.
- Bang van la 35 bang, dung voi thiet ke hien tai.
- Da co cot `registration_number` trong `asset`.
- Da co API luu thong tin chi tiet khach hang.
- Da co API luu nguoi tham chieu.
- Da co API luu thong tin phap ly xe.
- Da co API luu thong tin giay to xe.
- Backend Docker dang chay tren port 8080.
- Test Maven pass.
- Test API tai san moi pass.

### Can luu y tiep

- Neu BA/DA muon folder `database/migrations` la nguon chinh tuyet doi, can yeu cau BA/DA bo sung migration tuong duong V20 cho cot `asset.registration_number`.
- V20 hien dang nam trong resources cua backend de backend chay duoc feature theo dataflow hien tai.
- Cac message moi trong API tai san dang dung tieng Viet khong dau de tranh loi encoding hien tai trong source.
- Mot so file cu trong project dang bi hien thi mojibake tieng Viet, can co dot rieng de chuan hoa encoding neu team muon sua dong bo.

## 12. Cap nhat sau khi BA/DA merge them V20

Sau khi merge them tai lieu database moi, BA/DA da bo sung migration:

```text
database/migrations/V20__add_registration_certificate_number_to_asset.sql
```

Migration nay chot ten cot:

```text
asset.registration_certificate_number
```

Vi vay resources da duoc dieu chinh lai de dong bo voi BA/DA:

```text
backend/loan-onboarding/src/main/resources/db/migration/V20__add_registration_certificate_number_to_asset.sql
```

File V20 cu do Codex tao truoc do da bi loai bo:

```text
backend/loan-onboarding/src/main/resources/db/migration/V20__add_vehicle_registration_number_to_asset.sql
```

Code JPA cung da doi mapping:

```text
Asset.registrationNumber -> @Column(name = "registration_certificate_number")
```

API request/response van giu field JSON:

```text
registrationNumber
```

Ly do giu field JSON:

- FE dang goi theo nghia nghiep vu "so dang ky xe".
- Ten cot DB theo BA/DA la `registration_certificate_number`.
- Backend map trung gian nen FE khong can doi payload.

Luu y quan trong:

- Docker/local DB truoc do co the da apply V20 cu voi cot `registration_number`.
- Sau khi source da doi theo BA/DA, database dev cu can reset hoac Flyway repair truoc khi chay lai migration.
- Tren database reset moi, V20 se tao dung cot `registration_certificate_number`.
