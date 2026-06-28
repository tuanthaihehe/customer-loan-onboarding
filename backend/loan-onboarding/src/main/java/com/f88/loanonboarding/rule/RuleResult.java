package com.f88.loanonboarding.rule;

public record RuleResult(
        boolean passed,
        String ruleCode,
        String message,
        RuleSeverity severity
) {

    public static RuleResult pass(RuleCode ruleCode) {
        return new RuleResult(
                true,
                ruleCode.name(),
                "Rule passed",
                RuleSeverity.INFO
        );
    }

    public static RuleResult fail(RuleCode ruleCode, String message) {
        return new RuleResult(
                false,
                ruleCode.name(),
                message,
                RuleSeverity.ERROR
        );
    }

    public static RuleResult warning(RuleCode ruleCode, String message) {
        return new RuleResult(
                false,
                ruleCode.name(),
                message,
                RuleSeverity.WARNING
        );
    }
}
