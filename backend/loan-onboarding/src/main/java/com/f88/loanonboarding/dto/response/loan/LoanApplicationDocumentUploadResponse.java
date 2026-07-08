package com.f88.loanonboarding.dto.response.loan;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record LoanApplicationDocumentUploadResponse(
        String applicationCode,
        int uploadedCount,
        List<DocumentItem> documents
) {
    public record DocumentItem(
            UUID documentId,
            String documentTypeCode,
            String documentTypeName,
            String fileUrl,
            String fileName,
            LocalDateTime uploadedAt
    ) {
    }
}
