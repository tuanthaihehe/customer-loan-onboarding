package com.f88.loanonboarding.dto.response.asset;

import java.math.BigDecimal;
import java.util.List;

import com.f88.loanonboarding.enums.AssetValuationState;

public record AssetValuationPreviewResponse(
        String applicationCode,
        BigDecimal marketValue,
        BigDecimal totalDeductionRate,
        BigDecimal totalDeductionAmount,
        BigDecimal finalValue,
        BigDecimal ltvRatio,
        BigDecimal loanableValue,
        AssetValuationState valuationState,
        List<String> appliedDeductionTypes
) {
}
