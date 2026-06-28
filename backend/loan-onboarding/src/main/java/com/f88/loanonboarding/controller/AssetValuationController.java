package com.f88.loanonboarding.controller;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.f88.loanonboarding.common.response.ApiResponse;
import com.f88.loanonboarding.dto.request.asset.AssetValuationPreviewRequest;
import com.f88.loanonboarding.dto.response.asset.AssetValuationPreviewResponse;
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

    @Operation(summary = "Tính thử giá trị định giá tài sản")
    @PostMapping("/api/v1/loan-applications/{applicationCode}/asset-valuations/preview")
    public ApiResponse<AssetValuationPreviewResponse> preview(
            @PathVariable String applicationCode,
            @Valid @RequestBody AssetValuationPreviewRequest request
    ) {
        return ApiResponse.success("Asset valuation preview calculated", assetValuationService.preview(applicationCode, request));
    }

    @Operation(summary = "Lưu kết quả định giá thử vào hồ sơ vay")
    @PatchMapping("/api/v1/loan-applications/{applicationCode}/valuation-preview")
    public ApiResponse<AssetValuationPreviewResponse> savePreview(
            @PathVariable String applicationCode,
            @Valid @RequestBody AssetValuationPreviewRequest request
    ) {
        return ApiResponse.success("Asset valuation preview saved", assetValuationService.savePreview(applicationCode, request));
    }
}
