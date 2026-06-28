package com.f88.loanonboarding.rule.loan;

import java.math.BigDecimal;

import com.f88.loanonboarding.rule.BusinessRule;
import com.f88.loanonboarding.rule.RuleCode;
import com.f88.loanonboarding.rule.RuleContext;
import com.f88.loanonboarding.rule.RuleResult;

public class RequestedAmountRule implements BusinessRule {

    private static final BigDecimal MIN_AMOUNT = BigDecimal.valueOf(1_000_000);
    private static final BigDecimal MAX_AMOUNT = BigDecimal.valueOf(100_000_000);

    @Override
    public RuleResult evaluate(RuleContext context) {
        if (context.requestedAmount() == null) {
            return RuleResult.fail(RuleCode.REQUESTED_AMOUNT_LIMIT, "Requested amount is required");
        }

        if (context.requestedAmount().compareTo(MIN_AMOUNT) < 0) {
            return RuleResult.fail(RuleCode.REQUESTED_AMOUNT_LIMIT, "Requested amount must be at least 1000000");
        }

        if (context.requestedAmount().compareTo(MAX_AMOUNT) > 0) {
            return RuleResult.fail(RuleCode.REQUESTED_AMOUNT_LIMIT, "Requested amount must not exceed 100000000");
        }

        return RuleResult.pass(RuleCode.REQUESTED_AMOUNT_LIMIT);
    }
}
