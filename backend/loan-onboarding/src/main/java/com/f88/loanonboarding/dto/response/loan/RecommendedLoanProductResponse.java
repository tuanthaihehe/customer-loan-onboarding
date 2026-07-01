package com.f88.loanonboarding.dto.response.loan;

import java.math.BigDecimal;
import java.util.List;

public record RecommendedLoanProductResponse(
        String productCode,
        String productName,
        BigDecimal minLoanAmount,
        BigDecimal maxLoanAmount,
        BigDecimal maxLtvPercent,
        BigDecimal monthlyInterestRatePercent,
        List<Integer> supportedTermMonths,
        BigDecimal maxLoanByLtv,
        BigDecimal effectiveMaxLoanAmount,
        BigDecimal suggestedLoanAmount,
        BigDecimal loanAmountGap,
        BigDecimal estimatedMonthlyPayment,
        boolean recommended
) {
}
