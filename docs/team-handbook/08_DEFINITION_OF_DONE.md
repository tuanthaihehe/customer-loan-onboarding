# Team Definition of Done

## 1. Done cho API trong Flow 1

Một API được xem là done khi:

- có controller endpoint;
- có request/response DTO nếu cần;
- có service method;
- mock data không hard-code rối trong service;
- Swagger chạy được;
- docs/api sample cập nhật;
- Swagger checklist cập nhật.

## 2. Done cho demo project

Project được xem là demo-ready khi:

```text
POST /api/v1/loan-applications/{applicationCode}/submit-for-approval
```

trả:

```text
applicationState = APP_SUBMITTED
approvalCaseCode != null
```

## 3. Không bắt buộc hiện tại

- unit test rule chi tiết;
- controller test chi tiết;
- DB integration;
- rule registry;
- production security.
