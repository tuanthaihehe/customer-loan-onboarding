package com.f88.loanonboarding.dto.request.loan;

import java.util.List;

public record SubmitLoanApplicationDraftRequest(
        List<Document> documents
) {

    public record Document(
            String documentTypeCode,
            String fileUrl,
            String fileName
    ) {
    }
}
