package com.f88.loanonboarding.dto.response.draft;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

public record LoanApplicationDraftStepPayloadResponse(
        UUID draftId,
        String stepCode,
        String status,
        JsonNode payload
) {
}
