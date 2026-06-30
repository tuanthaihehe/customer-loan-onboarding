# Task Split For 3 DEVs

Tài liệu này chia việc theo scope demo Flow 1.

## DEV 1 - Customer & Loan Application

Phụ trách:

- Customer lookup;
- create draft;
- save draft;
- get detail;
- submit for approval.

File chính:

```text
controller/CustomerController.java
controller/LoanApplicationController.java
service/impl/CustomerServiceDbImpl.java
service/impl/LoanApplicationServiceDbImpl.java
database/customer seed data.java
database/loan_application seed data.java
```

## DEV 2 - Asset & Valuation

Phụ trách:

- asset lookup;
- save asset snapshot;
- valuation preview;
- save valuation preview.

File chính:

```text
controller/AssetController.java
controller/AssetValuationController.java
service/impl/AssetServiceDbImpl.java
service/impl/AssetValuationServiceDbImpl.java
database/asset migration pending.java
database/valuation migration pending.java
```

## DEV 3 - Eligibility, Reference Data, Docs

Phụ trách:

- eligibility check;
- reference data;
- API sample;
- Swagger test checklist;
- docs/handoff.

File chính:

```text
controller/EligibilityController.java
controller/ReferenceDataController.java
service/impl/EligibilityServiceDbImpl.java
service/impl/ReferenceDataServiceDbImpl.java
database/eligibility migration pending.java
database/reference seed data.java
docs/
```

## Quy tắc chung

Không ai tự ý mở rộng sang approval/contract/disbursement full khi chưa có yêu cầu.

