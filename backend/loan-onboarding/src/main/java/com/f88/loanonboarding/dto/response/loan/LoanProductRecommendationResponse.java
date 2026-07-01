package com.f88.loanonboarding.dto.response.loan;

import java.math.BigDecimal;
import java.util.List;

public record LoanProductRecommendationResponse(
        String applicationCode,
        String loanPurpose,
        String assetType,
        Integer requestedTermMonths,
        BigDecimal requestedAmount,
        String scoreGrade,
        LoanProductValuationSummaryResponse valuation,
        String recommendedProductCode,
        List<RecommendedLoanProductResponse> products
) {
}
