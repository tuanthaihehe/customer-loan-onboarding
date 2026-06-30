package com.f88.loanonboarding.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.f88.loanonboarding.common.response.ApiResponse;
import com.f88.loanonboarding.dto.request.asset.AssetValuationPreviewRequest;
import com.f88.loanonboarding.dto.response.asset.AssetValuationPreviewResponse;
import com.f88.loanonboarding.dto.response.asset.VehicleMarketPriceResponse;
import com.f88.loanonboarding.service.AssetValuationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Asset Valuation", description = "API định giá tài sản")
@RestController
public class AssetValuationController {

    private final AssetValuationService assetValuationService;

    public AssetValuationController(AssetValuationService assetValuationService) {
        this.assetValuationService = assetValuationService;
    }

    @Operation(summary = "Lấy giá thị trường hiện hành theo biến thể xe")
    @GetMapping("/api/v1/asset-valuations/market-price")
    public ApiResponse<VehicleMarketPriceResponse> getMarketPrice(@RequestParam String vehicleVariant) {
        return ApiResponse.success(assetValuationService.getMarketPrice(vehicleVariant));
    }

    @Operation(
            summary = "Tính thử giá trị định giá tài sản, không lưu database",
            description = "Dùng sau khi chọn xong xe. API lấy giá thị trường theo vehicleVariant, áp dụng các yếu tố giảm trừ nếu có, rồi trả về giá trị tài sản sau định giá. API này không cần hồ sơ vay và không ghi asset_valuation."
    )
    @PostMapping("/api/v1/asset-valuations/preview")
    public ApiResponse<AssetValuationPreviewResponse> preview(
            @Valid @RequestBody AssetValuationPreviewRequest request
    ) {
        return ApiResponse.success("Asset valuation preview calculated", assetValuationService.preview(request));
    }

    @Operation(
            summary = "Lưu kết quả định giá tài sản vào hồ sơ vay",
            description = "Dùng ở bước chốt định giá. Hồ sơ vay phải tồn tại và đã được gắn asset trước đó. API tính lại giá trị định giá từ vehicleVariant và deductionItems, sau đó ghi asset_valuation và asset_valuation_deduction."
    )
    @PostMapping("/api/v1/loan-applications/{applicationCode}/asset-valuations")
    public ApiResponse<AssetValuationPreviewResponse> savePreview(
            @PathVariable String applicationCode,
            @Valid @RequestBody AssetValuationPreviewRequest request
    ) {
        return ApiResponse.success("Asset valuation preview saved", assetValuationService.savePreview(applicationCode, request));
    }
}
