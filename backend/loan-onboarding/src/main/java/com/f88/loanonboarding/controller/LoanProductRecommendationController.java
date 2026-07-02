package com.f88.loanonboarding.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.f88.loanonboarding.common.response.ApiResponse;
import com.f88.loanonboarding.dto.request.loan.FinalLoanOfferPreviewRequest;
import com.f88.loanonboarding.dto.request.loan.LoanProductRecommendationRequest;
import com.f88.loanonboarding.dto.request.loan.SelectFinalLoanOfferRequest;
import com.f88.loanonboarding.dto.response.loan.FinalLoanOfferResponse;
import com.f88.loanonboarding.dto.response.loan.LoanProductRecommendationResponse;
import com.f88.loanonboarding.service.LoanProductRecommendationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Loan Product Recommendation", description = "API đề xuất gói vay theo tài sản sau giảm trừ")
@RestController
public class LoanProductRecommendationController {

    private final LoanProductRecommendationService loanProductRecommendationService;

    public LoanProductRecommendationController(LoanProductRecommendationService loanProductRecommendationService) {
        this.loanProductRecommendationService = loanProductRecommendationService;
    }

    @Operation(
            summary = "Đề xuất gói vay cho hồ sơ dựa trên tài sản, yếu tố giảm trừ và nhu cầu vay",
            description = "Hồ sơ cần có thông tin nhu cầu vay và đã gắn tài sản. API tính lại giá sau giảm trừ, lọc loan_product theo mục đích vay, loại tài sản, kỳ hạn và score grade, sau đó trả tối đa 3 gói phù hợp nhất."
    )
    @PostMapping("/api/v1/loan-applications/{applicationCode}/loan-product-recommendations")
    public ApiResponse<LoanProductRecommendationResponse> recommend(
            @PathVariable String applicationCode,
            @Valid @RequestBody(required = false) LoanProductRecommendationRequest request
    ) {
        return ApiResponse.success(
                "Đề xuất gói vay thành công",
                loanProductRecommendationService.recommend(applicationCode, request)
        );
    }

    @Operation(
            summary = "Load va tinh man hinh de xuat goi vay cuoi cung",
            description = "Dung cho buoc 5. Backend lay ho so, tai san, dinh gia gan nhat, tinh scoring mock, loc top san pham phu hop va tinh lich tra hang thang theo nhu cau vay co the dieu chinh."
    )
    @PostMapping("/api/v1/loan-applications/{applicationCode}/final-loan-offer/preview")
    public ApiResponse<FinalLoanOfferResponse> previewFinalOffer(
            @PathVariable String applicationCode,
            @Valid @RequestBody(required = false) FinalLoanOfferPreviewRequest request
    ) {
        return ApiResponse.success(
                "Tinh de xuat goi vay cuoi cung thanh cong",
                loanProductRecommendationService.previewFinalOffer(applicationCode, request)
        );
    }

    @Operation(
            summary = "Luu goi vay cuoi cung duoc chon",
            description = "Dung khi staff chon san pham vay cuoi cung. Backend validate san pham con phu hop, sau do luu loan_product_id va snapshot final offer vao APP_DRAFT."
    )
    @PostMapping("/api/v1/loan-applications/{applicationCode}/final-loan-offer/select")
    public ApiResponse<FinalLoanOfferResponse> selectFinalOffer(
            @PathVariable String applicationCode,
            @Valid @RequestBody SelectFinalLoanOfferRequest request
    ) {
        return ApiResponse.success(
                "Luu goi vay cuoi cung thanh cong",
                loanProductRecommendationService.selectFinalOffer(applicationCode, request)
        );
    }
}
