package com.f88.loanonboarding.rule;

import java.util.List;

import org.springframework.stereotype.Service;

import com.f88.loanonboarding.common.error.ErrorCode;
import com.f88.loanonboarding.exception.BusinessException;

@Service
public class RuleEvaluationServiceImpl implements RuleEvaluationService {

    @Override
    public List<RuleResult> evaluate(RuleContext context, List<BusinessRule> rules) {
        return rules.stream()
                .map(rule -> rule.evaluate(context))
                .toList();
    }

    @Override
    public void validateOrThrow(RuleContext context, List<BusinessRule> rules) {
        evaluate(context, rules).stream()
                .filter(result -> !result.passed())
                .filter(result -> RuleSeverity.ERROR.equals(result.severity()))
                .findFirst()
                .ifPresent(result -> {
                    throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, result.message());
                });
    }
}
