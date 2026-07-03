# Nghiệm thu màn hình 5 - Đề xuất gói vay cuối cùng

Ngày lập: 2026-07-03

Phạm vi tài liệu: mô tả API, luồng xử lý và các công thức tính toán đang dùng cho màn hình 5 "Đề xuất gói vay cuối cùng".

Tài liệu này không thay thế migration/seed của DA/BA. Phần code hiện tại không tạo bảng mới, không sửa `database/migrations`, không sửa `database/seed`, không sửa `src/main/resources/db`.

## 1. File code xử lý chính

Toàn bộ logic màn hình 5 đang nằm trong:

```text
backend/loan-onboarding/src/main/java/com/f88/loanonboarding/service/impl/LoanProductRecommendationServiceImpl.java
```

Các hàm chính:

```text
previewFinalOffer(...)              // load và tính dữ liệu màn 5
selectFinalOffer(...)               // lưu/chốt gói vay cuối cùng
calculateFinalOffer(...)            // gom dữ liệu + tính scoring + lọc gói vay
resolveLatestValuation(...)         // lấy định giá tài sản mới nhất
calculateScoring(...)               // tính score grade mock
toRecommendation(...)               // tính từng gói vay đề xuất
calculateEstimatedMonthlyPayment(...) // tính số tiền trả tháng đầu/ước tính
buildRepaymentSchedule(...)         // tính lịch trả nợ từng kỳ
```

DTO response chính:

```text
backend/loan-onboarding/src/main/java/com/f88/loanonboarding/dto/response/loan/FinalLoanOfferResponse.java
```

Các DTO bổ sung cho màn 5:

```text
FinalOfferCustomerSummaryResponse.java
FinalOfferAssetSummaryResponse.java
AppliedDeductionResponse.java
RepaymentScheduleItemResponse.java
```

## 2. API phục vụ màn hình 5

### 2.1 Load/tính màn hình 5

```http
POST /api/v1/loan-applications/{applicationCode}/final-loan-offer/preview
```

Request mẫu:

```json
{
  "requestedAmount": 10000000,
  "loanTermMonths": 12,
  "paymentMethod": "BANK_TRANSFER",
  "monthlyPaymentDay": 20,
  "processingBranch": "PGD Hoang Mai",
  "limit": 3
}
```

API này dùng để FE hiển thị màn 5. Nếu staff chỉnh số tiền vay hoặc kỳ hạn, FE gọi lại API này để backend tính lại scoring, danh sách gói vay, tóm tắt khoản vay và lịch trả nợ.

### 2.2 Lưu/chốt gói vay cuối cùng

```http
POST /api/v1/loan-applications/{applicationCode}/final-loan-offer/select
```

Request mẫu:

```json
{
  "productCode": "XM_PREFER",
  "requestedAmount": 10000000,
  "loanTermMonths": 12,
  "paymentMethod": "BANK_TRANSFER",
  "monthlyPaymentDay": 20,
  "processingBranch": "PGD Hoang Mai"
}
```

API này validate lại gói vay còn phù hợp, sau đó lưu vào:

```text
loan_application.loan_product_id
loan_application.requested_amount
loan_application.loan_term_months
loan_application.branch
```

Không tạo bảng phụ để lưu gói vay cuối cùng.

## 3. Nguồn dữ liệu màn 5

Backend tự tổng hợp dữ liệu từ database theo `applicationCode`, FE không cần truyền lại toàn bộ dữ liệu các màn trước.

Nguồn chính:

```text
loan_application
customer
asset
asset_valuation
asset_valuation_deduction
asset_deduction_type
mock_score_grade_rule
score_grade
loan_product
loan_product_purpose
loan_product_vehicle_type
loan_product_term
loan_product_score_grade
```

Luồng lấy dữ liệu:

1. Tìm hồ sơ theo `loan_application_code`.
2. Kiểm tra hồ sơ đã có mục đích vay, kỳ hạn, số tiền vay và tài sản.
3. Lấy tài sản từ `loan_application.asset_id`.
4. Lấy bản định giá mới nhất theo asset từ `asset_valuation`.
5. Lấy các yếu tố giảm trừ đã chọn từ `asset_valuation_deduction`.
6. Tính scoring mock từ `mock_score_grade_rule`.
7. Lọc sản phẩm từ `loan_product` và các bảng mapping.
8. Tính số tiền vay đề xuất, khoản vay tối đa, tiền trả tháng đầu.
9. Tạo lịch trả nợ dự kiến.
10. Trả response cho FE.

## 4. Công thức định giá dùng trong màn 5

