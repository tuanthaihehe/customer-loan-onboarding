package com.f88.loanonboarding.service;

import com.f88.loanonboarding.dto.request.loan.LoanProductRecommendationRequest;
import com.f88.loanonboarding.dto.response.loan.LoanProductRecommendationResponse;

public interface LoanProductRecommendationService {

    LoanProductRecommendationResponse recommend(String applicationCode, LoanProductRecommendationRequest request);
}
