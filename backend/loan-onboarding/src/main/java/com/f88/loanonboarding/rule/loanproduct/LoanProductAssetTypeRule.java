package com.f88.loanonboarding.rule.loanproduct;

import com.f88.loanonboarding.rule.BusinessRule;
import com.f88.loanonboarding.rule.RuleCode;
import com.f88.loanonboarding.rule.RuleContext;
import com.f88.loanonboarding.rule.RuleResult;

public class LoanProductAssetTypeRule implements BusinessRule {

    @Override
    public RuleResult evaluate(RuleContext context) {
        if (context.assetType() == null) {
            return RuleResult.fail(RuleCode.LOAN_PRODUCT_ASSET_TYPE_ALLOWED, "Asset type is required");
        }
        if (context.allowedAssetTypes().contains(context.assetType().code())) {
            return RuleResult.pass(RuleCode.LOAN_PRODUCT_ASSET_TYPE_ALLOWED);
        }
        return RuleResult.fail(
                RuleCode.LOAN_PRODUCT_ASSET_TYPE_ALLOWED,
                "Sản phẩm không áp dụng cho loại tài sản đã chọn."
        );
    }
}
