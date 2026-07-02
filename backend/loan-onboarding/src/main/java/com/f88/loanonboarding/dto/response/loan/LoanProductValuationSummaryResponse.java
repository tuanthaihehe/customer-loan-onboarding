package com.f88.loanonboarding.dto.response.loan;

import java.math.BigDecimal;
import java.util.List;

public record LoanProductValuationSummaryResponse(
        BigDecimal marketValue,
        BigDecimal totalDeductionAmount,
        BigDecimal finalValue,
        List<String> appliedDeductionTypes
) {
}
