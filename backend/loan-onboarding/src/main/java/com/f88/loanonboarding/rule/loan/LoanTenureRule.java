package com.f88.loanonboarding.rule.loan;

import java.util.Set;

import com.f88.loanonboarding.rule.BusinessRule;
import com.f88.loanonboarding.rule.RuleCode;
import com.f88.loanonboarding.rule.RuleContext;
import com.f88.loanonboarding.rule.RuleResult;

public class LoanTenureRule implements BusinessRule {

    private static final Set<Integer> ALLOWED_TENURES = Set.of(3, 6, 9, 12, 18, 24);

    @Override
    public RuleResult evaluate(RuleContext context) {
        if (context.requestedTenure() == null) {
            return RuleResult.fail(RuleCode.LOAN_TENURE_ALLOWED, "Requested tenure is required");
        }

        if (!ALLOWED_TENURES.contains(context.requestedTenure())) {
            return RuleResult.fail(RuleCode.LOAN_TENURE_ALLOWED, "Requested tenure must be one of 3, 6, 9, 12, 18, 24 months");
        }

        return RuleResult.pass(RuleCode.LOAN_TENURE_ALLOWED);
    }
}
