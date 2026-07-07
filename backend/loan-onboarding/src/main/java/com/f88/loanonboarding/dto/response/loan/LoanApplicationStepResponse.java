package com.f88.loanonboarding.dto.response.loan;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonNode;
import com.f88.loanonboarding.enums.LoanApplicationStepDataStatus;

public record LoanApplicationStepResponse(
        String stepCode,
        String stepName,
        int stepOrder,
        LoanApplicationStepDataStatus status,
        boolean requiresReview,
        String invalidatedByStepCode,
        String invalidatedReason,
        LocalDateTime completedAt,
        LocalDateTime reviewedAt,
        LocalDateTime updatedAt,
        JsonNode payload
) {
}
