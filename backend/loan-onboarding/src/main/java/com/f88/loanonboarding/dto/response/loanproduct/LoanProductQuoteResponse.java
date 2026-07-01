package com.f88.loanonboarding.dto.response.loanproduct;

import java.math.BigDecimal;

public record LoanProductQuoteResponse(
        Integer rank,
        String productCode,
        String productName,
        BigDecimal minLoanAmount,
        BigDecimal productMaxLoanAmount,
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
