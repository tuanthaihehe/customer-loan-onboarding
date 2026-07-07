package com.f88.loanonboarding.rule.valuation;

import java.math.BigDecimal;

import com.f88.loanonboarding.rule.BusinessRule;
import com.f88.loanonboarding.rule.RuleCode;
import com.f88.loanonboarding.rule.RuleContext;
import com.f88.loanonboarding.rule.RuleResult;

public class AssetValuationDeductionLimitRule implements BusinessRule {

    @Override
    public RuleResult evaluate(RuleContext context) {
        if (context.marketValue() == null || context.totalDeductionAmount() == null) {
            return RuleResult.fail(
                    RuleCode.ASSET_VALUATION_DEDUCTION_LIMIT,
                    "Market value and total deduction amount are required"
            );
        }
        if (context.totalDeductionAmount().compareTo(BigDecimal.ZERO) < 0) {
            return RuleResult.fail(
                    RuleCode.ASSET_VALUATION_DEDUCTION_LIMIT,
                    "Total deduction amount must not be negative"
            );
        }
        if (context.totalDeductionAmount().compareTo(context.marketValue()) > 0) {
            return RuleResult.fail(
                    RuleCode.ASSET_VALUATION_DEDUCTION_LIMIT,
                    "Tong giam tru khong duoc lon hon gia thi truong."
            );
        }
        return RuleResult.pass(RuleCode.ASSET_VALUATION_DEDUCTION_LIMIT);
    }
}
