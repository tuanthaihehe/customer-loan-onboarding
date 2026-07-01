package com.f88.loanonboarding.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.f88.loanonboarding.common.response.ApiResponse;
import com.f88.loanonboarding.dto.response.common.ReferenceDataItemResponse;
import com.f88.loanonboarding.enums.AssetType;
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

    @Operation(summary = "Lấy danh mục kỳ hạn vay")
    @GetMapping("/loan-terms")
    public ApiResponse<List<ReferenceDataItemResponse>> getLoanTerms() {
        return ApiResponse.success(referenceDataService.getLoanTerms());
    }

    @Operation(summary = "Lấy danh mục loại tài sản")
    @GetMapping("/asset-types")
    public ApiResponse<List<ReferenceDataItemResponse>> getAssetTypes() {
        return ApiResponse.success(referenceDataService.getAssetTypes());
    }

    @Operation(summary = "Lấy danh mục hãng xe")
    @GetMapping("/vehicle-brands")
    public ApiResponse<List<ReferenceDataItemResponse>> getVehicleBrands(@RequestParam(required = false) AssetType assetType) {
        return ApiResponse.success(referenceDataService.getVehicleBrands(assetType));
    }

    @Operation(summary = "Lấy danh mục dòng xe")
    @GetMapping("/vehicle-models")
    public ApiResponse<List<ReferenceDataItemResponse>> getVehicleModels(@RequestParam(required = false) String brandCode) {
        return ApiResponse.success(referenceDataService.getVehicleModels(brandCode));
    }

    @Operation(summary = "Lấy danh mục phiên bản xe")
    @GetMapping("/vehicle-versions")
    public ApiResponse<List<ReferenceDataItemResponse>> getVehicleVersions(@RequestParam(required = false) String modelCode) {
        return ApiResponse.success(referenceDataService.getVehicleVersions(modelCode));
    }

    @Operation(
            summary = "Lấy năm sản xuất theo dòng xe và phiên bản xe",
            description = "Trả các năm sản xuất có dữ liệu variant thật trong catalog theo modelCode và versionCode đã chọn. versionCode chỉ unique trong phạm vi model."
    )
    @GetMapping("/manufacture-years")
    public ApiResponse<List<ReferenceDataItemResponse>> getManufactureYears(
            @RequestParam String modelCode,
            @RequestParam String versionCode
    ) {
        return ApiResponse.success(referenceDataService.getManufactureYears(modelCode, versionCode));
    }

    @Operation(
            summary = "Lấy màu xe theo dòng xe, phiên bản và năm sản xuất",
            description = "Không trả toàn bộ bảng màu master. API join vehicle_variant, vehicle_year và vehicle_color để chỉ trả các màu có thật cho modelCode, versionCode và manufactureYear đã chọn."
    )
    @GetMapping("/vehicle-colors")
    public ApiResponse<List<ReferenceDataItemResponse>> getVehicleColors(
            @RequestParam String modelCode,
            @RequestParam String versionCode,
            @RequestParam Integer manufactureYear
    ) {
        return ApiResponse.success(referenceDataService.getVehicleColors(modelCode, versionCode, manufactureYear));
    }

    @Operation(
            summary = "Resolve biến thể xe cuối cùng theo dòng xe, phiên bản, năm và màu",
            description = "Dùng sau khi user đã chọn modelCode, versionCode, manufactureYear và colorCode. API trả vehicleVariant duy nhất để lưu asset snapshot, preview valuation hoặc lấy market price."
    )
    @GetMapping("/vehicle-variant")
    public ApiResponse<ReferenceDataItemResponse> resolveVehicleVariant(
            @RequestParam String modelCode,
            @RequestParam String versionCode,
            @RequestParam Integer manufactureYear,
            @RequestParam String colorCode
    ) {
        return ApiResponse.success(referenceDataService.resolveVehicleVariant(modelCode, versionCode, manufactureYear, colorCode));
    }

    @Operation(summary = "Lấy danh mục yếu tố giảm trừ định giá")
    @GetMapping("/valuation-deduction-factors")
    public ApiResponse<List<ReferenceDataItemResponse>> getValuationDeductionFactors() {
        return ApiResponse.success(referenceDataService.getValuationDeductionFactors());
    }
}
