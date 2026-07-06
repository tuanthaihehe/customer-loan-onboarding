package com.f88.loanonboarding.dto.response.loanproduct;

import java.math.BigDecimal;
import java.util.List;

public record LoanProductQuoteResponse(
        Integer rank,
        String productCode,
        String productName,
        List<Integer> allowedTenors,
        BigDecimal minLoanAmount,
        BigDecimal maxLoanAmount,
        BigDecimal maxLtvPercent,
        BigDecimal maxLoanByLtv,
        BigDecimal effectiveMaxLoanAmount,
        BigDecimal suggestedLoanAmount,
        BigDecimal loanAmountGap,
        BigDecimal monthlyInterestRatePercent,
        BigDecimal principalPerMonth,
        BigDecimal interestPerMonth,
        BigDecimal estimatedMonthlyPayment,
        boolean recommended
) {
}
