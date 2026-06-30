package com.f88.loanonboarding.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.f88.loanonboarding.dto.response.common.ReferenceDataItemResponse;
import com.f88.loanonboarding.entity.AssetDeductionTypeEntity;
import com.f88.loanonboarding.entity.LoanPurposeEntity;
import com.f88.loanonboarding.entity.VehicleBrandEntity;
import com.f88.loanonboarding.entity.VehicleColorEntity;
import com.f88.loanonboarding.entity.VehicleModelEntity;
import com.f88.loanonboarding.entity.VehicleTypeEntity;
import com.f88.loanonboarding.entity.VehicleVersionEntity;
import com.f88.loanonboarding.repository.AssetDeductionTypeRepository;
import com.f88.loanonboarding.repository.LoanPurposeRepository;
import com.f88.loanonboarding.repository.VehicleBrandRepository;
import com.f88.loanonboarding.repository.VehicleColorRepository;
import com.f88.loanonboarding.repository.VehicleModelRepository;
import com.f88.loanonboarding.repository.VehicleTypeRepository;
import com.f88.loanonboarding.repository.VehicleVersionRepository;
import com.f88.loanonboarding.repository.VehicleYearRepository;
import com.f88.loanonboarding.service.ReferenceDataService;

@Service
public class ReferenceDataServiceDbImpl implements ReferenceDataService {

    private final LoanPurposeRepository loanPurposeRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final VehicleBrandRepository vehicleBrandRepository;
    private final VehicleModelRepository vehicleModelRepository;
    private final VehicleVersionRepository vehicleVersionRepository;
    private final VehicleYearRepository vehicleYearRepository;
    private final VehicleColorRepository vehicleColorRepository;
    private final AssetDeductionTypeRepository assetDeductionTypeRepository;

    public ReferenceDataServiceDbImpl(
            LoanPurposeRepository loanPurposeRepository,
            VehicleTypeRepository vehicleTypeRepository,
            VehicleBrandRepository vehicleBrandRepository,
            VehicleModelRepository vehicleModelRepository,
            VehicleVersionRepository vehicleVersionRepository,
            VehicleYearRepository vehicleYearRepository,
            VehicleColorRepository vehicleColorRepository,
            AssetDeductionTypeRepository assetDeductionTypeRepository
    ) {
        this.loanPurposeRepository = loanPurposeRepository;
        this.vehicleTypeRepository = vehicleTypeRepository;
        this.vehicleBrandRepository = vehicleBrandRepository;
        this.vehicleModelRepository = vehicleModelRepository;
        this.vehicleVersionRepository = vehicleVersionRepository;
        this.vehicleYearRepository = vehicleYearRepository;
        this.vehicleColorRepository = vehicleColorRepository;
        this.assetDeductionTypeRepository = assetDeductionTypeRepository;
    }

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
        return loanPurposeRepository.findByActiveTrueOrderBySortOrderAscNameAsc()
                .stream()
                .map(this::item)
                .toList();
    }

    @Override
    public List<ReferenceDataItemResponse> getAssetTypes() {
        return vehicleTypeRepository.findByActiveTrueOrderBySortOrderAscNameAsc()
                .stream()
                .map(this::item)
                .toList();
    }

    @Override
    public List<ReferenceDataItemResponse> getVehicleBrands(String assetType) {
        List<VehicleBrandEntity> brands = assetType == null || assetType.isBlank()
                ? vehicleBrandRepository.findByActiveTrueAndVehicleTypeActiveTrueOrderBySortOrderAscNameAsc()
                : vehicleBrandRepository.findByActiveTrueAndVehicleTypeActiveTrueAndVehicleTypeCodeOrderBySortOrderAscNameAsc(assetType);
        return brands.stream().map(this::item).toList();
    }

    @Override
    public List<ReferenceDataItemResponse> getVehicleModels(String brandCode) {
        List<VehicleModelEntity> models = brandCode == null || brandCode.isBlank()
                ? vehicleModelRepository.findByActiveTrueAndVehicleBrandActiveTrueOrderBySortOrderAscNameAsc()
                : vehicleModelRepository.findByActiveTrueAndVehicleBrandActiveTrueAndVehicleBrandCodeOrderBySortOrderAscNameAsc(brandCode);
        return models.stream().map(this::item).toList();
    }

    @Override
    public List<ReferenceDataItemResponse> getVehicleVariants(String modelCode) {
        List<VehicleVersionEntity> versions = modelCode == null || modelCode.isBlank()
                ? vehicleVersionRepository.findByActiveTrueAndVehicleModelActiveTrueOrderBySortOrderAscNameAsc()
                : vehicleVersionRepository.findByActiveTrueAndVehicleModelActiveTrueAndVehicleModelCodeOrderBySortOrderAscNameAsc(modelCode);
        return versions.stream().map(this::item).toList();
    }

    @Override
    public List<ReferenceDataItemResponse> getManufactureYears() {
        return vehicleYearRepository.findActiveManufactureYears()
                .stream()
                .map(year -> item(year.toString(), year.toString(), null))
                .toList();
    }

    @Override
    public List<ReferenceDataItemResponse> getVehicleColors() {
        return vehicleColorRepository.findByActiveTrueOrderBySortOrderAscNameAsc()
                .stream()
                .map(this::item)
                .toList();
    }

    @Override
    public List<ReferenceDataItemResponse> getValuationDeductionFactors() {
        return assetDeductionTypeRepository.findByActiveTrueOrderBySortOrderAscNameAsc()
                .stream()
                .map(this::item)
                .toList();
    }

    private ReferenceDataItemResponse item(LoanPurposeEntity entity) {
        return item(entity.getCode(), entity.getName(), entity.getDescription());
    }

    private ReferenceDataItemResponse item(VehicleTypeEntity entity) {
        return item(entity.getCode(), entity.getName(), entity.getDescription());
    }

    private ReferenceDataItemResponse item(VehicleBrandEntity entity) {
        return item(entity.getCode(), entity.getName(), null);
    }

    private ReferenceDataItemResponse item(VehicleModelEntity entity) {
        return item(entity.getCode(), entity.getName(), null);
    }

    private ReferenceDataItemResponse item(VehicleVersionEntity entity) {
        return item(entity.getCode(), entity.getName(), null);
    }

    private ReferenceDataItemResponse item(VehicleColorEntity entity) {
        return item(entity.getCode(), entity.getName(), null);
    }

    private ReferenceDataItemResponse item(AssetDeductionTypeEntity entity) {
        return item(entity.getCode(), entity.getName(), entity.getDescription());
    }

    private ReferenceDataItemResponse item(String code, String name, String description) {
        return new ReferenceDataItemResponse(code, name, description);
    }
}
