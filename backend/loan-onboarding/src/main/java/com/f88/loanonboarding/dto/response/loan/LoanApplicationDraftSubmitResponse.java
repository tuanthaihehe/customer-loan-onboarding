package com.f88.loanonboarding.dto.response.loan;

import com.f88.loanonboarding.enums.LoanApplicationDraftStatus;

public record LoanApplicationDraftSubmitResponse(
        String draftCode,
        LoanApplicationDraftStatus status,
        String loanApplicationCode,
        String message
) {
}
