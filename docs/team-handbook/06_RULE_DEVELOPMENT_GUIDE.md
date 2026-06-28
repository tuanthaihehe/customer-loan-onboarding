# Rule Development Guide

## 1. Rule hiện tại là gì?

Rule hiện tại là **demo guard**. Nó giúp kiểm soát một số input cơ bản để demo không chạy quá tự do.

Không xem đây là rule engine chính thức.

## 2. Không làm trong giai đoạn này

- Không tạo rule registry.
- Không viết decision table.
- Không cấu hình rule bằng DB.
- Không viết unit test chi tiết cho tất cả rule.

## 3. Có thể làm

Chỉ thêm rule nhỏ nếu phục vụ trực tiếp Flow 1, ví dụ:

- số tiền vay phải lớn hơn 0;
- kỳ hạn phải hợp lệ;
- tài sản phải có loại tài sản và biển số;
- tỷ lệ giảm trừ không âm.

## 4. Cách viết rule đơn giản

Rule nên:

- đọc từ `RuleContext`;
- trả `RuleResult.pass()` hoặc `RuleResult.fail()`;
- không gọi repository;
- không phụ thuộc controller DTO;
- không chứa mock data.

## 5. Sau này khi rule chốt

Khi BA chốt rule, có thể bổ sung:

- unit test rule;
- mapping Action → Rule;
- rule registry;
- config table nếu cần.
