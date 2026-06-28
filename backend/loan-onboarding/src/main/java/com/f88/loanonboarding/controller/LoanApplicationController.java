package com.f88.loanonboarding.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.f88.loanonboarding.common.response.ApiResponse;
import com.f88.loanonboarding.dto.request.loan.CancelLoanApplicationRequest;
import com.f88.loanonboarding.dto.request.loan.CreateLoanApplicationRequest;
import com.f88.loanonboarding.dto.request.loan.SaveLoanApplicationDraftRequest;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDetailResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDraftResponse;
import com.f88.loanonboarding.dto.response.loan.StepCompletionResponse;
import com.f88.loanonboarding.dto.response.loan.SubmitForApprovalResponse;
import com.f88.loanonboarding.service.LoanApplicationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Loan Application", description = "API tạo và cập nhật hồ sơ vay")
@RestController
@RequestMapping("/api/v1/loan-applications")
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    public LoanApplicationController(LoanApplicationService loanApplicationService) {
        this.loanApplicationService = loanApplicationService;
    }

    @Operation(summary = "Tạo hồ sơ vay nháp")
    @PostMapping
    public ApiResponse<LoanApplicationDraftResponse> createDraft(
            @Valid @RequestBody CreateLoanApplicationRequest request
    ) {
        return ApiResponse.success("Loan application draft created", loanApplicationService.createDraft(request));
    }

    @Operation(summary = "Lấy chi tiết hồ sơ vay")
    @GetMapping("/{applicationCode}")
    public ApiResponse<LoanApplicationDetailResponse> getDetail(@PathVariable String applicationCode) {
        return ApiResponse.success(loanApplicationService.getDetail(applicationCode));
    }

    @Operation(summary = "Lưu nháp thông tin hồ sơ vay")
    @PatchMapping("/{applicationCode}/draft")
    public ApiResponse<LoanApplicationDraftResponse> saveDraft(
            @PathVariable String applicationCode,
            @Valid @RequestBody SaveLoanApplicationDraftRequest request
    ) {
        return ApiResponse.success("Loan application draft saved", loanApplicationService.saveDraft(applicationCode, request));
    }

    @Operation(summary = "Hủy hồ sơ vay")
    @PostMapping("/{applicationCode}/cancel")
    public ApiResponse<LoanApplicationDraftResponse> cancel(
            @PathVariable String applicationCode,
            @Valid @RequestBody CancelLoanApplicationRequest request
    ) {
        return ApiResponse.success("Loan application cancelled", loanApplicationService.cancel(applicationCode, request));
    }

    @Operation(summary = "Hoàn thành bước thông tin sơ bộ và gói vay")
    @PostMapping("/{applicationCode}/steps/preliminary/complete")
    public ApiResponse<StepCompletionResponse> completePreliminaryStep(@PathVariable String applicationCode) {
        return ApiResponse.success(
                "Preliminary step completed",
                loanApplicationService.completePreliminaryStep(applicationCode)
        );
    }

    @Operation(summary = "Gửi hồ sơ vay sang bước phê duyệt")
    @PostMapping("/{applicationCode}/submit-for-approval")
    public ApiResponse<SubmitForApprovalResponse> submitForApproval(@PathVariable String applicationCode) {
        return ApiResponse.success(
                "Loan application submitted for approval",
                loanApplicationService.submitForApproval(applicationCode)
        );
    }

}
