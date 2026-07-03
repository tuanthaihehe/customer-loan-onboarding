package com.f88.loanonboarding.dto.response.loan;

import java.math.BigDecimal;

public record AppliedDeductionResponse(
        String code,
        String name,
        BigDecimal deductionAmount,
        BigDecimal deductionPercent
) {
}
