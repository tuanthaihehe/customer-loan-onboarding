package com.f88.loanonboarding.controller;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.f88.loanonboarding.common.response.ApiResponse;
import com.f88.loanonboarding.dto.request.asset.AssetLookupRequest;
import com.f88.loanonboarding.dto.request.asset.SaveAssetSnapshotRequest;
import com.f88.loanonboarding.dto.response.asset.AssetLookupResponse;
import com.f88.loanonboarding.dto.response.asset.AssetSnapshotResponse;
import com.f88.loanonboarding.service.AssetService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Asset", description = "API tra cứu và ghi nhận tài sản")
@RestController
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @Operation(summary = "Tra cứu tài sản trước khi gắn vào hồ sơ vay")
    @PostMapping("/api/v1/assets/lookup")
    public ApiResponse<AssetLookupResponse> lookup(@Valid @RequestBody AssetLookupRequest request) {
        return ApiResponse.success("Asset lookup completed", assetService.lookup(request));
    }

    @Operation(summary = "Lưu thông tin tài sản vào hồ sơ vay nháp")
    @PatchMapping("/api/v1/loan-applications/{applicationCode}/asset-snapshot")
    public ApiResponse<AssetSnapshotResponse> saveSnapshot(
            @PathVariable String applicationCode,
            @Valid @RequestBody SaveAssetSnapshotRequest request
    ) {
        return ApiResponse.success("Asset snapshot saved", assetService.saveSnapshot(applicationCode, request));
    }
}
