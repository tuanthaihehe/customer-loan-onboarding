package com.f88.loanonboarding.dto.response.loan;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.f88.loanonboarding.enums.LoanApplicationDraftStatus;

public record LoanApplicationDraftDetailResponse(
        UUID draftId,
        String draftCode,
        LoanApplicationDraftStatus status,
        String currentStepCode,
        String currentStepName,
        LocalDateTime expiredAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String convertedLoanApplicationCode,
        LoanApplicationDraftCustomerResponse customer,
        List<LoanApplicationDraftStepResponse> steps
) {
}
