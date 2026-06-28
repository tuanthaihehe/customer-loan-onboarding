package com.f88.loanonboarding.service;

import java.util.List;

import com.f88.loanonboarding.dto.response.common.ReferenceDataItemResponse;

public interface ReferenceDataService {

    List<ReferenceDataItemResponse> getGenders();

    List<ReferenceDataItemResponse> getOccupations();

    List<ReferenceDataItemResponse> getLoanPurposes();

    List<ReferenceDataItemResponse> getAssetTypes();

    List<ReferenceDataItemResponse> getVehicleBrands(String assetType);

    List<ReferenceDataItemResponse> getVehicleModels(String brandCode);

    List<ReferenceDataItemResponse> getVehicleVariants(String modelCode);

    List<ReferenceDataItemResponse> getManufactureYears();

    List<ReferenceDataItemResponse> getVehicleColors();

    List<ReferenceDataItemResponse> getValuationDeductionFactors();
}
