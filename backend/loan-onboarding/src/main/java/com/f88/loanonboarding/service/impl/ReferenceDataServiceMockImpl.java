package com.f88.loanonboarding.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.f88.loanonboarding.dto.response.common.ReferenceDataItemResponse;
import com.f88.loanonboarding.mock.DemoReferenceDataMockDataProvider;
import com.f88.loanonboarding.service.ReferenceDataService;

@Service
public class ReferenceDataServiceMockImpl implements ReferenceDataService {

    private final DemoReferenceDataMockDataProvider mockDataProvider;

    public ReferenceDataServiceMockImpl(DemoReferenceDataMockDataProvider mockDataProvider) {
        this.mockDataProvider = mockDataProvider;
    }

    @Override
    public List<ReferenceDataItemResponse> getGenders() {
        return mockDataProvider.genders();
    }

    @Override
    public List<ReferenceDataItemResponse> getOccupations() {
        return mockDataProvider.occupations();
    }

    @Override
    public List<ReferenceDataItemResponse> getLoanPurposes() {
        return mockDataProvider.loanPurposes();
    }

    @Override
    public List<ReferenceDataItemResponse> getAssetTypes() {
        return mockDataProvider.assetTypes();
    }

    @Override
    public List<ReferenceDataItemResponse> getVehicleBrands(String assetType) {
        return mockDataProvider.vehicleBrands();
    }

    @Override
    public List<ReferenceDataItemResponse> getVehicleModels(String brandCode) {
        return mockDataProvider.vehicleModels();
    }

    @Override
    public List<ReferenceDataItemResponse> getVehicleVariants(String modelCode) {
        return mockDataProvider.vehicleVariants();
    }

    @Override
    public List<ReferenceDataItemResponse> getManufactureYears() {
        return mockDataProvider.manufactureYears();
    }

    @Override
    public List<ReferenceDataItemResponse> getVehicleColors() {
        return mockDataProvider.vehicleColors();
    }

    @Override
    public List<ReferenceDataItemResponse> getValuationDeductionFactors() {
        return mockDataProvider.valuationDeductionFactors();
    }
}
