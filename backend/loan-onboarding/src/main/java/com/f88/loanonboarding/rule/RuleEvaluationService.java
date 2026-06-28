package com.f88.loanonboarding.rule;

import java.util.List;

public interface RuleEvaluationService {

    List<RuleResult> evaluate(RuleContext context, List<BusinessRule> rules);

    void validateOrThrow(RuleContext context, List<BusinessRule> rules);
}
