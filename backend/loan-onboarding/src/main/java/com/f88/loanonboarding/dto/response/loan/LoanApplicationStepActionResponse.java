package com.f88.loanonboarding.dto.response.loan;

import com.f88.loanonboarding.enums.LoanApplicationStepDataStatus;

public record LoanApplicationStepActionResponse(
        String applicationCode,
        String stepCode,
        LoanApplicationStepDataStatus stepStatus,
        String currentStepCode,
        boolean applicationCompleted,
        String message
) {
}
