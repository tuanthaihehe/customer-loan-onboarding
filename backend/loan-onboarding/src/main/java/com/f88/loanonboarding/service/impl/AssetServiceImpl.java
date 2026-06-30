package com.f88.loanonboarding.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.f88.loanonboarding.common.error.ErrorCode;
import com.f88.loanonboarding.dto.request.asset.AssetLookupRequest;
import com.f88.loanonboarding.dto.request.asset.SaveAssetSnapshotRequest;
import com.f88.loanonboarding.dto.response.asset.AssetLookupResponse;
import com.f88.loanonboarding.dto.response.asset.AssetSnapshotResponse;
import com.f88.loanonboarding.exception.BusinessException;
import com.f88.loanonboarding.repository.LoanApplicationRepository;
import com.f88.loanonboarding.rule.RuleContext;
import com.f88.loanonboarding.rule.RuleEvaluationService;
import com.f88.loanonboarding.rule.asset.AssetDuplicateRule;
import com.f88.loanonboarding.rule.asset.AssetRequiredRule;
import com.f88.loanonboarding.service.AssetService;

@Service
public class AssetServiceImpl implements AssetService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final RuleEvaluationService ruleEvaluationService;

    public AssetServiceImpl(
            LoanApplicationRepository loanApplicationRepository,
            RuleEvaluationService ruleEvaluationService
    ) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.ruleEvaluationService = ruleEvaluationService;
    }

    @Override
    public AssetLookupResponse lookup(AssetLookupRequest request) {
        ruleEvaluationService.validateOrThrow(
                RuleContext.asset(request.assetType(), request.licensePlate(), false),
                List.of(new AssetRequiredRule(), new AssetDuplicateRule())
        );

        return new AssetLookupResponse(false, null, null, false, "ASSET_TABLE_NOT_AVAILABLE");
    }

    @Override
    public AssetSnapshotResponse saveSnapshot(String applicationCode, SaveAssetSnapshotRequest request) {
        ensureApplicationExists(applicationCode);
        throw new BusinessException(
                ErrorCode.BUSINESS_RULE_VIOLATION,
                "Chua co bang asset snapshot trong database that, khong luu du lieu gia."
        );
    }

    private void ensureApplicationExists(String applicationCode) {
        if (loanApplicationRepository.findByLoanApplicationCode(applicationCode).isEmpty()) {
            throw new BusinessException(ErrorCode.LOAN_APPLICATION_NOT_FOUND);
        }
    }
}
