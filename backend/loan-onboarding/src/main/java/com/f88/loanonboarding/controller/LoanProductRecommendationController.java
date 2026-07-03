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
            summary = "Load và tính màn hình đề xuất gói vay cuối cùng",
            description = "Dùng cho bước 5. Backend lấy hồ sơ, tài sản, định giá gần nhất, tính scoring mock, lọc top sản phẩm phù hợp và tính lịch trả hàng tháng theo nhu cầu vay có thể điều chỉnh."
    )
    @PostMapping("/api/v1/loan-applications/{applicationCode}/final-loan-offer/preview")
    public ApiResponse<FinalLoanOfferResponse> previewFinalOffer(
            @PathVariable String applicationCode,
            @Valid @RequestBody(required = false) FinalLoanOfferPreviewRequest request
    ) {
        return ApiResponse.success(
                "Tính đề xuất gói vay cuối cùng thành công",
                loanProductRecommendationService.previewFinalOffer(applicationCode, request)
        );
    }

    @Operation(
            summary = "Lưu gói vay cuối cùng được chọn",
            description = "Dùng khi staff chọn sản phẩm vay cuối cùng. Backend validate sản phẩm còn phù hợp, sau đó lưu loan_product_id vào hồ sơ APP_DRAFT."
    )
    @PostMapping("/api/v1/loan-applications/{applicationCode}/final-loan-offer/select")
    public ApiResponse<FinalLoanOfferResponse> selectFinalOffer(
            @PathVariable String applicationCode,
            @Valid @RequestBody SelectFinalLoanOfferRequest request
    ) {
        return ApiResponse.success(
                "Lưu gói vay cuối cùng thành công",
                loanProductRecommendationService.selectFinalOffer(applicationCode, request)
        );
    }
}
