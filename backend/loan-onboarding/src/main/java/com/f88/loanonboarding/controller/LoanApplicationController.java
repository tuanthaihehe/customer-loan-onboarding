package com.f88.loanonboarding.controller;

import java.util.List;

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
import com.f88.loanonboarding.dto.request.loan.UpdateLoanApplicationRequest;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDetailResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationListItemResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationSummaryResponse;
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

    @Operation(summary = "Tạo hồ sơ vay")
    @PostMapping
    public ApiResponse<LoanApplicationSummaryResponse> createApplication(
            @Valid @RequestBody CreateLoanApplicationRequest request
    ) {
        return ApiResponse.success("Loan application created", loanApplicationService.createApplication(request));
    }

    @Operation(summary = "Lấy danh sách hồ sơ vay đã ra khỏi luồng nháp")
    @GetMapping
    public ApiResponse<List<LoanApplicationListItemResponse>> findLoanApplications() {
        return ApiResponse.success(loanApplicationService.findLoanApplications());
    }

    @Operation(summary = "Lấy chi tiết hồ sơ vay")
    @GetMapping("/{applicationCode}")
    public ApiResponse<LoanApplicationDetailResponse> getDetail(@PathVariable String applicationCode) {
        return ApiResponse.success(loanApplicationService.getDetail(applicationCode));
    }

    @Operation(summary = "Cập nhật thông tin khoản vay")
    @PatchMapping("/{applicationCode}/loan-request")
    public ApiResponse<LoanApplicationSummaryResponse> updateLoanRequest(
            @PathVariable String applicationCode,
            @Valid @RequestBody UpdateLoanApplicationRequest request
    ) {
        return ApiResponse.success("Loan application updated", loanApplicationService.updateLoanRequest(applicationCode, request));
    }

    @Operation(summary = "Hủy hồ sơ vay")
    @PostMapping("/{applicationCode}/cancel")
    public ApiResponse<LoanApplicationSummaryResponse> cancel(
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
