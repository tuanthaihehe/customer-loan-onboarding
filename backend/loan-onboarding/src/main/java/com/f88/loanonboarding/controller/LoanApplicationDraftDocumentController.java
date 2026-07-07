package com.f88.loanonboarding.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.f88.loanonboarding.common.response.ApiResponse;
import com.f88.loanonboarding.dto.response.loan.DraftDocumentResponses;
import com.f88.loanonboarding.service.LoanApplicationDraftDocumentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Loan Application Draft Documents", description = "API upload chứng từ cho màn 4")
@RestController
@RequestMapping("/api/v1/loan-application-drafts/{draftCode}/documents")
public class LoanApplicationDraftDocumentController {

    private final LoanApplicationDraftDocumentService documentService;

    public LoanApplicationDraftDocumentController(LoanApplicationDraftDocumentService documentService) {
        this.documentService = documentService;
    }

    @Operation(summary = "Lấy danh mục chứng từ cần upload ở màn 4")
    @GetMapping("/requirements")
    public ApiResponse<List<DraftDocumentResponses.RequirementGroup>> getRequirements(
            @PathVariable String draftCode
    ) {
        documentService.getState(draftCode);
        return ApiResponse.success(documentService.getRequirements());
    }

    @Operation(summary = "Upload file chứng từ và trả về URL để frontend hiển thị")
    @PostMapping(value = "/{documentCode}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<DraftDocumentResponses.UploadUrlResult> upload(
            @PathVariable String draftCode,
            @PathVariable String documentCode,
            @RequestPart("file") MultipartFile file
    ) {
        return ApiResponse.success("Upload chứng từ thành công", documentService.upload(draftCode, documentCode, file));
    }
}
