package com.f88.loanonboarding.rule.asset;

import com.f88.loanonboarding.rule.BusinessRule;
import com.f88.loanonboarding.rule.RuleCode;
import com.f88.loanonboarding.rule.RuleContext;
import com.f88.loanonboarding.rule.RuleResult;

public class AssetDuplicateRule implements BusinessRule {

    @Override
    public RuleResult evaluate(RuleContext context) {
        if (context.duplicatedAsset()) {
            return RuleResult.fail(
                    RuleCode.ASSET_DUPLICATE_CHECK,
                    "Asset already exists or is already linked to another active loan application"
            );
        }

        return RuleResult.pass(RuleCode.ASSET_DUPLICATE_CHECK);
    }
}
