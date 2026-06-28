package com.f88.loanonboarding.rule.customer;

import com.f88.loanonboarding.rule.BusinessRule;
import com.f88.loanonboarding.rule.RuleCode;
import com.f88.loanonboarding.rule.RuleContext;
import com.f88.loanonboarding.rule.RuleResult;

public class CustomerBlacklistRule implements BusinessRule {

    @Override
    public RuleResult evaluate(RuleContext context) {
        if (context.blacklist()) {
            return RuleResult.fail(RuleCode.CUSTOMER_BLACKLIST_CHECK, "Customer is in blacklist");
        }

        return RuleResult.pass(RuleCode.CUSTOMER_BLACKLIST_CHECK);
    }
}
