package com.f88.loanonboarding.dto.response.loan;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.f88.loanonboarding.enums.LoanApplicationState;

public record LoanApplicationDetailResponse(
        UUID applicationId,
        String applicationCode,
        LoanApplicationState applicationState,
        String applicationStateName,
        String currentStepCode,
        String currentStepName,
        LocalDateTime expiredAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String customerCode,
        Map<String, Object> applicantSnapshot,
        Map<String, Object> loanRequest,
        Map<String, Object> assetSnapshot,
        Map<String, Object> valuationPreview,
        Map<String, Object> stepStatus,
        LocalDateTime updatedDate,
        LoanApplicationOnboardingCustomerResponse customer,
        LoanApplicationOnboardingLoanInfoResponse loanInfo,
        LoanApplicationOnboardingAssetResponse asset,
        LoanApplicationOnboardingValuationResponse valuation,
        List<LoanApplicationOnboardingReferencePersonResponse> references,
        List<LoanApplicationDocumentListResponse.DocumentItem> documents
) {
}
