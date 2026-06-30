package com.f88.loanonboarding.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.f88.loanonboarding.common.error.ErrorCode;
import com.f88.loanonboarding.dto.request.asset.AssetValuationPreviewRequest;
import com.f88.loanonboarding.dto.request.asset.ValuationDeductionItemRequest;
import com.f88.loanonboarding.dto.response.asset.AssetValuationPreviewResponse;
import com.f88.loanonboarding.dto.response.asset.VehicleMarketPriceResponse;
import com.f88.loanonboarding.entity.Asset;
import com.f88.loanonboarding.entity.AssetDeductionType;
import com.f88.loanonboarding.entity.AssetValuation;
import com.f88.loanonboarding.entity.AssetValuationDeduction;
import com.f88.loanonboarding.entity.LoanApplication;
import com.f88.loanonboarding.entity.VehicleMarketPrice;
import com.f88.loanonboarding.entity.VehicleVariant;
import com.f88.loanonboarding.enums.AssetValuationState;
import com.f88.loanonboarding.exception.BusinessException;
import com.f88.loanonboarding.repository.AssetDeductionTypeRepository;
import com.f88.loanonboarding.repository.AssetValuationDeductionRepository;
import com.f88.loanonboarding.repository.AssetValuationRepository;
import com.f88.loanonboarding.repository.LoanApplicationRepository;
import com.f88.loanonboarding.repository.VehicleMarketPriceRepository;
import com.f88.loanonboarding.repository.VehicleVariantRepository;
import com.f88.loanonboarding.service.AssetValuationService;

