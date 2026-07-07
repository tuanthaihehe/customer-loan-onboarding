package com.f88.loanonboarding.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestPart;

import com.f88.loanonboarding.common.response.ApiResponse;
import com.f88.loanonboarding.dto.request.loan.CancelLoanApplicationRequest;
import com.f88.loanonboarding.dto.request.loan.CompleteLoanApplicationDocumentUploadRequest;
import com.f88.loanonboarding.dto.request.loan.CreateLoanApplicationRequest;
import com.f88.loanonboarding.dto.request.loan.SaveCustomerDetailRequest;
import com.f88.loanonboarding.dto.request.loan.SaveLoanApplicationDraftRequest;
import com.f88.loanonboarding.dto.request.loan.SaveReferencePersonsRequest;
import com.f88.loanonboarding.dto.response.loan.CompleteLoanApplicationDocumentUploadResponse;
import com.f88.loanonboarding.dto.response.loan.CustomerDetailResponse;
import com.f88.loanonboarding.dto.response.loan.DeleteLoanApplicationDocumentUploadResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDetailResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDocumentUploadResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDraftResponse;
import com.f88.loanonboarding.dto.response.loan.ReferencePersonsResponse;
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
        return ApiResponse.success("Tạo hồ sơ vay nháp thành công", loanApplicationService.createDraft(request));
    }

    @Operation(summary = "Lấy chi tiết hồ sơ vay")
    @GetMapping("/{applicationCode}")
    public ApiResponse<LoanApplicationDetailResponse> getDetail(@PathVariable String applicationCode) {
        return ApiResponse.success(loanApplicationService.getDetail(applicationCode));
    }

    @Operation(summary = "Lưu thông tin sơ bộ khách hàng và nhu cầu vay")
    @PatchMapping("/{applicationCode}/draft")
    public ApiResponse<LoanApplicationDraftResponse> saveDraft(
            @PathVariable String applicationCode,
            @Valid @RequestBody SaveLoanApplicationDraftRequest request
    ) {
        return ApiResponse.success("Lưu thông tin sơ bộ khách hàng thành công", loanApplicationService.saveDraft(applicationCode, request));
    }

    @Operation(summary = "Lưu thông tin chi tiết khách hàng")
    @PatchMapping("/{applicationCode}/customer-detail")
    public ApiResponse<CustomerDetailResponse> saveCustomerDetail(
            @PathVariable String applicationCode,
            @Valid @RequestBody SaveCustomerDetailRequest request
    ) {
        return ApiResponse.success("Lưu thông tin chi tiết khách hàng thành công", loanApplicationService.saveCustomerDetail(applicationCode, request));
    }

    @Operation(summary = "Lưu danh sách người tham chiếu")
    @PutMapping("/{applicationCode}/reference-persons")
    public ApiResponse<ReferencePersonsResponse> saveReferencePersons(
            @PathVariable String applicationCode,
            @Valid @RequestBody SaveReferencePersonsRequest request
    ) {
        return ApiResponse.success("Lưu người tham chiếu thành công", loanApplicationService.saveReferencePersons(applicationCode, request));
    }

    @Operation(summary = "Upload chứng từ cho hồ sơ vay chính")
    @PostMapping(value = "/{applicationCode}/documents/{documentCode}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<List<LoanApplicationDocumentUploadResponse>> uploadDocument(
            @PathVariable String applicationCode,
            @PathVariable String documentCode,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        return ApiResponse.success(
                "Upload chứng từ hồ sơ vay thành công",
                loanApplicationService.uploadDocuments(applicationCode, documentCode, uploadedFiles(files, file))
        );
    }

    private List<MultipartFile> uploadedFiles(List<MultipartFile> files, MultipartFile file) {
        List<MultipartFile> result = new ArrayList<>();
        if (files != null) {
            result.addAll(files);
        }
        if (file != null) {
            result.add(file);
        }
        return result;
    }

    @Operation(summary = "Xóa ảnh chứng từ đã upload lên S3")
    @DeleteMapping("/{applicationCode}/documents")
    public ApiResponse<DeleteLoanApplicationDocumentUploadResponse> deleteUploadedDocument(
            @PathVariable String applicationCode,
            @RequestParam String fileUrl
    ) {
        return ApiResponse.success(
                "Xóa ảnh chứng từ thành công",
                loanApplicationService.deleteUploadedDocument(applicationCode, fileUrl)
        );
    }

    @Operation(summary = "Hoàn tất upload chứng từ và lưu metadata vào hồ sơ vay")
    @PostMapping("/{applicationCode}/documents/complete")
    public ApiResponse<CompleteLoanApplicationDocumentUploadResponse> completeDocumentUpload(
            @PathVariable String applicationCode,
            @RequestBody CompleteLoanApplicationDocumentUploadRequest request
    ) {
        return ApiResponse.success(
                "Hoàn tất upload chứng từ thành công",
                loanApplicationService.completeDocumentUpload(applicationCode, request)
        );
    }

    @Operation(summary = "Hủy hồ sơ vay")
    @PostMapping("/{applicationCode}/cancel")
    public ApiResponse<LoanApplicationDraftResponse> cancel(
            @PathVariable String applicationCode,
            @Valid @RequestBody CancelLoanApplicationRequest request
    ) {
        return ApiResponse.success("Hủy hồ sơ vay thành công", loanApplicationService.cancel(applicationCode, request));
    }

    @Operation(summary = "Hoàn thành bước thông tin sơ bộ và gói vay")
    @PostMapping("/{applicationCode}/steps/preliminary/complete")
    public ApiResponse<StepCompletionResponse> completePreliminaryStep(@PathVariable String applicationCode) {
        return ApiResponse.success(
                "Hoàn thành bước thông tin sơ bộ",
                loanApplicationService.completePreliminaryStep(applicationCode)
        );
    }

    @Operation(summary = "Gửi hồ sơ vay sang bước phê duyệt")
    @PostMapping("/{applicationCode}/submit-for-approval")
    public ApiResponse<SubmitForApprovalResponse> submitForApproval(@PathVariable String applicationCode) {
        return ApiResponse.success(
                "Gửi hồ sơ vay sang bước phê duyệt thành công",
                loanApplicationService.submitForApproval(applicationCode)
        );
    }
}
