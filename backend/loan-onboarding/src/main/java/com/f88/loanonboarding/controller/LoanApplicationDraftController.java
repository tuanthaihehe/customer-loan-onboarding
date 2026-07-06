package com.f88.loanonboarding.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.f88.loanonboarding.common.response.ApiResponse;
import com.f88.loanonboarding.dto.request.loan.CancelLoanApplicationDraftRequest;
import com.f88.loanonboarding.dto.request.loan.CompleteLoanApplicationDraftStepRequest;
import com.f88.loanonboarding.dto.request.loan.CreateLoanApplicationDraftFlowRequest;
import com.f88.loanonboarding.dto.request.loan.SaveLoanApplicationDraftStepRequest;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDraftDetailResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDraftStepActionResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDraftSubmitResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDraftSummaryResponse;
import com.f88.loanonboarding.enums.LoanApplicationDraftStatus;
import com.f88.loanonboarding.service.LoanApplicationDraftFlowService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Loan Application Draft", description = "API quản lý hồ sơ vay nháp nhiều bước")
@RestController
@RequestMapping("/api/v1/loan-application-drafts")
public class LoanApplicationDraftController {

    private final LoanApplicationDraftFlowService draftFlowService;

    public LoanApplicationDraftController(LoanApplicationDraftFlowService draftFlowService) {
        this.draftFlowService = draftFlowService;
    }

    @Operation(
            summary = "Tạo hồ sơ vay nháp theo khách hàng đã có",
            description = "Không tạo Customer mới. Frontend cần gọi Customer API trước, sau đó truyền customerId hoặc customerCode vào API này. Nếu có customerIdentifyPayload, bước CUSTOMER_IDENTIFY sẽ được đánh dấu hoàn thành."
    )
    @PostMapping
    public ApiResponse<LoanApplicationDraftDetailResponse> createDraft(
            @RequestBody CreateLoanApplicationDraftFlowRequest request
    ) {
        return ApiResponse.success("Loan application draft created", draftFlowService.createDraft(request));
    }

    @Operation(summary = "Lấy danh sách hồ sơ vay nháp")
    @GetMapping
    public ApiResponse<List<LoanApplicationDraftSummaryResponse>> findDrafts(
            @RequestParam(required = false) LoanApplicationDraftStatus status
    ) {
        return ApiResponse.success(draftFlowService.findDrafts(status));
    }

    @Operation(summary = "Lấy chi tiết hồ sơ vay nháp để frontend fill lại form")
    @GetMapping("/{draftCode}")
    public ApiResponse<LoanApplicationDraftDetailResponse> getDraft(@PathVariable String draftCode) {
        return ApiResponse.success(draftFlowService.getDraft(draftCode));
    }

    @Operation(
            summary = "Lưu nháp dữ liệu một bước",
            description = "Dùng cho autosave hoặc nút lưu nháp. API này không chuyển sang bước tiếp theo và không cho set COMPLETED; muốn hoàn thành bước thì gọi API complete."
    )
    @PutMapping("/{draftCode}/steps/{stepCode}")
    public ApiResponse<LoanApplicationDraftStepActionResponse> saveStep(
            @PathVariable String draftCode,
            @PathVariable String stepCode,
            @RequestBody SaveLoanApplicationDraftStepRequest request
    ) {
        return ApiResponse.success("Draft step saved", draftFlowService.saveStep(draftCode, stepCode, request));
    }

    @Operation(
            summary = "Hoàn thành một bước và chuyển bước tiếp theo",
            description = "Backend lưu payload, set step COMPLETED, cập nhật currentStepCode. Nếu toàn bộ bước đã hoàn thành thì draft chuyển sang COMPLETED và sẵn sàng submit."
    )
    @PostMapping("/{draftCode}/steps/{stepCode}/complete")
    public ApiResponse<LoanApplicationDraftStepActionResponse> completeStep(
            @PathVariable String draftCode,
            @PathVariable String stepCode,
            @RequestBody CompleteLoanApplicationDraftStepRequest request
    ) {
        return ApiResponse.success("Draft step completed", draftFlowService.completeStep(draftCode, stepCode, request));
    }

    @Operation(
            summary = "Submit hồ sơ vay nháp thành hồ sơ vay thật",
            description = "Chỉ submit khi tất cả step đã COMPLETED và không còn step requiresReview. Backend tạo loan_application thật và gắn converted_loan_application_id vào draft."
    )
    @PostMapping("/{draftCode}/submit")
    public ApiResponse<LoanApplicationDraftSubmitResponse> submit(@PathVariable String draftCode) {
        return ApiResponse.success("Draft submitted", draftFlowService.submit(draftCode));
    }

    @Operation(summary = "Hủy hồ sơ vay nháp")
    @PostMapping("/{draftCode}/cancel")
    public ApiResponse<LoanApplicationDraftSubmitResponse> cancel(
            @PathVariable String draftCode,
            @RequestBody(required = false) CancelLoanApplicationDraftRequest request
    ) {
        return ApiResponse.success("Draft cancelled", draftFlowService.cancel(draftCode, request));
    }
}
