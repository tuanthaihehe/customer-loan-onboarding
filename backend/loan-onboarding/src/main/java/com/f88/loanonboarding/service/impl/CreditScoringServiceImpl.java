package com.f88.loanonboarding.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.f88.loanonboarding.common.error.ErrorCode;
import com.f88.loanonboarding.dto.request.creditscoring.CreditScoringCalculateRequest;
import com.f88.loanonboarding.dto.response.creditscoring.CreditScoringCalculateResponse;
import com.f88.loanonboarding.dto.response.creditscoring.CreditScoringComponentResponse;
import com.f88.loanonboarding.entity.AgeScoreBand;
import com.f88.loanonboarding.entity.DependentScoreBand;
import com.f88.loanonboarding.entity.IncomeScoreBand;
import com.f88.loanonboarding.entity.OverallScoreGradeBand;
import com.f88.loanonboarding.exception.BusinessException;
import com.f88.loanonboarding.repository.AgeScoreBandRepository;
import com.f88.loanonboarding.repository.DependentScoreBandRepository;
import com.f88.loanonboarding.repository.IncomeScoreBandRepository;
import com.f88.loanonboarding.repository.OverallScoreGradeBandRepository;
import com.f88.loanonboarding.service.CreditScoringService;

@Service
public class CreditScoringServiceImpl implements CreditScoringService {

    private static final String DEFAULT_RULE_SET_CODE = "BASIC_SCORING_V1";
    private static final int SCORE_SCALE = 2;

    private final IncomeScoreBandRepository incomeScoreBandRepository;
    private final AgeScoreBandRepository ageScoreBandRepository;
    private final DependentScoreBandRepository dependentScoreBandRepository;
    private final OverallScoreGradeBandRepository overallScoreGradeBandRepository;

    public CreditScoringServiceImpl(
            IncomeScoreBandRepository incomeScoreBandRepository,
            AgeScoreBandRepository ageScoreBandRepository,
            DependentScoreBandRepository dependentScoreBandRepository,
            OverallScoreGradeBandRepository overallScoreGradeBandRepository
    ) {
        this.incomeScoreBandRepository = incomeScoreBandRepository;
        this.ageScoreBandRepository = ageScoreBandRepository;
        this.dependentScoreBandRepository = dependentScoreBandRepository;
        this.overallScoreGradeBandRepository = overallScoreGradeBandRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public CreditScoringCalculateResponse calculate(CreditScoringCalculateRequest request) {
        String ruleSetCode = normalizeRuleSetCode(request.ruleSetCode());
        LocalDate asOfDate = LocalDate.now();

        IncomeScoreBand incomeBand = incomeScoreBandRepository
                .findActiveBand(ruleSetCode, request.monthlyIncomeAmount(), asOfDate)
                .orElseThrow(() -> missingBand("income_score_band", ruleSetCode));
        AgeScoreBand ageBand = ageScoreBandRepository
                .findActiveBand(ruleSetCode, request.age(), asOfDate)
                .orElseThrow(() -> missingBand("age_score_band", ruleSetCode));
        DependentScoreBand dependentBand = dependentScoreBandRepository
                .findActiveBand(ruleSetCode, request.dependentCount(), asOfDate)
                .orElseThrow(() -> missingBand("dependent_score_band", ruleSetCode));

        CreditScoringComponentResponse incomeComponent = component(
                "INCOME",
                request.monthlyIncomeAmount(),
                incomeBand.getScoreValue(),
                incomeBand.getWeight(),
                incomeBand.getDisplayLabel()
        );
        CreditScoringComponentResponse ageComponent = component(
                "AGE",
                request.age(),
                ageBand.getScoreValue(),
                ageBand.getWeight(),
                ageBand.getDisplayLabel()
        );
        CreditScoringComponentResponse dependentComponent = component(
                "DEPENDENT",
                request.dependentCount(),
                dependentBand.getScoreValue(),
                dependentBand.getWeight(),
                dependentBand.getDisplayLabel()
        );

        BigDecimal totalScore = incomeComponent.weightedScore()
                .add(ageComponent.weightedScore())
                .add(dependentComponent.weightedScore())
                .setScale(SCORE_SCALE, RoundingMode.HALF_UP);

        OverallScoreGradeBand gradeBand = overallScoreGradeBandRepository
                .findActiveBand(ruleSetCode, totalScore, asOfDate)
                .orElseThrow(() -> missingBand("overall_score_grade_band", ruleSetCode));

        return new CreditScoringCalculateResponse(
                ruleSetCode,
                totalScore,
                gradeBand.getGradeCode(),
                gradeBand.getDisplayLabel(),
                List.of(incomeComponent, ageComponent, dependentComponent)
        );
    }

    private String normalizeRuleSetCode(String ruleSetCode) {
        if (ruleSetCode == null || ruleSetCode.isBlank()) {
            return DEFAULT_RULE_SET_CODE;
        }
        return ruleSetCode.trim();
    }

    private CreditScoringComponentResponse component(
            String component,
            Object inputValue,
            BigDecimal scoreValue,
            BigDecimal weight,
            String displayLabel
    ) {
        BigDecimal weightedScore = scoreValue.multiply(weight).setScale(SCORE_SCALE, RoundingMode.HALF_UP);
        return new CreditScoringComponentResponse(
                component,
                inputValue,
                scoreValue.setScale(SCORE_SCALE, RoundingMode.HALF_UP),
                weight,
                weightedScore,
                displayLabel
        );
    }

    private BusinessException missingBand(String tableName, String ruleSetCode) {
        return new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "No active " + tableName + " configured for ruleSetCode: " + ruleSetCode
        );
    }
}
