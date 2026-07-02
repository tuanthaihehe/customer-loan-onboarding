package com.f88.loanonboarding.dto.response.loan;

import java.math.BigDecimal;

public record LoanScoringResponse(
        String scoreGrade,
        Integer overallScore,
        Integer aScore,
        Integer bScore,
        Integer aScoreWeight,
        Integer bScoreWeight,
        BigDecimal ltvPercent,
        String matchedRuleCode,
        String matchedRuleName
) {
}
