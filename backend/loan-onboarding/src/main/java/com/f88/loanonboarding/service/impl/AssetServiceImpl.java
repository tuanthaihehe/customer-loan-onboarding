package com.f88.loanonboarding.service.impl;

import java.time.Year;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.f88.loanonboarding.common.error.ErrorCode;
import com.f88.loanonboarding.dto.request.asset.AssetLookupRequest;
import com.f88.loanonboarding.dto.request.asset.SaveAssetSnapshotRequest;
import com.f88.loanonboarding.dto.response.asset.AssetLookupResponse;
import com.f88.loanonboarding.dto.response.asset.AssetSnapshotResponse;
import com.f88.loanonboarding.entity.Asset;
import com.f88.loanonboarding.entity.LoanApplication;
import com.f88.loanonboarding.entity.VehicleVariant;
import com.f88.loanonboarding.enums.AssetType;
import com.f88.loanonboarding.exception.BusinessException;
import com.f88.loanonboarding.repository.AssetRepository;
import com.f88.loanonboarding.repository.LoanApplicationRepository;
import com.f88.loanonboarding.repository.VehicleVariantRepository;
import com.f88.loanonboarding.rule.RuleContext;
import com.f88.loanonboarding.rule.RuleEvaluationService;
import com.f88.loanonboarding.rule.asset.AssetRequiredRule;
import com.f88.loanonboarding.service.AssetService;

@Service
public class AssetServiceImpl implements AssetService {

    private static final String AVAILABLE = "AVAILABLE";
    private static final String DRAFT_STATE = "APP_DRAFT";

    private final LoanApplicationRepository loanApplicationRepository;
    private final AssetRepository assetRepository;
    private final VehicleVariantRepository vehicleVariantRepository;
    private final RuleEvaluationService ruleEvaluationService;

