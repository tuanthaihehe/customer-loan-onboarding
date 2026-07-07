package com.f88.loanonboarding.dto.response.loan;

public record DeleteLoanApplicationDocumentUploadResponse(
        String applicationCode,
        String fileUrl,
        boolean deleted,
        String message
) {
}
