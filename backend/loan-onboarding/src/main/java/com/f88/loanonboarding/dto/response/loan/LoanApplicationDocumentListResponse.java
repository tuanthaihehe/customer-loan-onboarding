package com.f88.loanonboarding.dto.response.loan;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record LoanApplicationDocumentListResponse(
        String applicationCode,
        int documentCount,
        List<DocumentItem> documents
) {
    public record DocumentItem(
            UUID documentId,
            String documentTypeCode,
            String documentTypeName,
            String fileUrl,
            String fileName,
            LocalDateTime uploadedAt,
            String uploadedBy
    ) {
    }
}
