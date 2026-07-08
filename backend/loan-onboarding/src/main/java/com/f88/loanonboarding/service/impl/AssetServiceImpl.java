package com.f88.loanonboarding.service.impl;

import java.time.Year;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.f88.loanonboarding.common.error.ErrorCode;
import com.f88.loanonboarding.dto.request.asset.AssetLookupRequest;
import com.f88.loanonboarding.dto.request.asset.SaveAssetLegalInfoRequest;
import com.f88.loanonboarding.dto.request.asset.SaveAssetSnapshotRequest;
import com.f88.loanonboarding.dto.request.asset.SaveVehicleRegistrationRequest;
import com.f88.loanonboarding.dto.response.asset.AssetLookupResponse;
import com.f88.loanonboarding.dto.response.asset.AssetLegalInfoResponse;
import com.f88.loanonboarding.dto.response.asset.AssetSnapshotResponse;
import com.f88.loanonboarding.entity.Asset;
import com.f88.loanonboarding.entity.LoanApplication;
import com.f88.loanonboarding.entity.VehicleVariant;
import com.f88.loanonboarding.enums.AssetStatus;
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

    private static final List<String> NON_EDITABLE_STATES = List.of("APP_SUBMITTED", "APP_CANCELLED", "APP_EXPIRED", "APP_CLOSED");

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
                        asset.getStatus().isEligibleForPledge(),
                        asset.getStatus().getBlockReasonCode(),
                        asset.getStatus().getBlockReasonMessage()
                ))
                .orElseGet(() -> new AssetLookupResponse(false, null, null, true, null, null));
    }

    @Override
    @Transactional
    public AssetSnapshotResponse saveSnapshot(String applicationCode, SaveAssetSnapshotRequest request) {
        LoanApplication application = loanApplicationRepository.findByLoanApplicationCode(applicationCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOAN_APPLICATION_NOT_FOUND));
        ensureEditableApplication(application);
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

    @Override
    @Transactional
    public AssetLegalInfoResponse saveLegalInfo(String applicationCode, SaveAssetLegalInfoRequest request) {
        LoanApplication application = findDraftApplication(applicationCode);
        Asset asset = requireAsset(application);
        String frameNumber = normalizeIdentifier(request.frameNumber());
        String engineNumber = normalizeIdentifier(request.engineNumber());
        ensureUniqueIdentifier(asset, assetRepository.findByFrameNumber(frameNumber), "Số khung đã tồn tại trong database");
        ensureUniqueIdentifier(asset, assetRepository.findByEngineNumber(engineNumber), "Số máy đã tồn tại trong database");
        asset.setFrameNumber(frameNumber);
        asset.setEngineNumber(engineNumber);
        Asset saved = assetRepository.save(asset);
        return toLegalInfoResponse(applicationCode, saved);
    }

    @Override
    @Transactional
    public AssetLegalInfoResponse saveVehicleRegistration(String applicationCode, SaveVehicleRegistrationRequest request) {
        LoanApplication application = findDraftApplication(applicationCode);
        Asset asset = requireAsset(application);
        String registrationNumber = normalizeIdentifier(request.registrationNumber());
        ensureUniqueIdentifier(asset, assetRepository.findByRegistrationNumber(registrationNumber), "Số đăng ký xe đã tồn tại trong database");
        asset.setRegistrationNumber(registrationNumber);
        asset.setRegistrationIssueDate(request.registrationIssueDate());
        Asset saved = assetRepository.save(asset);
        return toLegalInfoResponse(applicationCode, saved);
    }

    private LoanApplication findDraftApplication(String applicationCode) {
        LoanApplication application = loanApplicationRepository.findByLoanApplicationCode(applicationCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOAN_APPLICATION_NOT_FOUND));
        ensureEditableApplication(application);
        return application;
    }

    private Asset requireAsset(LoanApplication application) {
        if (application.getAsset() == null) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Hồ sơ chưa có thông tin tài sản. Hãy lưu thông tin sơ bộ tài sản trước."
            );
        }
        return application.getAsset();
    }

    private void ensureUniqueIdentifier(Asset currentAsset, java.util.Optional<Asset> existingAsset, String message) {
        if (existingAsset.isPresent() && !existingAsset.get().getId().equals(currentAsset.getId())) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, message);
        }
    }

    private void ensureEditableApplication(LoanApplication application) {
        if (application.getCurrentState() == null || NON_EDITABLE_STATES.contains(application.getCurrentState().getCode())) {
            throw new BusinessException(
                    ErrorCode.INVALID_LOAN_APPLICATION_STATE,
                    "Không được lưu thông tin tài sản khi hồ sơ đã nộp, đã hủy hoặc đã hết hạn."
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
        if (!sameAssetOnCurrentApplication && !asset.getStatus().isEligibleForPledge()) {
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
        asset.setStatus(AssetStatus.AVAILABLE);
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

    private AssetLegalInfoResponse toLegalInfoResponse(String applicationCode, Asset asset) {
        VehicleVariant variant = asset.getVehicleVariant();
        var vehicleYear = variant.getVehicleYear();
        var vehicleVersion = vehicleYear.getVehicleVersion();
        var vehicleModel = vehicleVersion.getVehicleModel();
        var vehicleBrand = vehicleModel.getVehicleBrand();
        var vehicleType = vehicleBrand.getVehicleType();
        var vehicleColor = variant.getVehicleColor();

        return new AssetLegalInfoResponse(
                applicationCode,
                asset.getAssetCode(),
                AssetType.fromCode(vehicleType.getCode()),
                asset.getLicensePlate(),
                vehicleBrand.getCode(),
                vehicleModel.getCode(),
                variant.getCode(),
                vehicleYear.getManufactureYear(),
                vehicleColor.getCode(),
                asset.getFrameNumber(),
                asset.getEngineNumber(),
                asset.getRegistrationNumber(),
                asset.getRegistrationIssueDate()
        );
    }

    private String normalizeLicensePlate(String licensePlate) {
        return licensePlate == null ? null : licensePlate.trim().toUpperCase();
    }

    private String normalizeIdentifier(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }

    private boolean sameCode(String input, String code) {
        return normalizeCode(input).equals(normalizeCode(code));
    }

    private String normalizeCode(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }
}
