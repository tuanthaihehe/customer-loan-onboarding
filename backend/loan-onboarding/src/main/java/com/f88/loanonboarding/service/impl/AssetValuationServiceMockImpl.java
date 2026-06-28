package com.f88.loanonboarding.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;

import com.f88.loanonboarding.dto.request.asset.AssetValuationPreviewRequest;
import com.f88.loanonboarding.dto.request.asset.ValuationDeductionItemRequest;
import com.f88.loanonboarding.dto.response.asset.AssetValuationPreviewResponse;
import com.f88.loanonboarding.enums.AssetValuationState;
import com.f88.loanonboarding.mock.DemoValuationMockDataProvider;
import com.f88.loanonboarding.rule.RuleContext;
import com.f88.loanonboarding.rule.RuleEvaluationService;
import com.f88.loanonboarding.rule.valuation.LoanToValueRule;
import com.f88.loanonboarding.rule.valuation.LoanableAmountRule;
import com.f88.loanonboarding.service.AssetValuationService;

@Service
public class AssetValuationServiceMockImpl implements AssetValuationService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final RuleEvaluationService ruleEvaluationService;
    private final DemoValuationMockDataProvider mockDataProvider;

    public AssetValuationServiceMockImpl(
            RuleEvaluationService ruleEvaluationService,
            DemoValuationMockDataProvider mockDataProvider
    ) {
        this.ruleEvaluationService = ruleEvaluationService;
        this.mockDataProvider = mockDataProvider;
    }

    @Override
    public AssetValuationPreviewResponse preview(String applicationCode, AssetValuationPreviewRequest request) {
        return calculate(applicationCode, request, AssetValuationState.VAL_RECORDED);
    }

    @Override
    public AssetValuationPreviewResponse savePreview(String applicationCode, AssetValuationPreviewRequest request) {
        return calculate(applicationCode, request, AssetValuationState.VAL_ACTIVE);
    }

    private AssetValuationPreviewResponse calculate(
            String applicationCode,
            AssetValuationPreviewRequest request,
            AssetValuationState state
    ) {
        BigDecimal marketValue = mockDataProvider.defaultMarketValue();
        BigDecimal ltvRatio = mockDataProvider.defaultLtvRatio();

        List<ValuationDeductionItemRequest> deductionItems = request.deductionItems() == null
                ? List.of()
                : request.deductionItems();

        BigDecimal totalDeductionRate = deductionItems.stream()
                .map(ValuationDeductionItemRequest::rate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDeductionAmount = marketValue
                .multiply(totalDeductionRate)
                .divide(ONE_HUNDRED, 0, RoundingMode.HALF_UP);
        BigDecimal finalValue = marketValue.subtract(totalDeductionAmount);
        BigDecimal loanableValue = finalValue
                .multiply(ltvRatio)
                .divide(ONE_HUNDRED, 0, RoundingMode.HALF_UP);
        List<String> appliedTypes = deductionItems.stream()
                .map(ValuationDeductionItemRequest::type)
                .toList();

        ruleEvaluationService.validateOrThrow(
                RuleContext.valuation(finalValue, loanableValue, ltvRatio),
                List.of(new LoanToValueRule(), new LoanableAmountRule())
        );

        return new AssetValuationPreviewResponse(
                applicationCode,
                marketValue,
                totalDeductionRate,
                totalDeductionAmount,
                finalValue,
                ltvRatio,
                loanableValue,
                state,
                appliedTypes
        );
    }
}
