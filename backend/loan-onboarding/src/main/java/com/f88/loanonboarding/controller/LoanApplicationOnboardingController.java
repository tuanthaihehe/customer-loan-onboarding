package com.f88.loanonboarding.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.f88.loanonboarding.common.response.ApiResponse;
import com.f88.loanonboarding.dto.request.loan.CancelLoanApplicationOnboardingRequest;
import com.f88.loanonboarding.dto.request.loan.CompleteLoanApplicationStepRequest;
import com.f88.loanonboarding.dto.request.loan.CreateLoanApplicationOnboardingRequest;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationOnboardingDetailResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationStepActionResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationSubmitResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationOnboardingSummaryResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDocumentListResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDocumentUploadResponse;
import com.f88.loanonboarding.enums.LoanApplicationOnboardingStatus;
import com.f88.loanonboarding.service.LoanApplicationDocumentService;
import com.f88.loanonboarding.service.LoanApplicationOnboardingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Loan Application Onboarding", description = "API khởi tạo và hoàn thiện hồ sơ vay nhiều bước trên loan_application.")
@RestController
@RequestMapping("/api/v1/loan-applications/onboarding")
public class LoanApplicationOnboardingController {

    private final LoanApplicationOnboardingService onboardingService;
    private final LoanApplicationDocumentService documentService;

    public LoanApplicationOnboardingController(
            LoanApplicationOnboardingService onboardingService,
            LoanApplicationDocumentService documentService
    ) {
        this.onboardingService = onboardingService;
        this.documentService = documentService;
    }

    @Operation(
            summary = "Khởi tạo hồ sơ vay theo khách hàng đã có",
            description = "Không tạo Customer mới. Frontend cần gọi Customer API trước, sau đó truyền customerId hoặc customerCode vào API này. Backend tạo loan_application ngay từ đầu."
    )
    @PostMapping
    public ApiResponse<LoanApplicationOnboardingDetailResponse> createApplication(
            @RequestBody CreateLoanApplicationOnboardingRequest request
    ) {
        return ApiResponse.success("Loan application created", onboardingService.createApplication(request));
    }

    @Operation(summary = "Lấy danh sách hồ sơ vay đang khởi tạo/hoàn thiện")
    @GetMapping
    public ApiResponse<List<LoanApplicationOnboardingSummaryResponse>> findApplications(
            @RequestParam(required = false) LoanApplicationOnboardingStatus status
    ) {
        return ApiResponse.success(onboardingService.findApplications(status));
    }

    @Operation(summary = "Lấy chi tiết hồ sơ vay để frontend fill lại form")
    @GetMapping("/{applicationCode}")
    public ApiResponse<LoanApplicationOnboardingDetailResponse> getApplication(@PathVariable String applicationCode) {
        return ApiResponse.success(onboardingService.getApplication(applicationCode));
    }

    @Operation(
            summary = "Hoàn thành một bước và chuyển bước tiếp theo",
            description = "Backend lưu payload vào loan_application_step_data, set step COMPLETED, cập nhật currentStepCode. Nếu toàn bộ bước đã hoàn thành thì loan_application chuyển sang APP_COMPLETED và sẵn sàng submit."
    )
    @PostMapping("/{applicationCode}/steps/{stepCode}/complete")
    public ApiResponse<LoanApplicationStepActionResponse> completeStep(
            @PathVariable String applicationCode,
            @PathVariable String stepCode,
            @RequestBody CompleteLoanApplicationStepRequest request
    ) {
        return ApiResponse.success("Application step completed", onboardingService.completeStep(applicationCode, stepCode, request));
    }

    @Operation(
            summary = "Upload chứng từ hồ sơ vay lên S3",
            description = "Nhận multipart files kèm documentTypeCodes, upload file lên S3 và lưu reference vào loan_application_document theo từng loại chứng từ."
    )
    @PostMapping(value = "/{applicationCode}/documents", consumes = "multipart/form-data")
    public ApiResponse<LoanApplicationDocumentUploadResponse> uploadDocuments(
            @PathVariable String applicationCode,
            @RequestParam("documentTypeCodes") List<String> documentTypeCodes,
            @RequestPart("files") List<MultipartFile> files,
            @RequestParam(value = "uploadedBy", required = false) String uploadedBy
    ) {
        return ApiResponse.success(
                "Documents uploaded",
                documentService.uploadDocuments(applicationCode, documentTypeCodes, files, uploadedBy)
        );
    }

    @Operation(
            summary = "Lấy danh sách chứng từ của hồ sơ vay",
            description = "Trả về các chứng từ đã upload và đang được lưu reference trong loan_application_document theo applicationCode."
    )
    @GetMapping("/{applicationCode}/documents")
    public ApiResponse<LoanApplicationDocumentListResponse> findDocuments(@PathVariable String applicationCode) {
        return ApiResponse.success(documentService.findDocuments(applicationCode));
    }

    @Operation(
            summary = "Nộp hồ sơ vay sang thẩm định",
            description = "Chỉ submit khi loan_application đang ở APP_COMPLETED, tất cả step đã COMPLETED và không còn step requiresReview. API này không tạo hồ sơ mới vì loan_application đã là hồ sơ thật từ đầu."
    )
    @PostMapping("/{applicationCode}/submit")
    public ApiResponse<LoanApplicationSubmitResponse> submit(@PathVariable String applicationCode) {
        return ApiResponse.success("Application submitted", onboardingService.submit(applicationCode));
    }

    @Operation(summary = "Hủy hồ sơ vay")
    @PostMapping("/{applicationCode}/cancel")
    public ApiResponse<LoanApplicationSubmitResponse> cancel(
            @PathVariable String applicationCode,
            @RequestBody(required = false) CancelLoanApplicationOnboardingRequest request
    ) {
        return ApiResponse.success("Application cancelled", onboardingService.cancel(applicationCode, request));
    }
}
