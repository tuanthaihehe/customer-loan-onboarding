package com.f88.loanonboarding.dto.response.creditscoring;

import java.math.BigDecimal;
import java.util.List;

public record CreditScoringCalculateResponse(
        String ruleSetCode,
        BigDecimal totalScore,
        String scoreGrade,
        String scoreGradeLabel,
        List<CreditScoringComponentResponse> components
) {
}
