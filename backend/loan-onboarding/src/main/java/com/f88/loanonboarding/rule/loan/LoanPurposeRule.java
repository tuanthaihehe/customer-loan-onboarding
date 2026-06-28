package com.f88.loanonboarding.rule.loan;

import java.util.Set;

import com.f88.loanonboarding.rule.BusinessRule;
import com.f88.loanonboarding.rule.RuleCode;
import com.f88.loanonboarding.rule.RuleContext;
import com.f88.loanonboarding.rule.RuleResult;

public class LoanPurposeRule implements BusinessRule {

    private static final Set<String> ALLOWED_PURPOSES = Set.of("BUSINESS", "PERSONAL", "EMERGENCY");

    @Override
    public RuleResult evaluate(RuleContext context) {
        if (context.loanPurpose() == null || context.loanPurpose().isBlank()) {
            return RuleResult.fail(RuleCode.LOAN_PURPOSE_ALLOWED, "Loan purpose is required");
        }

        if (!ALLOWED_PURPOSES.contains(context.loanPurpose())) {
            return RuleResult.fail(RuleCode.LOAN_PURPOSE_ALLOWED, "Loan purpose is not allowed");
        }

        return RuleResult.pass(RuleCode.LOAN_PURPOSE_ALLOWED);
    }
}
