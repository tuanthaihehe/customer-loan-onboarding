package com.f88.loanonboarding.dto.request.draft;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreateLoanApplicationDraftRequest(
        @NotNull(message = "customerId là bắt buộc")
        UUID customerId
) {
}
