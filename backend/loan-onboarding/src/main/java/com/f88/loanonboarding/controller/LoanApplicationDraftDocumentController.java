package com.f88.loanonboarding.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.f88.loanonboarding.common.response.ApiResponse;
import com.f88.loanonboarding.dto.response.loan.DraftDocumentResponses;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDraftStepActionResponse;
import com.f88.loanonboarding.service.LoanApplicationDraftDocumentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Loan Application Draft Documents", description = "API upload chứng từ cho màn 4 của hồ sơ vay nháp")
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

    @Operation(summary = "Lấy trạng thái upload chứng từ của hồ sơ vay nháp")
    @GetMapping
    public ApiResponse<DraftDocumentResponses.State> getState(@PathVariable String draftCode) {
        return ApiResponse.success(documentService.getState(draftCode));
    }

    @Operation(summary = "Upload hoặc thay thế một chứng từ của màn 4")
    @PostMapping(value = "/{documentCode}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<DraftDocumentResponses.UploadResult> upload(
            @PathVariable String draftCode,
            @PathVariable String documentCode,
            @RequestPart("file") MultipartFile file
    ) {
        return ApiResponse.success("Upload chứng từ thành công", documentService.upload(draftCode, documentCode, file));
    }

    @Operation(summary = "Xóa file tạm của một chứng từ đã upload")
    @DeleteMapping("/{documentCode}")
    public ApiResponse<DraftDocumentResponses.State> delete(
            @PathVariable String draftCode,
            @PathVariable String documentCode
    ) {
        return ApiResponse.success("Xóa chứng từ tạm thành công", documentService.delete(draftCode, documentCode));
    }

    @Operation(summary = "Hoàn tất bước Upload chứng từ & Hoàn tất")
    @PostMapping("/complete")
    public ApiResponse<LoanApplicationDraftStepActionResponse> complete(@PathVariable String draftCode) {
        return ApiResponse.success("Hoàn tất upload chứng từ thành công", documentService.complete(draftCode));
    }
}
