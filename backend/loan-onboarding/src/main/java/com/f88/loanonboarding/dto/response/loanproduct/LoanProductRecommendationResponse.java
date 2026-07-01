package com.f88.loanonboarding.dto.response.loanproduct;

import java.util.List;

public record LoanProductRecommendationResponse(
        String recommendedProductCode,
        List<LoanProductQuoteResponse> products
) {
}
