# Frontend Guide - Loan Product Recommendation

Tài liệu này hướng dẫn frontend xử lý đầy đủ luồng đề xuất sản phẩm vay dựa trên các API backend hiện có.

Mục tiêu:

- Biết khi nào gọi API nào.
- Biết input cần truyền là gì.
- Biết field nào dùng để hiển thị lên UI.
- Biết khi user thay đổi dữ liệu thì cần gọi lại API nào.
- Tránh hiểu nhầm giữa giá tài sản gốc, giá tài sản sau định giá, hạn mức sản phẩm, hạn mức theo LTV và số tiền vay đề xuất.

## 1. Tổng quan nghiệp vụ

Luồng đề xuất sản phẩm vay không chạy độc lập. Nó phụ thuộc vào kết quả định giá tài sản.

Flow chuẩn:

```text
Chọn tài sản
-> Lấy giá thị trường
-> Preview định giá tài sản, có hoặc không có giảm trừ
-> Lấy finalValue
-> Gọi API đề xuất sản phẩm vay
-> Hiển thị top sản phẩm phù hợp
-> User chọn một sản phẩm
-> Gọi quote cho sản phẩm được chọn nếu cần tính lại riêng sản phẩm đó
```

Giá trị quan trọng nhất để truyền sang API đề xuất sản phẩm vay là:

```text
adjustedAssetValue = finalValue từ API asset valuation preview
```

Nếu user thay đổi giảm trừ tài sản, `finalValue` thay đổi, vì vậy danh sách sản phẩm vay và số tiền vay đề xuất cũng phải tính lại.

## 2. API liên quan

### 2.1. Lấy giá thị trường của tài sản

```http
GET /api/v1/asset-valuations/market-price?vehicleVariant={vehicleVariant}
```

Dùng khi frontend đã resolve được `vehicleVariant` cuối cùng.

Ví dụ:

```http
GET /api/v1/asset-valuations/market-price?vehicleVariant=YAMAHA_EXCITER_155_ABS_2023_BLUE
```

Mục đích hiển thị:

- Hiển thị giá xe gốc hoặc giá thị trường.
- Đây chưa phải giá tài sản cuối để đề xuất khoản vay nếu có giảm trừ.

### 2.2. Preview định giá tài sản

```http
POST /api/v1/asset-valuations/preview
```

Dùng để tính giá trị tài sản cuối cùng sau giảm trừ.

Request không có giảm trừ:

```json
{
  "assetSnapshot": {
    "assetType": "MOTORBIKE",
    "brand": "YAMAHA",
    "model": "EXCITER_155",
    "vehicleVariant": "YAMAHA_EXCITER_155_ABS_2023_BLUE",
    "manufactureYear": 2023,
    "vehicleColor": "BLUE"
  },
  "deductionItems": []
}
```

Request có giảm trừ:

```json
{
  "assetSnapshot": {
    "assetType": "MOTORBIKE",
    "brand": "YAMAHA",
    "model": "EXCITER_155",
    "vehicleVariant": "YAMAHA_EXCITER_155_ABS_2023_BLUE",
    "manufactureYear": 2023,
    "vehicleColor": "BLUE"
  },
  "deductionItems": [
    {
      "type": "OLD_VEHICLE",
      "rate": 5
    },
    {
      "type": "PHYSICAL_DAMAGE",
      "rate": 3
    }
  ]
}
```

Response quan trọng:

```json
{
  "marketValue": 45000000,
  "totalDeductionRate": 8,
  "totalDeductionAmount": 3600000,
  "finalValue": 41400000,
  "valuationState": "VAL_ACTIVE",
  "appliedDeductionTypes": ["OLD_VEHICLE", "PHYSICAL_DAMAGE"]
}
```

Frontend dùng:

