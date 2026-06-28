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
service/impl/CustomerServiceMockImpl.java
service/impl/LoanApplicationServiceMockImpl.java
mock/DemoCustomerMockDataProvider.java
mock/DemoLoanApplicationMockDataProvider.java
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
service/impl/AssetServiceMockImpl.java
service/impl/AssetValuationServiceMockImpl.java
mock/DemoAssetMockDataProvider.java
mock/DemoValuationMockDataProvider.java
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
service/impl/EligibilityServiceMockImpl.java
service/impl/ReferenceDataServiceMockImpl.java
mock/DemoEligibilityMockDataProvider.java
mock/DemoReferenceDataMockDataProvider.java
docs/
```

## Quy tắc chung

Không ai tự ý mở rộng sang approval/contract/disbursement full khi chưa có yêu cầu.
