package com.f88.loanonboarding.dto.response.loan;

import java.time.LocalDateTime;

import com.f88.loanonboarding.enums.LoanApplicationDraftStatus;

public record LoanApplicationDraftSummaryResponse(
        String draftCode,
        LoanApplicationDraftStatus status,
        String customerCode,
        String customerName,
        String phoneNumber,
        String currentStepCode,
        String currentStepName,
        LocalDateTime expiredAt,
        LocalDateTime updatedAt,
        String convertedLoanApplicationCode
) {
}
