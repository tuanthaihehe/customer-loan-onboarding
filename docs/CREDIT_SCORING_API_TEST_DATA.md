# Credit Scoring API Test Data

## API

```http
POST /api/v1/credit-scoring/calculate
```

API này tính score theo rule set đang seed trong database:

```text
BASIC_SCORING_V1
```

Nếu request không truyền `ruleSetCode`, backend mặc định dùng `BASIC_SCORING_V1`.

## Request Mẫu

```json
{
  "monthlyIncomeAmount": 15000000,
  "age": 31,
  "dependentCount": 1
}
```

## Kết Quả Kỳ Vọng

Với dữ liệu seed hiện tại:

```text
income 15,000,000 -> score 70, weight 0.3000 -> weighted 21
age 31 -> score 80, weight 0.4000 -> weighted 32
dependentCount 1 -> score 80, weight 0.3000 -> weighted 24
totalScore = 77
scoreGrade = B
```

Response kỳ vọng:

```json
{
  "success": true,
  "message": "Credit score calculated",
  "data": {
    "ruleSetCode": "BASIC_SCORING_V1",
    "totalScore": 77.00,
    "scoreGrade": "B",
    "scoreGradeLabel": "Tốt",
    "components": [
      {
        "component": "INCOME",
        "inputValue": 15000000,
        "scoreValue": 70.00,
        "weight": 0.3000,
        "weightedScore": 21.00,
        "displayLabel": "Từ 15 đến dưới 20 triệu"
      },
      {
        "component": "AGE",
        "inputValue": 31,
        "scoreValue": 80.00,
        "weight": 0.4000,
        "weightedScore": 32.00,
        "displayLabel": "Từ 30 đến dưới 40 tuổi"
      },
      {
        "component": "DEPENDENT",
        "inputValue": 1,
        "scoreValue": 80.00,
        "weight": 0.3000,
        "weightedScore": 24.00,
        "displayLabel": "1 người phụ thuộc"
      }
    ]
  }
}
```

## Sau Khi Có Score

Lấy `data.scoreGrade` truyền sang API đề xuất sản phẩm vay:

```http
POST /api/v1/loan-products/recommendations
```

Ví dụ:

```json
{
  "selectedLoanPurpose": "PERSONAL_CONSUMPTION",
  "selectedAssetType": "MOTORBIKE",
  "selectedTenor": 12,
  "requestedLoanAmount": 30000000,
  "adjustedAssetValue": 50000000,
  "scoreGrade": "B"
}
```

Nếu score thay đổi do thay đổi thu nhập, tuổi hoặc số người phụ thuộc, frontend nên gọi lại API đề xuất sản phẩm để danh sách sản phẩm vay được lọc lại theo hạng điểm mới.
