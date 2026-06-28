package com.f88.loanonboarding.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.f88.loanonboarding.dto.request.asset.AssetLookupRequest;
import com.f88.loanonboarding.dto.request.asset.SaveAssetSnapshotRequest;
import com.f88.loanonboarding.dto.response.asset.AssetLookupResponse;
import com.f88.loanonboarding.dto.response.asset.AssetSnapshotResponse;
import com.f88.loanonboarding.mock.DemoAssetMockDataProvider;
import com.f88.loanonboarding.rule.RuleContext;
import com.f88.loanonboarding.rule.RuleEvaluationService;
import com.f88.loanonboarding.rule.asset.AssetDuplicateRule;
import com.f88.loanonboarding.rule.asset.AssetRequiredRule;
import com.f88.loanonboarding.service.AssetService;

@Service
public class AssetServiceMockImpl implements AssetService {

    private final RuleEvaluationService ruleEvaluationService;
    private final DemoAssetMockDataProvider mockDataProvider;

    public AssetServiceMockImpl(
            RuleEvaluationService ruleEvaluationService,
            DemoAssetMockDataProvider mockDataProvider
    ) {
        this.ruleEvaluationService = ruleEvaluationService;
        this.mockDataProvider = mockDataProvider;
    }

    @Override
    public AssetLookupResponse lookup(AssetLookupRequest request) {
        ruleEvaluationService.validateOrThrow(
                RuleContext.asset(request.assetType(), request.licensePlate(), false),
                List.of(new AssetRequiredRule(), new AssetDuplicateRule())
        );

        return mockDataProvider.eligibleAsset();
    }

    @Override
    public AssetSnapshotResponse saveSnapshot(String applicationCode, SaveAssetSnapshotRequest request) {
        ruleEvaluationService.validateOrThrow(
                RuleContext.asset(request.assetType(), request.licensePlate(), false),
                List.of(new AssetRequiredRule(), new AssetDuplicateRule())
        );

        return mockDataProvider.snapshot(applicationCode, request);
    }
}
