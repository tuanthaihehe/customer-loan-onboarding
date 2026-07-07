package com.f88.loanonboarding.dto.response.loan;

import java.time.LocalDateTime;

public record LoanApplicationDocumentUploadResponse(
        String applicationCode,
        String documentCode,
        String documentTypeCode,
        String fileId,
        String fileName,
        String contentType,
        Long size,
        String fileUrl,
        LocalDateTime uploadedAt
) {
}
