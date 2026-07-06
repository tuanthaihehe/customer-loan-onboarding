package com.f88.loanonboarding.rule.loanproduct;

import com.f88.loanonboarding.rule.BusinessRule;
import com.f88.loanonboarding.rule.RuleCode;
import com.f88.loanonboarding.rule.RuleContext;
import com.f88.loanonboarding.rule.RuleResult;

public class LoanProductMinimumAmountRule implements BusinessRule {

    @Override
    public RuleResult evaluate(RuleContext context) {
        if (context.minLoanAmount() == null || context.effectiveMaxLoanAmount() == null) {
            return RuleResult.fail(
                    RuleCode.LOAN_PRODUCT_MIN_AMOUNT_ALLOWED,
                    "Product min amount and effective max loan amount are required"
            );
        }
        if (context.effectiveMaxLoanAmount().compareTo(context.minLoanAmount()) >= 0) {
            return RuleResult.pass(RuleCode.LOAN_PRODUCT_MIN_AMOUNT_ALLOWED);
        }
        return RuleResult.fail(
                RuleCode.LOAN_PRODUCT_MIN_AMOUNT_ALLOWED,
                "Sản phẩm không đạt số tiền vay tối thiểu với giá trị tài sản hiện tại."
        );
    }
}