```text
marketValue -> hiển thị giá xe gốc
totalDeductionRate -> hiển thị tổng tỷ lệ giảm trừ
totalDeductionAmount -> hiển thị tổng số tiền giảm trừ
finalValue -> giá trị tài sản sau định giá, truyền sang API đề xuất sản phẩm vay
appliedDeductionTypes -> hiển thị các yếu tố giảm trừ đã áp dụng
```

### 2.3. Đề xuất top sản phẩm vay

```http
POST /api/v1/loan-products/recommendations
```

Request:

```json
{
  "selectedLoanPurpose": "PERSONAL_CONSUMPTION",
  "selectedAssetType": "MOTORBIKE",
  "selectedTenor": 12,
  "requestedLoanAmount": 15000000,
  "adjustedAssetValue": 41400000,
  "scoreGrade": "A"
}
```

Ý nghĩa input:

```text
selectedLoanPurpose -> mục đích vay user đã chọn
selectedAssetType -> loại tài sản, ví dụ MOTORBIKE hoặc CAR
selectedTenor -> kỳ hạn user đang chọn, tính theo tháng
requestedLoanAmount -> số tiền khách muốn vay
adjustedAssetValue -> finalValue từ API preview định giá tài sản
scoreGrade -> hạng điểm nếu đã có, có thể null nếu chưa có score
```

Response:

```json
{
  "recommendedProductCode": "XM_STANDARD",
  "products": [
    {
      "rank": 1,
      "productCode": "XM_STANDARD",
      "productName": "Xe máy tiêu chuẩn",
      "allowedTenors": [6, 12, 18, 24],
      "minLoanAmount": 3000000,
      "maxLoanAmount": 15000000,
      "maxLtvPercent": 60,
      "maxLoanByLtv": 24840000,
      "effectiveMaxLoanAmount": 15000000,
      "suggestedLoanAmount": 15000000,
      "loanAmountGap": 0,
      "monthlyInterestRatePercent": 3.2,
      "principalPerMonth": 1250000,
      "interestPerMonth": 480000,
      "estimatedMonthlyPayment": 1730000,
      "recommended": true
    }
  ]
}
```

### 2.4. Tính quote cho một sản phẩm cụ thể

```http
POST /api/v1/loan-products/{productCode}/quote
```

Dùng khi user chọn một sản phẩm khác trong danh sách hoặc frontend cần tính lại riêng một sản phẩm cụ thể.

Ví dụ:

```http
POST /api/v1/loan-products/XM_FLEX/quote
```

Body giống API recommendation:

```json
{
  "selectedLoanPurpose": "PERSONAL_CONSUMPTION",
  "selectedAssetType": "MOTORBIKE",
  "selectedTenor": 12,
  "requestedLoanAmount": 15000000,
  "adjustedAssetValue": 41400000,
  "scoreGrade": "A"
}
```

Response là một object sản phẩm đã tính quote.

## 3. Field nào dùng để hiển thị lên UI

### 3.1. Card sản phẩm vay

Với mỗi item trong `products`, UI nên hiển thị:

```text
productName
productCode
allowedTenors
minLoanAmount
maxLoanAmount
maxLtvPercent
monthlyInterestRatePercent
suggestedLoanAmount
effectiveMaxLoanAmount
estimatedMonthlyPayment
recommended
```

Gợi ý hiển thị:

```text
Tên sản phẩm: productName
Kỳ hạn hỗ trợ: allowedTenors, ví dụ 6, 12, 18, 24 tháng
Hạn mức sản phẩm: minLoanAmount - maxLoanAmount
LTV tối đa: maxLtvPercent
Số tiền vay đề xuất: suggestedLoanAmount
Số tiền vay tối đa hiện tại: effectiveMaxLoanAmount
Trả hàng tháng dự kiến: estimatedMonthlyPayment
Badge đề xuất: recommended = true
```

### 3.2. Giải thích các field tiền

`maxLoanAmount`:

```text
Hạn mức tối đa cố định của sản phẩm vay.
```

Ví dụ sản phẩm chỉ cho vay tối đa 20 triệu:

```text
maxLoanAmount = 20,000,000
```