    public AssetServiceImpl(
            LoanApplicationRepository loanApplicationRepository,
            AssetRepository assetRepository,
            VehicleVariantRepository vehicleVariantRepository,
            RuleEvaluationService ruleEvaluationService
    ) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.assetRepository = assetRepository;
        this.vehicleVariantRepository = vehicleVariantRepository;
        this.ruleEvaluationService = ruleEvaluationService;
    }

    @Override
    @Transactional(readOnly = true)
    public AssetLookupResponse lookup(AssetLookupRequest request) {
        ruleEvaluationService.validateOrThrow(
                RuleContext.asset(request.assetType(), request.licensePlate(), false),
                List.of(new AssetRequiredRule())
        );

        return assetRepository.findByLicensePlate(normalizeLicensePlate(request.licensePlate()))
                .map(asset -> new AssetLookupResponse(
                        true,
                        asset.getAssetCode(),
                        asset.getStatus(),
                        AVAILABLE.equals(asset.getStatus()),
                        AVAILABLE.equals(asset.getStatus()) ? null : "ASSET_NOT_AVAILABLE"
                ))
                .orElseGet(() -> new AssetLookupResponse(false, null, null, true, null));
    }

    @Override
    @Transactional
    public AssetSnapshotResponse saveSnapshot(String applicationCode, SaveAssetSnapshotRequest request) {
        LoanApplication application = loanApplicationRepository.findByLoanApplicationCode(applicationCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOAN_APPLICATION_NOT_FOUND));
        ensureDraftApplication(application);
        VehicleVariant variant = resolveVariant(request);
        validateCatalogSelection(request, variant);
        String licensePlate = normalizeLicensePlate(request.licensePlate());

        Asset asset = assetRepository.findByLicensePlate(licensePlate)
                .orElseGet(() -> createAsset(licensePlate, variant));
        ensureAssetCanBeAttached(application, asset);

        asset.setVehicleVariant(variant);
        asset = assetRepository.save(asset);
        application.setAsset(asset);
        loanApplicationRepository.save(application);

        return toSnapshotResponse(applicationCode, asset);
    }

    private void ensureDraftApplication(LoanApplication application) {
        if (application.getCurrentState() == null || !DRAFT_STATE.equals(application.getCurrentState().getCode())) {
            throw new BusinessException(
                    ErrorCode.INVALID_LOAN_APPLICATION_STATE,
                    "Chỉ được lưu thông tin tài sản khi hồ sơ vay đang ở trạng thái nháp."
            );
        }
    }

    private VehicleVariant resolveVariant(SaveAssetSnapshotRequest request) {
        if (request.vehicleVariant() == null || request.vehicleVariant().isBlank()) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Phiên bản xe là bắt buộc để gắn đúng biến thể xe trong catalog."
            );
        }
        return vehicleVariantRepository.findByCode(request.vehicleVariant())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "Không tìm thấy phiên bản xe trong database: " + request.vehicleVariant()
                ));
    }

    private void validateCatalogSelection(SaveAssetSnapshotRequest request, VehicleVariant variant) {
        var vehicleYear = variant.getVehicleYear();
        var vehicleVersion = vehicleYear.getVehicleVersion();
        var vehicleModel = vehicleVersion.getVehicleModel();
        var vehicleBrand = vehicleModel.getVehicleBrand();
        var vehicleType = vehicleBrand.getVehicleType();
        var vehicleColor = variant.getVehicleColor();

        if (!request.assetType().code().equals(vehicleType.getCode())) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Loại tài sản không khớp với phiên bản xe trong catalog."
            );
        }
        if (!sameCode(request.brand(), vehicleBrand.getCode())) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Hãng xe không khớp với phiên bản xe trong catalog."
            );
        }
        if (!sameCode(request.model(), vehicleModel.getCode())) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Dòng xe không khớp với phiên bản xe trong catalog."
            );
        }
        if (!request.manufactureYear().equals(vehicleYear.getManufactureYear())) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Năm sản xuất không khớp với phiên bản xe trong catalog."
            );
        }
        if (!sameCode(request.vehicleColor(), vehicleColor.getCode())) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Màu xe không khớp với phiên bản xe trong catalog."
            );
        }
    }

    private void ensureAssetCanBeAttached(LoanApplication application, Asset asset) {
        boolean sameAssetOnCurrentApplication = application.getAsset() != null
                && application.getAsset().getId().equals(asset.getId());
        if (!sameAssetOnCurrentApplication && !AVAILABLE.equals(asset.getStatus())) {
            throw new BusinessException(
                    ErrorCode.ASSET_ALREADY_PLEDGED,
                    "Tài sản hiện tại đang bị cầm cố hoặc không có sẵn."
            );
        }
        boolean usedByAnotherOpenApplication = loanApplicationRepository
                .existsByAssetAndCurrentState_TerminalFalseAndLoanApplicationCodeNot(
                        asset,
                        application.getLoanApplicationCode()
                );
        if (usedByAnotherOpenApplication) {
            throw new BusinessException(
                    ErrorCode.ASSET_ALREADY_PLEDGED,
                    "Tài sản đã được gắn với một hồ sơ vay khác chưa kết thúc."
            );
        }
    }

    private Asset createAsset(String licensePlate, VehicleVariant variant) {
        Asset asset = new Asset();
        asset.setAssetCode(nextAssetCode());
        asset.setVehicleVariant(variant);
        asset.setLicensePlate(licensePlate);
        asset.setStatus(AVAILABLE);
        return asset;
    }

    private String nextAssetCode() {
        String prefix = "AST-" + Year.now().getValue() + "-";
        long sequence = assetRepository.countByAssetCodeStartingWith(prefix) + 1;
        return prefix + String.format("%06d", sequence);
    }

    private AssetSnapshotResponse toSnapshotResponse(String applicationCode, Asset asset) {
        VehicleVariant variant = asset.getVehicleVariant();
        var vehicleYear = variant.getVehicleYear();
        var vehicleVersion = vehicleYear.getVehicleVersion();
        var vehicleModel = vehicleVersion.getVehicleModel();
        var vehicleBrand = vehicleModel.getVehicleBrand();
        var vehicleType = vehicleBrand.getVehicleType();
        var vehicleColor = variant.getVehicleColor();

        return new AssetSnapshotResponse(
                applicationCode,
                AssetType.fromCode(vehicleType.getCode()),
                asset.getLicensePlate(),
                vehicleBrand.getCode(),
                vehicleModel.getCode(),
                variant.getCode(),
                vehicleYear.getManufactureYear(),
                vehicleColor.getCode()
        );
    }

    private String normalizeLicensePlate(String licensePlate) {
        return licensePlate == null ? null : licensePlate.trim().toUpperCase();
    }

    private boolean sameCode(String input, String code) {
        return normalizeCode(input).equals(normalizeCode(code));
    }

    private String normalizeCode(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }
}
