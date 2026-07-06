package com.f88.loanonboarding.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.f88.loanonboarding.common.response.ApiResponse;
import com.f88.loanonboarding.dto.request.draft.CreateLoanApplicationDraftRequest;
import com.f88.loanonboarding.dto.request.draft.SaveLoanApplicationDraftStepRequest;
import com.f88.loanonboarding.dto.response.draft.LoanApplicationDraftOverviewResponse;
import com.f88.loanonboarding.dto.response.draft.LoanApplicationDraftStepPayloadResponse;
import com.f88.loanonboarding.dto.response.draft.SaveLoanApplicationDraftStepResponse;
import com.f88.loanonboarding.service.LoanApplicationDraftService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Loan Application Draft", description = "API hồ sơ vay nháp theo từng bước")
@RestController
@RequestMapping("/api/v1/loan-application-drafts")
public class LoanApplicationDraftController {

    private final LoanApplicationDraftService draftService;

    public LoanApplicationDraftController(LoanApplicationDraftService draftService) {
        this.draftService = draftService;
    }

    @Operation(summary = "Tạo hồ sơ vay nháp theo customer đã tồn tại")
    @PostMapping
    public ApiResponse<LoanApplicationDraftOverviewResponse> createDraft(
            @Valid @RequestBody CreateLoanApplicationDraftRequest request
    ) {
        return ApiResponse.success("Tạo hồ sơ vay nháp theo step thành công", draftService.createDraft(request));
    }

    @Operation(summary = "Lấy overview hồ sơ vay nháp")
    @GetMapping("/{draftId}")
    public ApiResponse<LoanApplicationDraftOverviewResponse> getOverview(@PathVariable UUID draftId) {
        return ApiResponse.success(draftService.getOverview(draftId));
    }

    @Operation(summary = "Lấy toàn bộ dữ liệu các bước của hồ sơ vay nháp")
    @GetMapping("/{draftId}/data")
    public ApiResponse<LoanApplicationDraftOverviewResponse> getAllData(@PathVariable UUID draftId) {
        return ApiResponse.success(draftService.getOverview(draftId));
    }

    @Operation(summary = "Lấy payload của bước hiện tại trong hồ sơ vay nháp")
    @GetMapping("/{draftId}/current-step")
    public ApiResponse<LoanApplicationDraftStepPayloadResponse> getCurrentStepPayload(@PathVariable UUID draftId) {
        return ApiResponse.success(draftService.getCurrentStepPayload(draftId));
    }

    @Operation(summary = "Lấy payload của một step trong hồ sơ vay nháp")
    @GetMapping("/{draftId}/steps/{stepCode}")
    public ApiResponse<LoanApplicationDraftStepPayloadResponse> getStepPayload(
            @PathVariable UUID draftId,
            @PathVariable String stepCode
    ) {
        return ApiResponse.success(draftService.getStepPayload(draftId, stepCode));
    }

    @Operation(summary = "Lưu payload một bước của hồ sơ vay nháp")
    @PutMapping("/{draftId}/steps/{stepCode}")
    public ApiResponse<SaveLoanApplicationDraftStepResponse> saveStep(
            @PathVariable UUID draftId,
            @PathVariable String stepCode,
            @Valid @RequestBody SaveLoanApplicationDraftStepRequest request
    ) {
        return ApiResponse.success("Lưu step hồ sơ vay nháp thành công", draftService.saveStep(draftId, stepCode, request));
    }
}