`maxLoanByLtv`:

```text
Số tiền tối đa theo giá trị tài sản và LTV.
```

Công thức:

```text
maxLoanByLtv = adjustedAssetValue * maxLtvPercent / 100
```

Ví dụ:

```text
adjustedAssetValue = 41,400,000
maxLtvPercent = 70
maxLoanByLtv = 28,980,000
```

`effectiveMaxLoanAmount`:

```text
Số tiền tối đa thực tế được vay sau khi xét cả hạn mức sản phẩm và LTV.
```

Công thức:

```text
effectiveMaxLoanAmount = min(maxLoanAmount, maxLoanByLtv)
```

Ví dụ:

```text
maxLoanAmount = 20,000,000
maxLoanByLtv = 28,980,000
effectiveMaxLoanAmount = 20,000,000
```

`suggestedLoanAmount`:

```text
Số tiền backend đề xuất cho khách trong sản phẩm đó.
```

Công thức:

```text
suggestedLoanAmount = max(minLoanAmount, min(requestedLoanAmount, effectiveMaxLoanAmount))
```

Ví dụ khách muốn vay 15 triệu:

```text
requestedLoanAmount = 15,000,000
effectiveMaxLoanAmount = 20,000,000
suggestedLoanAmount = 15,000,000
```

Ví dụ khách muốn vay 80 triệu:

```text
requestedLoanAmount = 80,000,000
effectiveMaxLoanAmount = 20,000,000
suggestedLoanAmount = 20,000,000
```

`loanAmountGap`:

```text
Độ lệch giữa số tiền khách muốn vay và số tiền backend đề xuất.
```

Công thức:

```text
loanAmountGap = abs(suggestedLoanAmount - requestedLoanAmount)
```

UI có thể dùng để cảnh báo:

```text
Số tiền có thể vay thấp hơn nhu cầu của khách 60,000,000.
```

## 4. Khi nào gọi API nào

### 4.1. Khi user chọn xong tài sản

Điều kiện:

```text
assetType
brand
model
vehicleVariant
manufactureYear
vehicleColor
```

Frontend gọi:

```http
GET /api/v1/asset-valuations/market-price
POST /api/v1/asset-valuations/preview
```

Sau đó lấy:

```text
preview.finalValue
```

để chuẩn bị gọi recommendation.

### 4.2. Khi user nhập đủ thông tin vay

Điều kiện:

```text
selectedLoanPurpose
selectedAssetType
selectedTenor
requestedLoanAmount
adjustedAssetValue
```

Frontend gọi:

```http
POST /api/v1/loan-products/recommendations
```

Nếu có score thì truyền `scoreGrade`. Nếu chưa có score thì truyền:

```json
"scoreGrade": null
```

### 4.3. Khi user chọn một sản phẩm trong danh sách

Nếu user chỉ chọn sản phẩm và không đổi input nào, frontend có thể dùng ngay item đã có trong `products`.

Nếu cần backend validate/tính lại riêng sản phẩm đó, gọi:

```http
POST /api/v1/loan-products/{productCode}/quote
```

Nên gọi quote khi:

```text
User chọn sản phẩm khác
User đổi kỳ hạn
User đổi số tiền muốn vay
User đổi giảm trừ khiến adjustedAssetValue thay đổi
User có score mới
```

## 5. Khi input thay đổi thì xử lý thế nào

### 5.1. User đổi loại tài sản hoặc xe

Ví dụ đổi:

```text
assetType
brand
model
version
manufactureYear
color
vehicleVariant
```

Frontend cần:

```text
1. Clear giá thị trường cũ
2. Clear preview định giá cũ
3. Clear danh sách sản phẩm đề xuất cũ
4. Gọi lại market-price
5. Gọi lại preview
6. Lấy finalValue mới
7. Gọi lại recommendations nếu thông tin vay đã đủ
```

### 5.2. User thêm, bỏ hoặc đổi giảm trừ

Frontend cần:

