package com.f88.loanonboarding.dto.request.loan;

import java.time.LocalDateTime;
import java.util.List;

public record SubmitLoanApplicationDraftRequest(
        List<Document> documents
) {

    public record Document(
            String documentCode,
            String documentTypeCode,
            String fileUrl,
            String fileName,
            LocalDateTime uploadedAt,
            String uploadedBy,
            String note
    ) {
    }
}