Màn 5 không tự tính lại giảm trừ từ request. Màn 5 lấy bản định giá đã lưu ở bước trước.

Nguồn:

```text
asset_valuation.market_price_amount
asset_valuation.total_deduction_amount
asset_valuation.final_value_amount
```

Công thức nghiệp vụ:

```text
finalValue = marketValue - totalDeductionAmount
```

Trong màn 5, backend ưu tiên lấy trực tiếp `final_value_amount` từ bản ghi `asset_valuation` mới nhất để đảm bảo nhất quán với bước định giá đã lưu.

Danh sách yếu tố giảm trừ trả về trong response:

```json
"appliedDeductions": [
  {
    "code": "OLD_VEHICLE",
    "name": "Xe cũ",
    "deductionAmount": 3000000,
    "deductionPercent": 6.98
  }
]
```

`deductionPercent` được suy ra để FE dễ hiển thị:

```text
deductionPercent = deductionAmount / marketValue * 100
```

## 5. Công thức scoring

Hiện tại scoring là mock theo bảng DA/BA đã cung cấp:

```text
mock_score_grade_rule
score_grade
```

Công thức LTV dùng để match rule:

```text
ltvPercent = requestedAmount / finalAssetValue * 100
```

Backend tìm rule đầu tiên thỏa:

```text
monthlyIncome nằm trong min/max monthly income
requestedAmount nằm trong min/max requested amount
ltvPercent nằm trong min/max LTV
```

Sau khi match rule:

```text
scoreGrade = matchedRule.scoreGrade.code
```

Score số hiện tại đang map mock:

```text
A -> 95
B -> 80
C -> 65
D -> 50
Khác -> 40
```

Hiện tại B-score hành vi chưa có nguồn dữ liệu thật, nên response đang để:

```text
bScore = 0
bScoreWeight = 0
aScoreWeight = 1
overallScore = aScore
```

Điểm này là mock/demo, chưa phải scoring production.

## 6. Công thức lọc sản phẩm vay

Backend query các sản phẩm active phù hợp theo các điều kiện:

```text
loanPurposeCode
vehicleTypeCode
loanTermMonths
scoreGradeCode
```

Điều kiện lọc:

```text
product.active = true
vehicleType.code = selected vehicle type
loanTerm.termMonths = selected loan term
scoreGrade.code = calculated score grade
product.appliesToAllLoanPurposes = true
    OR product loan purpose = selected loan purpose
```

Nguồn code:

```text
LoanProductRepository.findMatchingProducts(...)
```

## 7. Công thức từng gói vay đề xuất

Với mỗi `loan_product` hợp lệ, backend tính:

### 7.1 Khoản vay tối đa theo LTV

```text
maxLoanByLtv = finalAssetValue * product.maxLtvPercent / 100
```

### 7.2 Khoản vay tối đa thực tế

```text
effectiveMaxLoanAmount = min(maxLoanByLtv, product.maxLoanAmount)
```

### 7.3 Loại sản phẩm không đạt min loan

Nếu:

```text
effectiveMaxLoanAmount < product.minLoanAmount
```

thì loại sản phẩm khỏi danh sách đề xuất.

### 7.4 Số tiền vay đề xuất

```text
suggestedLoanAmount = max(product.minLoanAmount, min(requestedAmount, effectiveMaxLoanAmount))
```

Ý nghĩa:

- Nếu khách muốn vay nằm trong khoảng sản phẩm cho phép thì đề xuất đúng số tiền khách muốn vay.
- Nếu khách muốn vay thấp hơn min loan của sản phẩm thì đề xuất bằng min loan.
- Nếu khách muốn vay cao hơn mức được vay tối đa thì đề xuất bằng mức tối đa thực tế.

### 7.5 Chênh lệch với nhu cầu khách hàng

```text
loanAmountGap = abs(requestedAmount - suggestedLoanAmount)
```

### 7.6 Tiền trả tháng đầu/ước tính

```text
principalPerMonth = suggestedLoanAmount / loanTermMonths
interestPerMonth = suggestedLoanAmount * monthlyInterestRatePercent / 100
estimatedMonthlyPayment = principalPerMonth + interestPerMonth
```

Lưu ý: `estimatedMonthlyPayment` trong product card là tiền trả kỳ đầu/ước tính theo gốc đều và lãi tính trên dư nợ ban đầu.

## 8. Công thức sắp xếp gói vay

Backend sort danh sách gói vay theo thứ tự:

```text
1. loanAmountGap tăng dần
2. estimatedMonthlyPayment tăng dần
3. productCode tăng dần
```

