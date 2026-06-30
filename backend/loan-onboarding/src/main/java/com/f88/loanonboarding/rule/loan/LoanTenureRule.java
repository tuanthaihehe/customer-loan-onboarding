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
            return RuleResult.fail(RuleCode.LOAN_TENURE_ALLOWED, "Kỳ hạn vay là bắt buộc");
        }

        if (!ALLOWED_TENURES.contains(context.requestedTenure())) {
            return RuleResult.fail(RuleCode.LOAN_TENURE_ALLOWED, "Kỳ hạn vay chỉ được chọn 3, 6, 9, 12, 18 hoặc 24 tháng");
        }

        return RuleResult.pass(RuleCode.LOAN_TENURE_ALLOWED);
    }
}
