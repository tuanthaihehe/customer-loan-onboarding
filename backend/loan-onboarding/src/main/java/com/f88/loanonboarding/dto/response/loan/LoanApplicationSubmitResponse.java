package com.f88.loanonboarding.dto.response.loan;

import com.f88.loanonboarding.enums.LoanApplicationOnboardingStatus;

public record LoanApplicationSubmitResponse(
        String applicationCode,
        com.f88.loanonboarding.enums.LoanApplicationState applicationState,
        LoanApplicationOnboardingStatus status,
        String message
) {
}
