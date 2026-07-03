package com.f88.loanonboarding.rule.customer;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.stereotype.Component;

import com.f88.loanonboarding.dmn.CustomerAgeDecisionResult;
import com.f88.loanonboarding.dmn.CustomerAgeDmnDecisionService;
import com.f88.loanonboarding.rule.BusinessRule;
import com.f88.loanonboarding.rule.RuleCode;
import com.f88.loanonboarding.rule.RuleContext;
import com.f88.loanonboarding.rule.RuleResult;

@Component
public class CustomerAgeRule implements BusinessRule {

    private final CustomerAgeDmnDecisionService customerAgeDmnDecisionService;

    public CustomerAgeRule(CustomerAgeDmnDecisionService customerAgeDmnDecisionService) {
        this.customerAgeDmnDecisionService = customerAgeDmnDecisionService;
    }

    @Override
    public RuleResult evaluate(RuleContext context) {
        if (context.dateOfBirth() == null) {
            return RuleResult.fail(RuleCode.CUSTOMER_AGE_CHECK, "Customer date of birth is required");
        }

        int age = Period.between(context.dateOfBirth(), LocalDate.now()).getYears();
        CustomerAgeDecisionResult decision = customerAgeDmnDecisionService.evaluate(age);
        if (!decision.eligible()) {
            return RuleResult.fail(RuleCode.CUSTOMER_AGE_CHECK, decision.reasonMessage());
        }

        return RuleResult.pass(RuleCode.CUSTOMER_AGE_CHECK);
    }
}
