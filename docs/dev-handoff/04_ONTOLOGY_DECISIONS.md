# Ontology Decisions liên quan backend

## 1. Object trung tâm

Trong Flow 1, object trung tâm là:

```text
LoanApplication
```

Các object liên quan trực tiếp trong demo:

| Object | Vai trò trong Flow 1 |
|---|---|
| Customer | Chủ thể vay |
| CustomerIdentifier | Thông tin định danh |
| LoanApplication | Hồ sơ vay trung tâm |
| VehicleAsset | Tài sản được dùng để cầm cố |
| CollateralAsset | Vai trò tài sản trong khoản vay |
| AssetValuation | Kết quả định giá |
| LoanEligibilityCheck | Kết quả kiểm tra điều kiện |
| ApprovalCase | Case phê duyệt mock sinh ra khi submit |

## 2. Điểm dừng của Flow 1

Flow 1 chỉ dừng ở:

```text
LoanApplicationSubmittedForApproval
```

Không xử lý quyết định phê duyệt thật trong scope hiện tại.

## 3. Mapping tư duy BA sang backend

```text
Actor thực hiện Action
→ Action bị kiểm soát bởi guard/rule tối giản
→ Action tác động Object
→ Action sinh Event mock
```

Ví dụ:

```text
Actor: Nhân viên PGD
Action: Submit Loan Application For Approval
Object: LoanApplication
Event: LoanApplicationSubmittedForApproval
Output: ApprovalCase mock được sinh ra
```