```text
1. Gọi lại POST /api/v1/asset-valuations/preview
2. Lấy finalValue mới
3. Set adjustedAssetValue = finalValue mới
4. Gọi lại POST /api/v1/loan-products/recommendations
5. Nếu đang chọn một product cụ thể, gọi lại quote cho product đó
```

Không dùng giá trị recommendation cũ sau khi giảm trừ thay đổi.

### 5.3. User đổi số tiền muốn vay

Frontend cần gọi lại:

```http
POST /api/v1/loan-products/recommendations
```

Nếu user đã chọn sản phẩm cụ thể, cũng nên gọi:

```http
POST /api/v1/loan-products/{productCode}/quote
```

Lý do:

```text
requestedLoanAmount ảnh hưởng trực tiếp suggestedLoanAmount và loanAmountGap.
```

### 5.4. User đổi kỳ hạn vay

Frontend cần gọi lại:

```http
POST /api/v1/loan-products/recommendations
```

Lý do:

```text
selectedTenor dùng để lọc sản phẩm.
selectedTenor cũng dùng để tính principalPerMonth và estimatedMonthlyPayment.
```

Nếu kỳ hạn mới không nằm trong `allowedTenors` của sản phẩm đang chọn, frontend nên bỏ chọn sản phẩm hiện tại và dùng lại sản phẩm recommended mới.

### 5.5. User đổi mục đích vay

Frontend cần gọi lại:

```http
POST /api/v1/loan-products/recommendations
```

Lý do:

```text
selectedLoanPurpose dùng để lọc sản phẩm.
```

### 5.6. User có score mới hoặc đổi score

Frontend cần gọi lại:

```http
POST /api/v1/loan-products/recommendations
```

Lý do:

```text
scoreGrade dùng để lọc sản phẩm nếu có score.
```

Nếu chưa có score:

```json
"scoreGrade": null
```

Backend sẽ bỏ qua điều kiện score.

## 6. Các case UI cần xử lý

### Case 1: Có danh sách sản phẩm

Điều kiện:

```text
products.length > 0
```

UI:

```text
Hiển thị danh sách sản phẩm.
Đánh dấu product có recommended = true.
Default chọn recommendedProductCode nếu chưa có sản phẩm nào được user chọn.
```

### Case 2: Không có sản phẩm phù hợp

Điều kiện:

```text
products.length = 0
```

UI hiển thị:

```text
Không có sản phẩm vay phù hợp với thông tin hiện tại.
```

Gợi ý cho user kiểm tra:

```text
Số tiền muốn vay
Kỳ hạn vay
Mục đích vay
Giá trị tài sản sau định giá
Score nếu có
```

### Case 3: Số tiền đề xuất thấp hơn nhu cầu khách

Điều kiện:

```text
suggestedLoanAmount < requestedLoanAmount
```

UI hiển thị:

```text
Sản phẩm này chỉ hỗ trợ tối đa {suggestedLoanAmount}, thấp hơn nhu cầu vay {requestedLoanAmount}.
```

Hoặc:

```text
Chênh lệch: loanAmountGap
```

### Case 4: Số tiền khách muốn vay nằm trong hạn mức

Điều kiện:

```text
suggestedLoanAmount = requestedLoanAmount
```

UI hiển thị:

```text
Sản phẩm đáp ứng đủ số tiền khách muốn vay.
```

### Case 5: Sản phẩm không hỗ trợ kỳ hạn user chọn

Trường hợp này thường không xuất hiện trong recommendation vì backend đã lọc.

Nhưng nếu user gọi quote trực tiếp cho sản phẩm không phù hợp, backend sẽ trả lỗi nghiệp vụ:

```text
Sản phẩm không hỗ trợ kỳ hạn vay đã chọn.
```

UI cần hiển thị lỗi và yêu cầu user chọn sản phẩm/kỳ hạn khác.

### Case 6: Giá trị tài sản sau giảm trừ quá thấp

Nếu:

