package com.f88.loanonboarding.service.impl;

import java.time.Year;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.f88.loanonboarding.dto.response.common.ReferenceDataItemResponse;
import com.f88.loanonboarding.repository.LoanApplicationStateRepository;
import com.f88.loanonboarding.service.ReferenceDataService;

@Service
public class ReferenceDataServiceImpl implements ReferenceDataService {

    private final LoanApplicationStateRepository stateRepository;

    public ReferenceDataServiceImpl(LoanApplicationStateRepository stateRepository) {
        this.stateRepository = stateRepository;
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
        int currentYear = Year.now().getValue();
        return java.util.stream.IntStream.rangeClosed(currentYear - 20, currentYear)
                .mapToObj(year -> new ReferenceDataItemResponse(String.valueOf(year), String.valueOf(year), null))
                .toList();
    }

    @Override
    public List<ReferenceDataItemResponse> getVehicleColors() {
        return List.of();
    }

    @Override
    public List<ReferenceDataItemResponse> getValuationDeductionFactors() {
        return List.of();
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
