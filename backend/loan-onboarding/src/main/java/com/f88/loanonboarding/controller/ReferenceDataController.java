package com.f88.loanonboarding.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.f88.loanonboarding.common.response.ApiResponse;
import com.f88.loanonboarding.dto.response.common.ReferenceDataItemResponse;
import com.f88.loanonboarding.service.ReferenceDataService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Reference Data", description = "API danh mục dùng cho dropdown UI")
@RestController
@RequestMapping("/api/v1/reference-data")
public class ReferenceDataController {

    private final ReferenceDataService referenceDataService;

    public ReferenceDataController(ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    @Operation(summary = "Lấy danh mục giới tính")
    @GetMapping("/genders")
    public ApiResponse<List<ReferenceDataItemResponse>> getGenders() {
        return ApiResponse.success(referenceDataService.getGenders());
    }

    @Operation(summary = "Lấy danh mục nghề nghiệp")
    @GetMapping("/occupations")
    public ApiResponse<List<ReferenceDataItemResponse>> getOccupations() {
        return ApiResponse.success(referenceDataService.getOccupations());
    }

    @Operation(summary = "Lấy danh mục mục đích vay")
    @GetMapping("/loan-purposes")
    public ApiResponse<List<ReferenceDataItemResponse>> getLoanPurposes() {
        return ApiResponse.success(referenceDataService.getLoanPurposes());
    }

    @Operation(summary = "Lấy danh mục loại tài sản")
    @GetMapping("/asset-types")
    public ApiResponse<List<ReferenceDataItemResponse>> getAssetTypes() {
        return ApiResponse.success(referenceDataService.getAssetTypes());
    }

    @Operation(summary = "Lấy danh mục hãng xe")
    @GetMapping("/vehicle-brands")
    public ApiResponse<List<ReferenceDataItemResponse>> getVehicleBrands(@RequestParam(required = false) String assetType) {
        return ApiResponse.success(referenceDataService.getVehicleBrands(assetType));
    }

    @Operation(summary = "Lấy danh mục dòng xe")
    @GetMapping("/vehicle-models")
    public ApiResponse<List<ReferenceDataItemResponse>> getVehicleModels(@RequestParam(required = false) String brandCode) {
        return ApiResponse.success(referenceDataService.getVehicleModels(brandCode));
    }

    @Operation(summary = "Lấy danh mục phiên bản xe")
    @GetMapping("/vehicle-variants")
    public ApiResponse<List<ReferenceDataItemResponse>> getVehicleVariants(@RequestParam(required = false) String modelCode) {
        return ApiResponse.success(referenceDataService.getVehicleVariants(modelCode));
    }

    @Operation(summary = "Lấy danh mục năm sản xuất")
    @GetMapping("/manufacture-years")
    public ApiResponse<List<ReferenceDataItemResponse>> getManufactureYears() {
        return ApiResponse.success(referenceDataService.getManufactureYears());
    }

    @Operation(summary = "Lấy danh mục màu xe")
    @GetMapping("/vehicle-colors")
    public ApiResponse<List<ReferenceDataItemResponse>> getVehicleColors() {
        return ApiResponse.success(referenceDataService.getVehicleColors());
    }

    @Operation(summary = "Lấy danh mục yếu tố giảm trừ định giá")
    @GetMapping("/valuation-deduction-factors")
    public ApiResponse<List<ReferenceDataItemResponse>> getValuationDeductionFactors() {
        return ApiResponse.success(referenceDataService.getValuationDeductionFactors());
    }
}
