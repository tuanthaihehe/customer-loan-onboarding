package com.f88.loanonboarding.dto.response.loan;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record LoanApplicationOnboardingValuationResponse(
        UUID valuationId,
        BigDecimal marketPriceAmount,
        BigDecimal totalDeductionAmount,
        BigDecimal finalValueAmount,
        String currencyCode,
        String valuationSource,
        LocalDateTime valuedAt,
        String valuedBy,
        String note
) {
}
