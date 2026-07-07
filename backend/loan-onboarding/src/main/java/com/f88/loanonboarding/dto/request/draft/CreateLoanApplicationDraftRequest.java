package com.f88.loanonboarding.dto.request.draft;

import jakarta.validation.constraints.NotBlank;

public record CreateLoanApplicationDraftRequest(
        @NotBlank(message = "customerId hoặc customerCode là bắt buộc")
        String customerId
) {
}
