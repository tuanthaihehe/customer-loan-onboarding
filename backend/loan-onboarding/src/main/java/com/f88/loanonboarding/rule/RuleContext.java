package com.f88.loanonboarding.rule;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RuleContext(
        String customerCode,
        LocalDate dateOfBirth,
        boolean blacklist,
        BigDecimal requestedAmount,
        Integer requestedTenure,
        String loanPurpose,
        String assetType,
        String licensePlate,
        boolean duplicatedAsset,
        BigDecimal assetFinalValue,
        BigDecimal loanableAmount,
        BigDecimal ltvRatio
) {

    public static RuleContext customer(String customerCode, LocalDate dateOfBirth, boolean blacklist) {
        return new RuleContext(
                customerCode,
                dateOfBirth,
                blacklist,
                null,
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null
        );
    }

    public static RuleContext loan(BigDecimal requestedAmount, Integer requestedTenure, String loanPurpose) {
        return new RuleContext(
                null,
                null,
                false,
                requestedAmount,
                requestedTenure,
                loanPurpose,
                null,
                null,
                false,
                null,
                null,
                null
        );
    }

    public static RuleContext asset(String assetType, String licensePlate, boolean duplicatedAsset) {
        return new RuleContext(
                null,
                null,
                false,
                null,
                null,
                null,
                assetType,
                licensePlate,
                duplicatedAsset,
                null,
                null,
                null
        );
    }

    public static RuleContext valuation(BigDecimal assetFinalValue, BigDecimal loanableAmount, BigDecimal ltvRatio) {
        return new RuleContext(
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                null,
                false,
                assetFinalValue,
                loanableAmount,
                ltvRatio
        );
    }
}
