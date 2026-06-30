package com.f88.loanonboarding.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.f88.loanonboarding.dto.response.common.ReferenceDataItemResponse;
import com.f88.loanonboarding.repository.AssetDeductionTypeRepository;
import com.f88.loanonboarding.repository.LoanApplicationStateRepository;
import com.f88.loanonboarding.repository.VehicleBrandRepository;
import com.f88.loanonboarding.repository.VehicleColorRepository;
import com.f88.loanonboarding.repository.VehicleModelRepository;
import com.f88.loanonboarding.repository.VehicleTypeRepository;
import com.f88.loanonboarding.repository.VehicleVariantRepository;
import com.f88.loanonboarding.repository.VehicleYearRepository;
import com.f88.loanonboarding.service.ReferenceDataService;

@Service
public class ReferenceDataServiceImpl implements ReferenceDataService {

    private final LoanApplicationStateRepository stateRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final VehicleBrandRepository vehicleBrandRepository;
    private final VehicleModelRepository vehicleModelRepository;
    private final VehicleVariantRepository vehicleVariantRepository;
    private final VehicleYearRepository vehicleYearRepository;
    private final VehicleColorRepository vehicleColorRepository;
    private final AssetDeductionTypeRepository assetDeductionTypeRepository;

    public ReferenceDataServiceImpl(
            LoanApplicationStateRepository stateRepository,
            VehicleTypeRepository vehicleTypeRepository,
            VehicleBrandRepository vehicleBrandRepository,
            VehicleModelRepository vehicleModelRepository,
            VehicleVariantRepository vehicleVariantRepository,
            VehicleYearRepository vehicleYearRepository,
            VehicleColorRepository vehicleColorRepository,
            AssetDeductionTypeRepository assetDeductionTypeRepository
    ) {
        this.stateRepository = stateRepository;
        this.vehicleTypeRepository = vehicleTypeRepository;
        this.vehicleBrandRepository = vehicleBrandRepository;
        this.vehicleModelRepository = vehicleModelRepository;
        this.vehicleVariantRepository = vehicleVariantRepository;
        this.vehicleYearRepository = vehicleYearRepository;
        this.vehicleColorRepository = vehicleColorRepository;
        this.assetDeductionTypeRepository = assetDeductionTypeRepository;
    }

    @Override
    public List<ReferenceDataItemResponse> getGenders() {
        return List.of(
                new ReferenceDataItemResponse("MALE", "Nam", null),
                new ReferenceDataItemResponse("FEMALE", "Nu", null),
                new ReferenceDataItemResponse("OTHER", "Khac", null)
        );
    }

    @Override
    public List<ReferenceDataItemResponse> getOccupations() {
        return List.of();
    }

    @Override
    public List<ReferenceDataItemResponse> getLoanPurposes() {
        return List.of(
                new ReferenceDataItemResponse("BUSINESS", "Kinh doanh", null),
                new ReferenceDataItemResponse("PERSONAL_CONSUMPTION", "Tieu dung ca nhan", null),
                new ReferenceDataItemResponse("VEHICLE_REPAIR", "Sua chua xe", null),
                new ReferenceDataItemResponse("MEDICAL", "Y te", null),
                new ReferenceDataItemResponse("EDUCATION", "Giao duc", null),
                new ReferenceDataItemResponse("HOME_REPAIR", "Sua nha", null),
                new ReferenceDataItemResponse("DEBT_REPAYMENT", "Tat toan khoan vay", null),
                new ReferenceDataItemResponse("OTHER", "Khac", null)
        );
    }

    @Override
    public List<ReferenceDataItemResponse> getAssetTypes() {
        return vehicleTypeRepository.findByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(item -> new ReferenceDataItemResponse(item.getCode(), item.getName(), item.getDescription()))
                .toList();
    }

    @Override
    public List<ReferenceDataItemResponse> getVehicleBrands(String assetType) {
        var brands = assetType == null || assetType.isBlank()
                ? vehicleBrandRepository.findByActiveTrueOrderBySortOrderAsc()
                : vehicleBrandRepository.findByVehicleType_CodeAndActiveTrueOrderBySortOrderAsc(assetType);
        return brands.stream()
                .map(item -> new ReferenceDataItemResponse(item.getCode(), item.getName(), null))
                .toList();
    }

    @Override
    public List<ReferenceDataItemResponse> getVehicleModels(String brandCode) {
        if (brandCode == null || brandCode.isBlank()) {
            return List.of();
        }
        return vehicleModelRepository.findByVehicleBrand_CodeAndActiveTrueOrderBySortOrderAsc(brandCode)
                .stream()
                .map(item -> new ReferenceDataItemResponse(item.getCode(), item.getName(), null))
                .toList();
    }

    @Override
    public List<ReferenceDataItemResponse> getVehicleVariants(String modelCode) {
        if (modelCode == null || modelCode.isBlank()) {
            return List.of();
        }
        return vehicleVariantRepository.findActiveByModelCode(modelCode)
                .stream()
                .map(item -> new ReferenceDataItemResponse(item.getCode(), item.getName(), null))
                .toList();
    }

    @Override
    public List<ReferenceDataItemResponse> getManufactureYears() {
        return vehicleYearRepository.findActiveManufactureYears()
                .stream()
                .map(year -> new ReferenceDataItemResponse(String.valueOf(year), String.valueOf(year), null))
                .toList();
    }

    @Override
    public List<ReferenceDataItemResponse> getVehicleColors() {
        return vehicleColorRepository.findByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(item -> new ReferenceDataItemResponse(item.getCode(), item.getName(), null))
                .toList();
    }

    @Override
    public List<ReferenceDataItemResponse> getValuationDeductionFactors() {
        return assetDeductionTypeRepository.findByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(item -> new ReferenceDataItemResponse(
                        item.getCode(),
                        item.getName(),
                        item.getDeductionAmount().toPlainString()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReferenceDataItemResponse> getLoanApplicationStates() {
        return stateRepository.findAllByOrderBySortOrderAsc()
                .stream()
                .map(state -> new ReferenceDataItemResponse(
                        state.getCode(),
                        state.getName(),
                        state.getDescription()
                ))
                .toList();
    }
}
