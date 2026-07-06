package com.f88.loanonboarding.dto.response.draft;

import java.util.UUID;

public record SaveLoanApplicationDraftStepResponse(
        UUID draftId,
        String savedStepCode,
        String savedStepStatus,
        String currentStepCode,
        String nextStepCode,
        String draftStatus
) {
}