```text
effectiveMaxLoanAmount < minLoanAmount
```

backend sẽ loại sản phẩm khỏi recommendation.

Nếu gọi quote riêng cho sản phẩm đó, backend trả lỗi:

```text
Sản phẩm không đạt số tiền vay tối thiểu với giá trị tài sản hiện tại.
```

UI hiển thị:

```text
Giá trị tài sản sau định giá không đủ điều kiện vay tối thiểu cho sản phẩm này.
```

## 7. Gợi ý state frontend

Frontend nên tách state như sau:

```ts
type AssetValuationState = {
  marketValue: number | null;
  deductionItems: Array<{ type: string; rate: number }>;
  totalDeductionAmount: number | null;
  finalValue: number | null;
};

type LoanProductRecommendationState = {
  selectedLoanPurpose: string | null;
  selectedAssetType: "MOTORBIKE" | "CAR" | null;
  selectedTenor: number | null;
  requestedLoanAmount: number | null;
  adjustedAssetValue: number | null;
  scoreGrade: string | null;
  products: LoanProductQuote[];
  recommendedProductCode: string | null;
  selectedProductCode: string | null;
};
```

Quy tắc:

```text
adjustedAssetValue luôn lấy từ assetValuation.finalValue.
Không cho gọi recommendations nếu adjustedAssetValue null.
Không giữ selectedProductCode nếu product đó không còn nằm trong products sau lần recommend mới.
```

## 8. Pseudo flow frontend

```ts
async function recalculateAssetAndProducts() {
  const preview = await previewAssetValuation({
    assetSnapshot,
    deductionItems
  });

  setAssetValuation(preview);

  if (hasEnoughLoanProductInput()) {
    const recommendation = await recommendLoanProducts({
      selectedLoanPurpose,
      selectedAssetType,
      selectedTenor,
      requestedLoanAmount,
      adjustedAssetValue: preview.finalValue,
      scoreGrade
    });

    setProducts(recommendation.products);
    setRecommendedProductCode(recommendation.recommendedProductCode);
    setSelectedProductCode(recommendation.recommendedProductCode);
  }
}
```

Khi user đổi giảm trừ:

```ts
onDeductionChange(nextDeductionItems) {
  setDeductionItems(nextDeductionItems);
  recalculateAssetAndProducts();
}
```

Khi user đổi số tiền vay/kỳ hạn/mục đích/score:

```ts
onLoanInputChange(nextInput) {
  setLoanInput(nextInput);
  recommendLoanProducts(nextInput);
}
```

Khi user chọn sản phẩm khác:

```ts
async function onSelectProduct(productCode: string) {
  setSelectedProductCode(productCode);

  const quote = await quoteLoanProduct(productCode, {
    selectedLoanPurpose,
    selectedAssetType,
    selectedTenor,
    requestedLoanAmount,
    adjustedAssetValue,
    scoreGrade
  });

  setSelectedProductQuote(quote);
}
```

## 9. Checklist cho frontend

Trước khi gọi recommendation:

```text
selectedLoanPurpose có giá trị
selectedAssetType có giá trị
selectedTenor có giá trị
requestedLoanAmount > 0
adjustedAssetValue > 0
```

Khi render product:

```text
Hiển thị productName
Hiển thị allowedTenors
Hiển thị minLoanAmount - maxLoanAmount
Hiển thị maxLtvPercent
Hiển thị suggestedLoanAmount
Hiển thị effectiveMaxLoanAmount
Hiển thị estimatedMonthlyPayment
Hiển thị recommended badge nếu recommended = true
```

Khi input thay đổi:

```text
Đổi tài sản -> preview lại -> recommendation lại
Đổi giảm trừ -> preview lại -> recommendation lại
Đổi số tiền vay -> recommendation lại
Đổi kỳ hạn -> recommendation lại
Đổi mục đích vay -> recommendation lại
Đổi score -> recommendation lại
Chọn sản phẩm khác -> quote lại sản phẩm đó
```
