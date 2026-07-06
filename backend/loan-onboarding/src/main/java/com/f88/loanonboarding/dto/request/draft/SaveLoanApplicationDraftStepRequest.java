package com.f88.loanonboarding.dto.request.draft;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.NotNull;

public record SaveLoanApplicationDraftStepRequest(
        @NotNull(message = "payload là bắt buộc")
        JsonNode payload
) {
}
