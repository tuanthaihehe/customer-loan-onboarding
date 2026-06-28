package com.f88.loanonboarding.rule.valuation;

import java.math.BigDecimal;

import com.f88.loanonboarding.rule.BusinessRule;
import com.f88.loanonboarding.rule.RuleCode;
import com.f88.loanonboarding.rule.RuleContext;
import com.f88.loanonboarding.rule.RuleResult;

public class LoanableAmountRule implements BusinessRule {

    @Override
    public RuleResult evaluate(RuleContext context) {
        if (context.assetFinalValue() == null || context.loanableAmount() == null) {
            return RuleResult.fail(RuleCode.LOANABLE_AMOUNT_CHECK, "Asset final value and loanable amount are required");
        }

        if (context.assetFinalValue().compareTo(BigDecimal.ZERO) <= 0) {
            return RuleResult.fail(RuleCode.LOANABLE_AMOUNT_CHECK, "Asset final value must be greater than zero");
        }

        if (context.loanableAmount().compareTo(context.assetFinalValue()) > 0) {
            return RuleResult.fail(RuleCode.LOANABLE_AMOUNT_CHECK, "Loanable amount must not exceed asset final value");
        }

        return RuleResult.pass(RuleCode.LOANABLE_AMOUNT_CHECK);
    }
}
