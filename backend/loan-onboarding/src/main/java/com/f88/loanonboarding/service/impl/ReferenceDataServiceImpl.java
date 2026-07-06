package com.f88.loanonboarding.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.f88.loanonboarding.common.error.ErrorCode;
import com.f88.loanonboarding.dto.response.common.ReferenceDataItemResponse;
import com.f88.loanonboarding.enums.AssetType;
import com.f88.loanonboarding.exception.BusinessException;
import com.f88.loanonboarding.repository.AssetDeductionTypeRepository;
import com.f88.loanonboarding.repository.BankRepository;
import com.f88.loanonboarding.repository.IncomeSourceRepository;
import com.f88.loanonboarding.repository.LoanApplicationStateRepository;
import com.f88.loanonboarding.repository.LoanApplicationStepRepository;
import com.f88.loanonboarding.repository.LoanPurposeRepository;
import com.f88.loanonboarding.repository.LoanTermRepository;
import com.f88.loanonboarding.repository.OccupationRepository;
import com.f88.loanonboarding.repository.VehicleBrandRepository;
import com.f88.loanonboarding.repository.VehicleColorRepository;
import com.f88.loanonboarding.repository.VehicleModelRepository;
import com.f88.loanonboarding.repository.VehicleTypeRepository;
import com.f88.loanonboarding.repository.VehicleVariantRepository;
import com.f88.loanonboarding.repository.VehicleVersionRepository;
import com.f88.loanonboarding.repository.VehicleYearRepository;
import com.f88.loanonboarding.service.ReferenceDataService;

@Service
public class ReferenceDataServiceImpl implements ReferenceDataService {

    private final LoanApplicationStateRepository stateRepository;
    private final LoanPurposeRepository loanPurposeRepository;
    private final LoanTermRepository loanTermRepository;
    private final LoanApplicationStepRepository loanApplicationStepRepository;
    private final OccupationRepository occupationRepository;
    private final BankRepository bankRepository;
    private final IncomeSourceRepository incomeSourceRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final VehicleBrandRepository vehicleBrandRepository;
    private final VehicleModelRepository vehicleModelRepository;
    private final VehicleVersionRepository vehicleVersionRepository;
    private final VehicleVariantRepository vehicleVariantRepository;
    private final VehicleYearRepository vehicleYearRepository;
    private final VehicleColorRepository vehicleColorRepository;
    private final AssetDeductionTypeRepository assetDeductionTypeRepository;

    public ReferenceDataServiceImpl(
            LoanApplicationStateRepository stateRepository,
            LoanPurposeRepository loanPurposeRepository,
            LoanTermRepository loanTermRepository,
            LoanApplicationStepRepository loanApplicationStepRepository,
            OccupationRepository occupationRepository,
            BankRepository bankRepository,
            IncomeSourceRepository incomeSourceRepository,
            VehicleTypeRepository vehicleTypeRepository,
            VehicleBrandRepository vehicleBrandRepository,
            VehicleModelRepository vehicleModelRepository,
            VehicleVersionRepository vehicleVersionRepository,
            VehicleVariantRepository vehicleVariantRepository,
            VehicleYearRepository vehicleYearRepository,
            VehicleColorRepository vehicleColorRepository,
            AssetDeductionTypeRepository assetDeductionTypeRepository
    ) {
        this.stateRepository = stateRepository;
        this.loanPurposeRepository = loanPurposeRepository;
        this.loanTermRepository = loanTermRepository;
        this.loanApplicationStepRepository = loanApplicationStepRepository;
        this.occupationRepository = occupationRepository;
        this.bankRepository = bankRepository;
        this.incomeSourceRepository = incomeSourceRepository;
        this.vehicleTypeRepository = vehicleTypeRepository;
        this.vehicleBrandRepository = vehicleBrandRepository;
        this.vehicleModelRepository = vehicleModelRepository;
        this.vehicleVersionRepository = vehicleVersionRepository;
        this.vehicleVariantRepository = vehicleVariantRepository;
        this.vehicleYearRepository = vehicleYearRepository;
        this.vehicleColorRepository = vehicleColorRepository;
        this.assetDeductionTypeRepository = assetDeductionTypeRepository;
    }

    @Override
    public List<ReferenceDataItemResponse> getGenders() {
        return List.of(
                new ReferenceDataItemResponse("MALE", "Nam", null),
                new ReferenceDataItemResponse("FEMALE", "Nữ", null)
        );
    }

    @Override
    public List<ReferenceDataItemResponse> getMaritalStatuses() {
        return List.of(
                new ReferenceDataItemResponse("SINGLE", "Độc thân", null),
                new ReferenceDataItemResponse("MARRIED", "Đã kết hôn", null)
        );
    }

    @Override
    public List<ReferenceDataItemResponse> getOccupations() {
        return occupationRepository.findByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(item -> new ReferenceDataItemResponse(item.getCode(), item.getName(), item.getDescription()))
                .toList();
    }

