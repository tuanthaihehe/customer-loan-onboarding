package com.f88.loanonboarding.dto.request.loan;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

public record CreateLoanApplicationOnboardingRequest(
        UUID customerId,
        String customerCode,
        JsonNode customerIdentifyPayload
) {
}
