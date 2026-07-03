package com.f88.loanonboarding.dto.response.loan;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record FinalLoanOfferResponse(
        String applicationCode,
        FinalOfferCustomerSummaryResponse customer,
        FinalOfferAssetSummaryResponse asset,
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
        BigDecimal totalPrincipalAmount,
        BigDecimal totalInterestAmount,
        BigDecimal totalPaymentAmount,
        LocalDateTime selectedAt,
        List<RecommendedLoanProductResponse> products,
        List<RepaymentScheduleItemResponse> repaymentSchedule
) {
}
