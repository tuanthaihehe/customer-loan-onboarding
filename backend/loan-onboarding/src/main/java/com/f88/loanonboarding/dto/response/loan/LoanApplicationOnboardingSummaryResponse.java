package com.f88.loanonboarding.dto.response.loan;

import java.time.LocalDateTime;

import com.f88.loanonboarding.enums.LoanApplicationOnboardingStatus;

public record LoanApplicationOnboardingSummaryResponse(
        String applicationCode,
        com.f88.loanonboarding.enums.LoanApplicationState applicationState,
        LoanApplicationOnboardingStatus status,
        String customerCode,
        String customerName,
        String phoneNumber,
        String currentStepCode,
        String currentStepName,
        LocalDateTime expiredAt,
        LocalDateTime updatedAt
) {
}
