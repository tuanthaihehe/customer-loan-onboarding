package com.f88.loanonboarding.dto.response.loan;

public record CompleteLoanApplicationDocumentUploadResponse(
        String applicationCode,
        int documentCount,
        String message
) {
}
