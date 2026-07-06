# DMN Decision Files

Các file trong thư mục này là DMN decision table để import/tạo trên Camunda Web Modeler.

Hiện tại chỉ `customer-age-eligibility.dmn` đã được tích hợp vào code. Các file còn lại là bản chuẩn bị, chưa được backend gọi trực tiếp.

## Recommended Camunda Import Files

Nên ưu tiên copy/import các file gom nhóm dưới đây lên Camunda Web Modeler.

Mỗi file là một DMN Diagram và bên trong có nhiều Decision Table cùng nhóm nghiệp vụ.

| File | DMN Diagram | Decisions |
| --- | --- | --- |
| `customer-rules.dmn` | Customer Rules | `customerAgeEligibility`, `customerBlacklistCheck` |
| `asset-rules.dmn` | Asset Rules | `assetRequiredCheck`, `assetDuplicateCheck` |
| `loan-request-rules.dmn` | Loan Request Rules | `loanPurposeEligibility`, `loanTenureEligibility`, `requestedAmountLimit` |
| `asset-valuation-rules.dmn` | Asset Valuation Rules | `assetValuationDeductionLimit`, `loanableAmountCheck`, `ltvLimitCheck` |
| `loan-product-recommendation-rules.dmn` | Loan Product Recommendation Rules | `loanProductPurposeEligibility`, `loanProductAssetTypeEligibility`, `loanProductTenorEligibility`, `loanProductScoreEligibility`, `loanProductMinAmountEligibility` |

Các file nhỏ bên dưới vẫn được giữ lại để dễ đọc, test riêng từng rule, hoặc copy từng Decision khi cần.

## Customer

| File | Decision ID | Inputs |
| --- | --- | --- |
| `customer-age-eligibility.dmn` | `customerAgeEligibility` | `age` |
| `customer-blacklist-check.dmn` | `customerBlacklistCheck` | `blacklist` |

## Asset

| File | Decision ID | Inputs |
| --- | --- | --- |
| `asset-required-check.dmn` | `assetRequiredCheck` | `assetType`, `licensePlate` |
| `asset-duplicate-check.dmn` | `assetDuplicateCheck` | `duplicatedAsset` |

## Loan Request

| File | Decision ID | Inputs |
| --- | --- | --- |
| `loan-purpose-eligibility.dmn` | `loanPurposeEligibility` | `loanPurpose` |
| `loan-tenure-eligibility.dmn` | `loanTenureEligibility` | `requestedTenure` |
| `requested-amount-limit.dmn` | `requestedAmountLimit` | `requestedAmount` |

## Asset Valuation

| File | Decision ID | Inputs |
| --- | --- | --- |
| `asset-valuation-deduction-limit.dmn` | `assetValuationDeductionLimit` | `marketValue`, `totalDeductionAmount` |
| `loanable-amount-check.dmn` | `loanableAmountCheck` | `assetFinalValue`, `loanableAmount` |
| `ltv-limit-check.dmn` | `ltvLimitCheck` | `ltvRatio` |

## Loan Product Recommendation

| File | Decision ID | Inputs |
| --- | --- | --- |
| `loan-product-purpose-eligibility.dmn` | `loanProductPurposeEligibility` | `loanPurpose`, `appliesToAllLoanPurposes`, `allowedLoanPurposes` |
| `loan-product-asset-type-eligibility.dmn` | `loanProductAssetTypeEligibility` | `assetType`, `allowedAssetTypes` |
| `loan-product-tenor-eligibility.dmn` | `loanProductTenorEligibility` | `requestedTenure`, `allowedTenors` |
| `loan-product-score-eligibility.dmn` | `loanProductScoreEligibility` | `scoreGrade`, `allowedScoreGrades` |
| `loan-product-min-amount-eligibility.dmn` | `loanProductMinAmountEligibility` | `minLoanAmount`, `effectiveMaxLoanAmount` |

## Standard Outputs

Các DMN rule mới đều trả về cùng một cấu trúc:

| Output | Type | Meaning |
| --- | --- | --- |
| `passed` | boolean | `true` nếu rule đạt, `false` nếu bị chặn |
| `reasonCode` | string | Mã rule/lý do fail, `null` nếu đạt |
| `reasonMessage` | string | Nội dung lỗi hiển thị/log, `null` nếu đạt |

## Notes

- Các file DMN không chứa block `DMNDI` layout để tránh lỗi parse khi deploy/import.
- Các message tiếng Việt trong DMN dùng ASCII không dấu để giảm rủi ro encoding khi copy/import.
- Các input dạng danh sách như `allowedLoanPurposes`, `allowedAssetTypes`, `allowedTenors`, `allowedScoreGrades` dùng FEEL `list contains(...)`.
