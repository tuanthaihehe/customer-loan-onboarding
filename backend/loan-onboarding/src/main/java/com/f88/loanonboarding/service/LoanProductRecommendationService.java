package com.f88.loanonboarding.service;

import com.f88.loanonboarding.dto.request.loan.LoanProductRecommendationRequest;
import com.f88.loanonboarding.dto.request.loan.FinalLoanOfferPreviewRequest;
import com.f88.loanonboarding.dto.request.loan.SelectFinalLoanOfferRequest;
import com.f88.loanonboarding.dto.response.loan.FinalLoanOfferResponse;
import com.f88.loanonboarding.dto.response.loan.LoanProductRecommendationResponse;

public interface LoanProductRecommendationService {

    LoanProductRecommendationResponse recommend(String applicationCode, LoanProductRecommendationRequest request);

    FinalLoanOfferResponse previewFinalOffer(String applicationCode, FinalLoanOfferPreviewRequest request);

    FinalLoanOfferResponse selectFinalOffer(String applicationCode, SelectFinalLoanOfferRequest request);
}