    @Override
    public List<ReferenceDataItemResponse> getIncomeSources() {
        return incomeSourceRepository.findByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(item -> new ReferenceDataItemResponse(item.getCode(), item.getName(), item.getDescription()))
                .toList();
    }

    @Override
    public List<ReferenceDataItemResponse> getBanks() {
        return bankRepository.findByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(item -> new ReferenceDataItemResponse(item.getCode(), item.getName(), item.getShortName()))
                .toList();
    }

    @Override
    public List<ReferenceDataItemResponse> getReferencePersonRelationships() {
        return List.of(
                new ReferenceDataItemResponse("FATHER", "Bố", null),
                new ReferenceDataItemResponse("MOTHER", "Mẹ", null),
                new ReferenceDataItemResponse("SPOUSE", "Vợ/chồng", null),
                new ReferenceDataItemResponse("SIBLING", "Anh/chị/em", null),
                new ReferenceDataItemResponse("RELATIVE", "Người thân", null),
                new ReferenceDataItemResponse("FRIEND", "Bạn bè", null),
                new ReferenceDataItemResponse("COLLEAGUE", "Đồng nghiệp", null),
                new ReferenceDataItemResponse("OTHER", "Khác", null)
        );
    }

    @Override
    public List<ReferenceDataItemResponse> getLoanPurposes() {
        return loanPurposeRepository.findByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(item -> new ReferenceDataItemResponse(item.getCode(), item.getName(), item.getDescription()))
                .toList();
    }

    @Override
    public List<ReferenceDataItemResponse> getLoanTerms() {
        return loanTermRepository.findByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(item -> new ReferenceDataItemResponse(
                        String.valueOf(item.getTermMonths()),
                        item.getName(),
                        item.getDescription()
                ))
                .toList();
    }

    @Override
    public List<ReferenceDataItemResponse> getLoanApplicationSteps() {
        return loanApplicationStepRepository.findByActiveTrueOrderByStepOrderAsc()
                .stream()
                .map(item -> new ReferenceDataItemResponse(item.getCode(), item.getName(), item.getDescription()))
                .toList();
    }

    @Override
    public List<ReferenceDataItemResponse> getAssetTypes() {
        return vehicleTypeRepository.findByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(item -> new ReferenceDataItemResponse(item.getCode(), item.getName(), item.getDescription()))
                .toList();
    }

    @Override
    public List<ReferenceDataItemResponse> getVehicleBrands(AssetType assetType) {
        var brands = assetType == null
                ? vehicleBrandRepository.findByActiveTrueOrderBySortOrderAsc()
                : vehicleBrandRepository.findByVehicleType_CodeAndActiveTrueOrderBySortOrderAsc(assetType.code());
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
    public List<ReferenceDataItemResponse> getVehicleVersions(String modelCode) {
        if (modelCode == null || modelCode.isBlank()) {
            return List.of();
        }
        return vehicleVersionRepository.findByVehicleModel_CodeAndActiveTrueOrderBySortOrderAsc(modelCode)
                .stream()
                .map(item -> new ReferenceDataItemResponse(item.getCode(), item.getName(), null))
                .toList();
    }

    @Override
    public List<ReferenceDataItemResponse> getManufactureYears(String modelCode, String versionCode) {
        if (modelCode == null || modelCode.isBlank() || versionCode == null || versionCode.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "modelCode và versionCode là bắt buộc để lấy năm sản xuất.");
        }
        return vehicleYearRepository.findActiveManufactureYearsByModelCodeAndVersionCode(modelCode, versionCode)
                .stream()
                .map(year -> new ReferenceDataItemResponse(String.valueOf(year), String.valueOf(year), null))
                .toList();
    }

    @Override
    public List<ReferenceDataItemResponse> getVehicleColors(String modelCode, String versionCode, Integer manufactureYear) {
        if (modelCode == null || modelCode.isBlank() || versionCode == null || versionCode.isBlank() || manufactureYear == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "modelCode, versionCode và manufactureYear là bắt buộc để lấy màu xe.");
        }
        return vehicleColorRepository.findActiveByModelCodeAndVersionCodeAndManufactureYear(modelCode, versionCode, manufactureYear)
                .stream()
                .map(item -> new ReferenceDataItemResponse(item.getCode(), item.getName(), null))
                .toList();
    }

    @Override
    public ReferenceDataItemResponse resolveVehicleVariant(String modelCode, String versionCode, Integer manufactureYear, String colorCode) {
        if (modelCode == null || modelCode.isBlank() || versionCode == null || versionCode.isBlank() || manufactureYear == null || colorCode == null || colorCode.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "modelCode, versionCode, manufactureYear và colorCode là bắt buộc.");
        }
        return vehicleVariantRepository
                .findActiveByModelCodeAndVersionCodeAndManufactureYearAndColorCode(modelCode, versionCode, manufactureYear, colorCode)
                .map(item -> new ReferenceDataItemResponse(item.getCode(), item.getName(), null))
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "Không tìm thấy vehicle variant phù hợp với version, năm sản xuất và màu xe."
                ));
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
