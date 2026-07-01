package com.f88.loanonboarding.rule.customer;

import java.time.LocalDate;
import java.time.Period;

import com.f88.loanonboarding.rule.BusinessRule;
import com.f88.loanonboarding.rule.RuleCode;
import com.f88.loanonboarding.rule.RuleContext;
import com.f88.loanonboarding.rule.RuleResult;

public class CustomerAgeRule implements BusinessRule {

    private static final int MIN_AGE = 18;

    @Override
    public RuleResult evaluate(RuleContext context) {
        if (context.dateOfBirth() == null) {
            return RuleResult.fail(RuleCode.CUSTOMER_AGE_CHECK, "Ngày sinh khách hàng là bắt buộc");
        }

        int age = Period.between(context.dateOfBirth(), LocalDate.now()).getYears();
        if (age < MIN_AGE) {
            return RuleResult.fail(RuleCode.CUSTOMER_AGE_CHECK, "Khách hàng phải đủ 18 tuổi trở lên");
        }

        return RuleResult.pass(RuleCode.CUSTOMER_AGE_CHECK);
    }
}
