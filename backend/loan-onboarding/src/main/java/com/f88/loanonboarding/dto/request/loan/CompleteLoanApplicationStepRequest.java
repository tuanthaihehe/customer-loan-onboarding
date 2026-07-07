package com.f88.loanonboarding.dto.request.loan;

import com.fasterxml.jackson.databind.JsonNode;

public record CompleteLoanApplicationStepRequest(
        JsonNode payload
) {
}
