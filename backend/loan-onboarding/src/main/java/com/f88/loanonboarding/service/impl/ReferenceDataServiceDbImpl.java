package com.f88.loanonboarding.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.f88.loanonboarding.dto.response.common.ReferenceDataItemResponse;
import com.f88.loanonboarding.service.ReferenceDataService;

@Service
public class ReferenceDataServiceDbImpl implements ReferenceDataService {

    @Override
    public List<ReferenceDataItemResponse> getGenders() {
        return List.of();
    }

    @Override
    public List<ReferenceDataItemResponse> getOccupations() {
        return List.of();
    }

    @Override
    public List<ReferenceDataItemResponse> getLoanPurposes() {
        return List.of(
                item("BUSINESS", "Kinh doanh", "Business or working capital purpose"),
                item("PERSONAL_CONSUMPTION", "Tiêu dùng cá nhân", "Personal consumption purpose"),
                item("VEHICLE_REPAIR", "Sửa chữa xe", "Vehicle repair or maintenance purpose"),
                item("MEDICAL", "Y tế", "Medical expense purpose"),
                item("EDUCATION", "Giáo dục", "Education expense purpose"),
                item("HOME_REPAIR", "Sửa nhà", "Home repair or renovation purpose"),
                item("DEBT_REPAYMENT", "Trả nợ", "Debt repayment or refinancing purpose"),
                item("OTHER", "Khác", "Other purpose not covered by predefined values")
        );
    }

    @Override
    public List<ReferenceDataItemResponse> getAssetTypes() {
        return List.of();
    }

    @Override
    public List<ReferenceDataItemResponse> getVehicleBrands(String assetType) {
        return List.of();
    }

    @Override
    public List<ReferenceDataItemResponse> getVehicleModels(String brandCode) {
        return List.of();
    }

    @Override
    public List<ReferenceDataItemResponse> getVehicleVariants(String modelCode) {
        return List.of();
    }

    @Override
    public List<ReferenceDataItemResponse> getManufactureYears() {
        return List.of();
    }

    @Override
    public List<ReferenceDataItemResponse> getVehicleColors() {
        return List.of();
    }

    @Override
    public List<ReferenceDataItemResponse> getValuationDeductionFactors() {
        return List.of();
    }

    private ReferenceDataItemResponse item(String code, String name, String description) {
        return new ReferenceDataItemResponse(code, name, description);
    }
}
