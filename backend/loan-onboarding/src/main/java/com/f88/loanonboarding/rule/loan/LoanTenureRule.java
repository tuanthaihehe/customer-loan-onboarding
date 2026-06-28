package com.f88.loanonboarding.rule.loan;

import com.f88.loanonboarding.rule.BusinessRule;
import com.f88.loanonboarding.rule.RuleCode;
import com.f88.loanonboarding.rule.RuleContext;
import com.f88.loanonboarding.rule.RuleResult;

public class LoanTenureRule implements BusinessRule {

    private static final int MIN_TENURE_MONTH = 1;
    private static final int MAX_TENURE_MONTH = 24;

    @Override
    public RuleResult evaluate(RuleContext context) {
        if (context.requestedTenure() == null) {
            return RuleResult.fail(RuleCode.LOAN_TENURE_ALLOWED, "Requested tenure is required");
        }

        if (context.requestedTenure() < MIN_TENURE_MONTH || context.requestedTenure() > MAX_TENURE_MONTH) {
            return RuleResult.fail(RuleCode.LOAN_TENURE_ALLOWED, "Requested tenure must be from 1 to 24 months");
        }

        return RuleResult.pass(RuleCode.LOAN_TENURE_ALLOWED);
    }
}
