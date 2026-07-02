package com.f88.loanonboarding.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.f88.loanonboarding.common.response.ApiResponse;
import com.f88.loanonboarding.dto.request.loanproduct.LoanProductRecommendationRequest;
import com.f88.loanonboarding.dto.response.loanproduct.LoanProductDetailResponse;
import com.f88.loanonboarding.dto.response.loanproduct.LoanProductQuoteResponse;
import com.f88.loanonboarding.dto.response.loanproduct.LoanProductRecommendationResponse;
import com.f88.loanonboarding.service.LoanProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Loan Product", description = "API đề xuất và tính toán sản phẩm vay")
@RestController
@RequestMapping("/api/v1/loan-products")
public class LoanProductController {

    private final LoanProductService loanProductService;

    public LoanProductController(LoanProductService loanProductService) {
        this.loanProductService = loanProductService;
    }

    @Operation(
            summary = "Đề xuất top 3 sản phẩm vay phù hợp",
            description = "Lọc sản phẩm theo mục đích vay, loại tài sản, kỳ hạn và hạng điểm nếu có; sau đó tính hạn mức theo LTV, số tiền đề xuất, tiền trả hàng tháng và sắp xếp để trả top 3 sản phẩm phù hợp nhất."
    )
    @PostMapping("/recommendations")
    public ApiResponse<LoanProductRecommendationResponse> recommend(
            @Valid @RequestBody LoanProductRecommendationRequest request
    ) {
        return ApiResponse.success("Loan product recommendations calculated", loanProductService.recommend(request));
    }

    @Operation(
            summary = "Lấy chi tiết cấu hình sản phẩm vay",
            description = "Trả cấu hình sản phẩm vay gồm min/max amount, LTV, lãi suất tháng và các điều kiện áp dụng như mục đích vay, loại tài sản, kỳ hạn, hạng điểm."
    )
    @GetMapping("/{productCode}")
    public ApiResponse<LoanProductDetailResponse> getDetail(@PathVariable String productCode) {
        return ApiResponse.success(loanProductService.getDetail(productCode));
    }

    @Operation(
            summary = "Tính quote cho một sản phẩm vay cụ thể",
            description = "Dùng khi frontend cần tính lại số tiền đề xuất và dự kiến trả hàng tháng cho đúng một sản phẩm cụ thể. Backend sẽ validate sản phẩm có phù hợp input hay không trước khi tính."
    )
    @PostMapping("/{productCode}/quote")
    public ApiResponse<LoanProductQuoteResponse> quote(
            @PathVariable String productCode,
            @Valid @RequestBody LoanProductRecommendationRequest request
    ) {
        return ApiResponse.success("Loan product quote calculated", loanProductService.quote(productCode, request));
    }
}
