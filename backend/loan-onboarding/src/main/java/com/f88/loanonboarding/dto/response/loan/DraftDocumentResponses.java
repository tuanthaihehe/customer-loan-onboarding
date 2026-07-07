package com.f88.loanonboarding.dto.response.loan;

import java.time.LocalDateTime;
import java.util.List;

public final class DraftDocumentResponses {

    private DraftDocumentResponses() {
    }

    public record RequirementGroup(
            String groupCode,
            String groupName,
            int requiredCount,
            int totalCount,
            List<RequirementItem> documents
    ) {
    }

    public record RequirementItem(
            String documentCode,
            String documentName,
            boolean required,
            List<String> allowedExtensions,
            long maxSizeMb
    ) {
    }

    public record State(
            String draftCode,
            List<GroupState> groups,
            Checklist checklist,
            EkycResult ekycResult
    ) {
    }

    public record GroupState(
            String groupCode,
            String groupName,
            int uploadedCount,
            int totalCount,
            int requiredUploadedCount,
            int requiredCount,
            List<DocumentFile> documents
    ) {
    }

    public record DocumentFile(
            String documentCode,
            String documentName,
            String groupCode,
            boolean required,
            boolean uploaded,
            String fileId,
            String fileName,
            String contentType,
            Long size,
            String fileUrl,
            LocalDateTime uploadedAt
    ) {
    }

    public record Checklist(
            int totalRequired,
            int uploadedRequired,
            boolean canComplete,
            List<String> missingRequiredDocumentCodes
    ) {
    }

    public record EkycResult(
            String faceMatchStatus,
            String livenessStatus,
            String note
    ) {
    }

    public record UploadResult(
            DocumentFile document,
            Checklist checklist,
            EkycResult ekycResult
    ) {
    }
}
