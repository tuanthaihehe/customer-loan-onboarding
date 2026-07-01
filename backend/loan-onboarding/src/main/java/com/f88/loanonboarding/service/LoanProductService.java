package com.f88.loanonboarding.service;

import com.f88.loanonboarding.dto.request.loanproduct.LoanProductRecommendationRequest;
import com.f88.loanonboarding.dto.response.loanproduct.LoanProductDetailResponse;
import com.f88.loanonboarding.dto.response.loanproduct.LoanProductQuoteResponse;
import com.f88.loanonboarding.dto.response.loanproduct.LoanProductRecommendationResponse;

public interface LoanProductService {

    LoanProductRecommendationResponse recommend(LoanProductRecommendationRequest request);

    LoanProductDetailResponse getDetail(String productCode);

    LoanProductQuoteResponse quote(String productCode, LoanProductRecommendationRequest request);
}
