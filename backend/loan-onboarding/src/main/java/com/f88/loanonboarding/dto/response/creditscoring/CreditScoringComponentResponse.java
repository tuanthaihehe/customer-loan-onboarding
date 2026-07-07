package com.f88.loanonboarding.dto.response.creditscoring;

import java.math.BigDecimal;

public record CreditScoringComponentResponse(
        String component,
        Object inputValue,
        BigDecimal scoreValue,
        BigDecimal weight,
        BigDecimal weightedScore,
        String displayLabel
) {
}
