package com.f88.loanonboarding.dto.response.loan;

import com.f88.loanonboarding.enums.LoanApplicationDraftStepStatus;

public record LoanApplicationDraftStepActionResponse(
        String draftCode,
        String stepCode,
        LoanApplicationDraftStepStatus stepStatus,
        String currentStepCode,
        boolean draftCompleted,
        String message
) {
}
