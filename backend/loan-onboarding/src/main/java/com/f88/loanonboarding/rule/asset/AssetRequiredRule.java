package com.f88.loanonboarding.rule.asset;

import com.f88.loanonboarding.rule.BusinessRule;
import com.f88.loanonboarding.rule.RuleCode;
import com.f88.loanonboarding.rule.RuleContext;
import com.f88.loanonboarding.rule.RuleResult;

public class AssetRequiredRule implements BusinessRule {

    @Override
    public RuleResult evaluate(RuleContext context) {
        if (context.assetType() == null || isBlank(context.licensePlate())) {
            return RuleResult.fail(
                    RuleCode.ASSET_REQUIRED,
                    "Asset type and license plate are required before linking asset to loan application"
            );
        }

        return RuleResult.pass(RuleCode.ASSET_REQUIRED);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
