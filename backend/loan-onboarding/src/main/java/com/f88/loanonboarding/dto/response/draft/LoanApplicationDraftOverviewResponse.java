package com.f88.loanonboarding.dto.response.draft;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

public record LoanApplicationDraftOverviewResponse(
        UUID draftId,
        String draftCode,
        UUID customerId,
        LoanApplicationDraftCustomerResponse customer,
        String status,
        String currentStepCode,
        JsonNode currentStepPayload,
        List<LoanApplicationDraftStepResponse> steps
) {
}
