package com.f88.loanonboarding.rule.loanproduct;

import com.f88.loanonboarding.rule.BusinessRule;
import com.f88.loanonboarding.rule.RuleCode;
import com.f88.loanonboarding.rule.RuleContext;
import com.f88.loanonboarding.rule.RuleResult;

public class LoanProductTenorRule implements BusinessRule {

    @Override
    public RuleResult evaluate(RuleContext context) {
        if (context.requestedTenure() == null) {
            return RuleResult.fail(RuleCode.LOAN_PRODUCT_TENOR_ALLOWED, "Tenor is required");
        }
        if (context.allowedTenors().contains(context.requestedTenure())) {
            return RuleResult.pass(RuleCode.LOAN_PRODUCT_TENOR_ALLOWED);
        }
        return RuleResult.fail(
                RuleCode.LOAN_PRODUCT_TENOR_ALLOWED,
                "Sản phẩm không hỗ trợ kỳ hạn vay đã chọn."
        );
    }
}
