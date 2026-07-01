package com.f88.loanonboarding.dto.response.loanproduct;

import java.math.BigDecimal;
import java.util.List;

public record LoanProductDetailResponse(
        String productCode,
        String productName,
        boolean appliesToAllLoanPurposes,
        BigDecimal minLoanAmount,
        BigDecimal maxLoanAmount,
        BigDecimal maxLtvPercent,
        BigDecimal monthlyInterestRatePercent,
        List<String> allowedLoanPurposes,
        List<String> allowedAssetTypes,
        List<Integer> allowedTenors,
        List<String> allowedScoreGrades
) {
}
