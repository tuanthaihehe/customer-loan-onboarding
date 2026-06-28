package com.f88.loanonboarding.dto.response.loan;

import java.time.LocalDateTime;

import com.f88.loanonboarding.enums.EligibilityResult;

public record EligibilityCheckResponse(
        String eligibilityCheckCode,
        String applicationCode,
        String checklistState,
        EligibilityResult eligibilityResult,
        int totalItemCount,
        int completedItemCount,
        int missingItemCount,
        int failedItemCount,
        LocalDateTime checkedDate
) {
}