Gói đầu tiên sau khi sort được đánh dấu:

```text
recommended = true
recommendedProductCode = productCode của gói đầu tiên
```

Khi preview màn 5 chưa có gói được chốt, backend dùng gói recommend đầu tiên làm `selectedProductCode` mặc định để FE có dữ liệu tóm tắt và lịch trả nợ ngay.

## 9. Công thức lịch trả nợ

Nguồn code:

```text
buildRepaymentSchedule(...)
```

Mô hình hiện tại:

```text
Gốc đều từng kỳ
Lãi tính trên dư nợ đầu kỳ
Dư nợ giảm dần
```

### 9.1 Gốc mỗi kỳ

```text
principalPerMonth = suggestedLoanAmount / loanTermMonths
```

Các kỳ đầu lấy `principalPerMonth` đã làm tròn. Kỳ cuối lấy toàn bộ dư nợ còn lại để đảm bảo:

```text
sum(principalAmount) = suggestedLoanAmount
endingBalance kỳ cuối = 0
```

### 9.2 Lãi mỗi kỳ

```text
interestAmount = beginningBalance * monthlyInterestRatePercent / 100
```

### 9.3 Tổng phải trả mỗi kỳ

```text
totalPaymentAmount = principalAmount + interestAmount
```

### 9.4 Dư nợ cuối kỳ

```text
endingBalance = beginningBalance - principalAmount
```

### 9.5 Dư nợ đầu kỳ kế tiếp

```text
beginningBalance kỳ sau = endingBalance kỳ trước
```

Response mẫu:

```json
"repaymentSchedule": [
  {
    "period": 1,
    "beginningBalance": 10000000,
    "principalAmount": 833333,
    "interestAmount": 280000,
    "totalPaymentAmount": 1113333,
    "endingBalance": 9166667
  }
]
```

Ý nghĩa field:

```text
period              kỳ trả nợ
beginningBalance    dư nợ đầu kỳ
principalAmount     tiền gốc kỳ đó
interestAmount      tiền lãi kỳ đó
totalPaymentAmount  tổng phải trả kỳ đó
endingBalance       dư nợ cuối kỳ
```

## 10. Tổng tiền trong response

Backend tính thêm:

```text
totalPrincipalAmount = sum(principalAmount)
totalInterestAmount = sum(interestAmount)
totalPaymentAmount = sum(totalPaymentAmount)
```

Điều kiện nghiệm thu:

```text
totalPrincipalAmount = selectedLoanAmount
repaymentSchedule.size = loanTermMonths
repaymentSchedule kỳ cuối endingBalance = 0
```

## 11. Case đã test thành công

Hồ sơ test:

```text
APP-2026-000006
```

Request:

```json
{
  "requestedAmount": 10000000,
  "loanTermMonths": 12,
  "paymentMethod": "BANK_TRANSFER",
  "monthlyPaymentDay": 20,
  "processingBranch": "PGD Hoang Mai",
  "limit": 3
}
```

Kết quả nghiệm thu:

```text
success = true
recommendedProductCode = XM_PREFER
products trả về 3 gói
repaymentSchedule trả về 12 kỳ
totalPrincipalAmount = 10000000
endingBalance kỳ cuối = 0
```

API chốt gói cũng đã test thành công:

```http
POST /api/v1/loan-applications/APP-2026-000006/final-loan-offer/select
```

Body:

```json
{
  "productCode": "XM_PREFER",
  "requestedAmount": 10000000,
  "loanTermMonths": 12,
  "paymentMethod": "BANK_TRANSFER",
  "monthlyPaymentDay": 20,
  "processingBranch": "PGD Hoang Mai"
}
```

Kết quả:

```text
success = true
selectedProductCode = XM_PREFER
selectedLoanAmount = 10000000
repaymentSchedule trả về 12 kỳ
totalPrincipalAmount = 10000000
```

## 12. Ghi chú giới hạn hiện tại

1. `paymentMethod` và `monthlyPaymentDay` hiện được tính/trả trong response, nhưng database hiện chưa có cột riêng để lưu bền vững hai trường này. Chỉ `branch` đang được lưu vào `loan_application.branch`.
2. B-score hiện chưa có nguồn dữ liệu hành vi thật, nên đang để mock bằng 0.
3. Scoring hiện tại dựa vào `mock_score_grade_rule`, chưa phải scoring production.
4. Lịch trả nợ hiện theo mô hình gốc đều, lãi trên dư nợ đầu kỳ. Nếu BA/DA đổi sang dư nợ giảm dần kiểu khác hoặc annuity, cần cập nhật công thức.
