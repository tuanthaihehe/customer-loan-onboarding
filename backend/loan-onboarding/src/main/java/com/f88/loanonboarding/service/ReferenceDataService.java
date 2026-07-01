package com.f88.loanonboarding.service;

import java.util.List;

import com.f88.loanonboarding.dto.response.common.ReferenceDataItemResponse;
import com.f88.loanonboarding.enums.AssetType;

public interface ReferenceDataService {

    List<ReferenceDataItemResponse> getGenders();

    List<ReferenceDataItemResponse> getOccupations();

    List<ReferenceDataItemResponse> getLoanPurposes();

    List<ReferenceDataItemResponse> getLoanTerms();

    List<ReferenceDataItemResponse> getAssetTypes();

    List<ReferenceDataItemResponse> getVehicleBrands(AssetType assetType);

    List<ReferenceDataItemResponse> getVehicleModels(String brandCode);

    List<ReferenceDataItemResponse> getVehicleVersions(String modelCode);

    List<ReferenceDataItemResponse> getVehicleVariants(String modelCode);

    List<ReferenceDataItemResponse> getManufactureYears(String versionCode);

    List<ReferenceDataItemResponse> getVehicleColors(String versionCode, Integer manufactureYear);

    ReferenceDataItemResponse resolveVehicleVariant(String versionCode, Integer manufactureYear, String colorCode);

    List<ReferenceDataItemResponse> getValuationDeductionFactors();
}
