package com.f88.loanonboarding.rule.loanproduct;

import com.f88.loanonboarding.rule.BusinessRule;
import com.f88.loanonboarding.rule.RuleCode;
import com.f88.loanonboarding.rule.RuleContext;
import com.f88.loanonboarding.rule.RuleResult;

public class LoanProductPurposeRule implements BusinessRule {

    @Override
    public RuleResult evaluate(RuleContext context) {
        if (context.loanPurpose() == null || context.loanPurpose().isBlank()) {
            return RuleResult.fail(RuleCode.LOAN_PRODUCT_PURPOSE_ALLOWED, "Loan purpose is required");
        }
        if (context.appliesToAllLoanPurposes() || context.allowedLoanPurposes().contains(context.loanPurpose())) {
            return RuleResult.pass(RuleCode.LOAN_PRODUCT_PURPOSE_ALLOWED);
        }
        return RuleResult.fail(
                RuleCode.LOAN_PRODUCT_PURPOSE_ALLOWED,
                "Sản phẩm không áp dụng cho mục đích vay đã chọn."
        );
    }
}
