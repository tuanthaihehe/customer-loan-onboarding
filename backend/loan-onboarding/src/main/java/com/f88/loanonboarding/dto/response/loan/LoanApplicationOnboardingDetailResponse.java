package com.f88.loanonboarding.dto.response.loan;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.f88.loanonboarding.enums.LoanApplicationOnboardingStatus;

public record LoanApplicationOnboardingDetailResponse(
        UUID applicationId,
        String applicationCode,
        com.f88.loanonboarding.enums.LoanApplicationState applicationState,
        LoanApplicationOnboardingStatus status,
        String currentStepCode,
        String currentStepName,
        LocalDateTime expiredAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LoanApplicationOnboardingCustomerResponse customer,
        List<LoanApplicationStepResponse> steps,
        LoanApplicationOnboardingLoanInfoResponse loanInfo,
        LoanApplicationOnboardingAssetResponse asset,
        LoanApplicationOnboardingValuationResponse valuation,
        List<LoanApplicationOnboardingReferencePersonResponse> references,
        List<LoanApplicationDocumentListResponse.DocumentItem> documents
) {
}
