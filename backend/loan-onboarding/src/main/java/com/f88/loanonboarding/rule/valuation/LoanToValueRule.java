package com.f88.loanonboarding.rule.valuation;

import java.math.BigDecimal;

import com.f88.loanonboarding.rule.BusinessRule;
import com.f88.loanonboarding.rule.RuleCode;
import com.f88.loanonboarding.rule.RuleContext;
import com.f88.loanonboarding.rule.RuleResult;

public class LoanToValueRule implements BusinessRule {

    private static final BigDecimal MAX_LTV_RATIO = BigDecimal.valueOf(70);

    @Override
    public RuleResult evaluate(RuleContext context) {
        if (context.ltvRatio() == null) {
            return RuleResult.fail(RuleCode.LTV_LIMIT_CHECK, "LTV ratio is required");
        }

        if (context.ltvRatio().compareTo(MAX_LTV_RATIO) > 0) {
            return RuleResult.fail(RuleCode.LTV_LIMIT_CHECK, "LTV ratio must not exceed 70 percent");
        }

        return RuleResult.pass(RuleCode.LTV_LIMIT_CHECK);
    }
}
