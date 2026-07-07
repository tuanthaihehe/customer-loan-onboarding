package com.f88.loanonboarding.dto.request.loan;

import com.fasterxml.jackson.databind.JsonNode;

public record CompleteLoanApplicationDraftStepRequest(
        JsonNode payload
) {
}
