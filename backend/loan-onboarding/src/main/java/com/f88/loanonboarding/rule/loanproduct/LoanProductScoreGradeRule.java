package com.f88.loanonboarding.rule.loanproduct;

import com.f88.loanonboarding.rule.BusinessRule;
import com.f88.loanonboarding.rule.RuleCode;
import com.f88.loanonboarding.rule.RuleContext;
import com.f88.loanonboarding.rule.RuleResult;

public class LoanProductScoreGradeRule implements BusinessRule {

    @Override
    public RuleResult evaluate(RuleContext context) {
        if (context.scoreGrade() == null || context.scoreGrade().isBlank()) {
            return RuleResult.pass(RuleCode.LOAN_PRODUCT_SCORE_ALLOWED);
        }
        if (context.allowedScoreGrades().contains(context.scoreGrade())) {
            return RuleResult.pass(RuleCode.LOAN_PRODUCT_SCORE_ALLOWED);
        }
        return RuleResult.fail(
                RuleCode.LOAN_PRODUCT_SCORE_ALLOWED,
                "Sản phẩm không áp dụng cho hạng điểm đã chọn."
        );
    }
}
