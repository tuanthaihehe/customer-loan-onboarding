package com.f88.loanonboarding.dto.response.draft;

import com.fasterxml.jackson.databind.JsonNode;

public record LoanApplicationDraftStepResponse(
        String stepCode,
        String stepName,
        int stepOrder,
        String status,
        JsonNode payload
) {
}
