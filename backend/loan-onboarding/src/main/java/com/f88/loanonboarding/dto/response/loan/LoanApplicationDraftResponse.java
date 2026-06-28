package com.f88.loanonboarding.dto.response.loan;

import java.time.LocalDateTime;

import com.f88.loanonboarding.enums.LoanApplicationState;

public record LoanApplicationDraftResponse(
        String applicationCode,
        LoanApplicationState applicationState,
        String customerCode,
        LocalDateTime createdDate,
        LocalDateTime lastSavedAt
) {
}
