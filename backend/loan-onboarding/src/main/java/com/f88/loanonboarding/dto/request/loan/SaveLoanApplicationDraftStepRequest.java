package com.f88.loanonboarding.dto.request.loan;

import com.fasterxml.jackson.databind.JsonNode;
import com.f88.loanonboarding.enums.LoanApplicationDraftStepStatus;

public record SaveLoanApplicationDraftStepRequest(
        LoanApplicationDraftStepStatus status,
        JsonNode payload
) {
}
