# Rule Skeleton / Demo Guard

## 1. Cách hiểu đúng

Rule hiện tại trong project là **rule skeleton ở mức demo guard**, không phải business rule production cuối cùng.

Mục tiêu hiện tại:

```text
Có chỗ đặt rule, chứng minh service không xử lý bừa bãi, và một số input sai cơ bản có thể bị chặn.
```

Không đặt mục tiêu:

```text
Xây rule engine hoàn chỉnh, rule registry, config table, hoặc test toàn bộ rule nghiệp vụ.
```

## 2. Vì sao chưa cần unit test rule chi tiết?

Team chưa chốt đầy đủ rule nghiệp vụ. Nếu viết unit test chi tiết lúc này, test sẽ dễ bị sửa liên tục khi BA/mentor đổi rule.

Hiện tại chỉ cần:

- rule class tồn tại;
- service có gọi rule ở một số điểm quan trọng;
- Swagger test được case pass/fail cơ bản;
- tài liệu ghi rõ rule chưa phải bản chính thức.

## 3. Vì sao chưa cần rule registry?

Rule registry chỉ phù hợp khi hệ thống có nhiều action/rule đã ổn định.

Demo Flow 1 hiện chỉ cần kiểm soát tối giản:

| Action | Guard hiện tại |
|---|---|
| Customer lookup | Kiểm tra tuổi/blacklist giả lập |
| Save loan draft | Kiểm tra số tiền, kỳ hạn, mục đích vay |
| Asset lookup/save | Kiểm tra thông tin tài sản bắt buộc/trùng giả lập |
| Valuation preview | Kiểm tra LTV/loanable amount cơ bản |
| Submit for approval | Demo endpoint kết thúc Flow 1, chưa chạy approval rule thật |

Do đó chưa cần rule registry/rule engine.

## 4. Cấu trúc hiện tại

| Thành phần | Vai trò |
|---|---|
| `BusinessRule` | Interface chung cho rule |
| `RuleContext` | Context truyền vào rule |
| `RuleResult` | Kết quả pass/fail/warning |
| `RuleEvaluationService` | Chạy danh sách rule được service truyền vào |
| `rule/customer` | Rule mẫu cho customer |
| `rule/loan` | Rule mẫu cho hồ sơ vay |
| `rule/asset` | Rule mẫu cho tài sản |
| `rule/valuation` | Rule mẫu cho định giá |

## 5. Nguyên tắc khi thêm rule trong giai đoạn demo

Chỉ thêm rule nếu rule đó phục vụ trực tiếp Flow 1.

Không thêm rule phức tạp cho:

- approval decision;
- contract;
- disbursement;
- role/permission nâng cao;
- config table;
- decision table.

## 6. Khi nào mới nên bổ sung unit test/rule registry?

Chỉ làm sau khi có đủ điều kiện:

```text
- BA đã chốt business rule.
- API/DTO ổn định.
- Mentor xác nhận cần kiểm thử rule tự động.
- Flow mở rộng vượt khỏi demo Flow 1.
```

Khi đó mới bổ sung:

- unit test cho từng rule;
- controller test chi tiết;
- rule registry;
- rule mapping từ Action → Rule.
