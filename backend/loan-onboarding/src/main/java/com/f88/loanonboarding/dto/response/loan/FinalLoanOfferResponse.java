package com.f88.loanonboarding.dto.response.loan;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record FinalLoanOfferResponse(
        String applicationCode,
        BigDecimal requestedAmount,
        Integer loanTermMonths,
        String paymentMethod,
        Integer monthlyPaymentDay,
        String processingBranch,
        LoanScoringResponse scoring,
        LoanProductValuationSummaryResponse valuation,
        String recommendedProductCode,
        String selectedProductCode,
        BigDecimal selectedLoanAmount,
        BigDecimal estimatedMonthlyPayment,
        LocalDateTime selectedAt,
        List<RecommendedLoanProductResponse> products
) {
}