@Service
public class AssetValuationServiceImpl implements AssetValuationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final VehicleVariantRepository vehicleVariantRepository;
    private final VehicleMarketPriceRepository vehicleMarketPriceRepository;
    private final AssetDeductionTypeRepository assetDeductionTypeRepository;
    private final AssetValuationRepository assetValuationRepository;
    private final AssetValuationDeductionRepository assetValuationDeductionRepository;

    public AssetValuationServiceImpl(
            LoanApplicationRepository loanApplicationRepository,
            VehicleVariantRepository vehicleVariantRepository,
            VehicleMarketPriceRepository vehicleMarketPriceRepository,
            AssetDeductionTypeRepository assetDeductionTypeRepository,
            AssetValuationRepository assetValuationRepository,
            AssetValuationDeductionRepository assetValuationDeductionRepository
    ) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.vehicleVariantRepository = vehicleVariantRepository;
        this.vehicleMarketPriceRepository = vehicleMarketPriceRepository;
        this.assetDeductionTypeRepository = assetDeductionTypeRepository;
        this.assetValuationRepository = assetValuationRepository;
        this.assetValuationDeductionRepository = assetValuationDeductionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AssetValuationPreviewResponse preview(AssetValuationPreviewRequest request) {
        ValuationCalculation calculation = calculate(request);
        return toResponse(null, calculation, AssetValuationState.VAL_RECORDED);
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleMarketPriceResponse getMarketPrice(String vehicleVariant) {
        VehicleVariant variant = resolveVehicleVariant(vehicleVariant);
        VehicleMarketPrice marketPrice = resolveCurrentMarketPrice(variant);
        return new VehicleMarketPriceResponse(
                variant.getCode(),
                variant.getName(),
                marketPrice.getPriceAmount(),
                marketPrice.getCurrencyCode(),
                marketPrice.getPriceSource(),
                marketPrice.getEffectiveFrom(),
                marketPrice.getEffectiveTo()
        );
    }

    @Override
    @Transactional
    public AssetValuationPreviewResponse savePreview(String applicationCode, AssetValuationPreviewRequest request) {
        LoanApplication application = loanApplicationRepository.findByLoanApplicationCode(applicationCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOAN_APPLICATION_NOT_FOUND));
        Asset asset = application.getAsset();
        if (asset == null) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Ho so chua gan tai san. Hay luu asset snapshot truoc khi luu dinh gia."
            );
        }

        ValuationCalculation calculation = calculate(request);
        AssetValuation valuation = new AssetValuation();
        valuation.setAsset(asset);
        valuation.setMarketPriceAmount(calculation.marketValue());
        valuation.setTotalDeductionAmount(calculation.totalDeductionAmount());
        valuation.setFinalValueAmount(calculation.finalValue());
        valuation.setValuationSource("SYSTEM");
        valuation = assetValuationRepository.save(valuation);

        for (AssetDeductionType deductionType : calculation.deductionTypes()) {
            AssetValuationDeduction deduction = new AssetValuationDeduction();
            deduction.setAssetValuation(valuation);
            deduction.setDeductionType(deductionType);
            deduction.setDeductionAmountSnapshot(deductionType.getDeductionAmount());
            assetValuationDeductionRepository.save(deduction);
        }

        return toResponse(applicationCode, calculation, AssetValuationState.VAL_ACTIVE);
    }

    private void ensureApplicationExists(String applicationCode) {
        if (loanApplicationRepository.findByLoanApplicationCode(applicationCode).isEmpty()) {
            throw new BusinessException(ErrorCode.LOAN_APPLICATION_NOT_FOUND);
        }
    }

    private ValuationCalculation calculate(AssetValuationPreviewRequest request) {
        String variantCode = request.assetSnapshot().vehicleVariant();
        if (variantCode == null || variantCode.isBlank()) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "vehicleVariant la bat buoc de tinh gia thi truong."
            );
        }

        VehicleVariant variant = resolveVehicleVariant(variantCode);
        VehicleMarketPrice marketPrice = resolveCurrentMarketPrice(variant);

        List<AssetDeductionType> deductionTypes = resolveDeductionTypes(request.deductionItems());
        BigDecimal totalDeductionAmount = deductionTypes.stream()
                .map(AssetDeductionType::getDeductionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalDeductionAmount.compareTo(marketPrice.getPriceAmount()) > 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_VALUATION_VALUE,
                    "Tong giam tru khong duoc lon hon gia thi truong."
            );
        }
        BigDecimal finalValue = marketPrice.getPriceAmount().subtract(totalDeductionAmount);
        return new ValuationCalculation(
                marketPrice.getPriceAmount(),
                BigDecimal.ZERO,
                totalDeductionAmount,
                finalValue,
                null,
                null,
                deductionTypes
        );
    }

    private VehicleVariant resolveVehicleVariant(String variantCode) {
        if (variantCode == null || variantCode.isBlank()) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "vehicleVariant la bat buoc de tinh gia thi truong."
            );
        }
        return vehicleVariantRepository.findByCode(variantCode)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "Khong tim thay vehicle variant: " + variantCode
                ));
    }

    private VehicleMarketPrice resolveCurrentMarketPrice(VehicleVariant variant) {
        return vehicleMarketPriceRepository
                .findEffectivePrices(variant, LocalDate.now())
                .stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "Khong tim thay gia thi truong hien hanh cho variant: " + variant.getCode()
                ));
    }

    private List<AssetDeductionType> resolveDeductionTypes(List<ValuationDeductionItemRequest> deductionItems) {
        if (deductionItems == null || deductionItems.isEmpty()) {
            return List.of();
        }
        List<AssetDeductionType> result = new ArrayList<>();
        for (ValuationDeductionItemRequest item : deductionItems) {
            AssetDeductionType deductionType = assetDeductionTypeRepository.findByCode(item.type())
                    .orElseThrow(() -> new BusinessException(
                            ErrorCode.RESOURCE_NOT_FOUND,
                            "Khong tim thay deduction type: " + item.type()
                    ));
            result.add(deductionType);
        }
        return result;
    }

    private AssetValuationPreviewResponse toResponse(
            String applicationCode,
            ValuationCalculation calculation,
            AssetValuationState state
    ) {
        return new AssetValuationPreviewResponse(
                applicationCode,
                calculation.marketValue(),
                calculation.totalDeductionRate(),
                calculation.totalDeductionAmount(),
                calculation.finalValue(),
                calculation.ltvRatio(),
                calculation.loanableValue(),
                state,
                calculation.deductionTypes().stream().map(AssetDeductionType::getCode).toList()
        );
    }

    private record ValuationCalculation(
            BigDecimal marketValue,
            BigDecimal totalDeductionRate,
            BigDecimal totalDeductionAmount,
            BigDecimal finalValue,
            BigDecimal ltvRatio,
            BigDecimal loanableValue,
            List<AssetDeductionType> deductionTypes
    ) {
    }
}
