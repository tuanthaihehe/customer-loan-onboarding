package com.f88.loanonboarding.rule;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import com.f88.loanonboarding.enums.AssetType;

public record RuleContext(
        String customerCode,
        LocalDate dateOfBirth,
        boolean blacklist,
        BigDecimal requestedAmount,
        Integer requestedTenure,
        String loanPurpose,
        AssetType assetType,
        String licensePlate,
        boolean duplicatedAsset,
        BigDecimal assetFinalValue,
        BigDecimal loanableAmount,
        BigDecimal ltvRatio,
        BigDecimal marketValue,
        BigDecimal totalDeductionAmount,
        String scoreGrade,
        boolean appliesToAllLoanPurposes,
        Set<String> allowedLoanPurposes,
        Set<String> allowedAssetTypes,
        Set<Integer> allowedTenors,
        Set<String> allowedScoreGrades,
        BigDecimal minLoanAmount,
        BigDecimal effectiveMaxLoanAmount
) {

    public static RuleContext customer(String customerCode, LocalDate dateOfBirth, boolean blacklist) {
        return base(
                customerCode,
                dateOfBirth,
                blacklist,
                null,
                null,
                null,
                null,
                null,
                false
        );
    }

    public static RuleContext loan(BigDecimal requestedAmount, Integer requestedTenure, String loanPurpose) {
        return base(
                null,
                null,
                false,
                requestedAmount,
                requestedTenure,
                loanPurpose,
                null,
                null,
                false
        );
    }

    public static RuleContext asset(AssetType assetType, String licensePlate, boolean duplicatedAsset) {
        return base(
                null,
                null,
                false,
                null,
                null,
                null,
                assetType,
                licensePlate,
                duplicatedAsset
        );
    }

    public static RuleContext valuation(BigDecimal assetFinalValue, BigDecimal loanableAmount, BigDecimal ltvRatio) {
        return valuation(assetFinalValue, loanableAmount, ltvRatio, null, null);
    }

    public static RuleContext valuation(
            BigDecimal assetFinalValue,
            BigDecimal loanableAmount,
            BigDecimal ltvRatio,
            BigDecimal marketValue,
            BigDecimal totalDeductionAmount
    ) {
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
                ltvRatio,
                marketValue,
                totalDeductionAmount,
                null,
                false,
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(),
                null,
                null
        );
    }

    public static RuleContext loanProduct(
            String selectedLoanPurpose,
            AssetType selectedAssetType,
            Integer selectedTenor,
            String scoreGrade,
            boolean appliesToAllLoanPurposes,
            Set<String> allowedLoanPurposes,
            Set<String> allowedAssetTypes,
            Set<Integer> allowedTenors,
            Set<String> allowedScoreGrades,
            BigDecimal minLoanAmount,
            BigDecimal effectiveMaxLoanAmount
    ) {
        return new RuleContext(
                null,
                null,
                false,
                null,
                selectedTenor,
                selectedLoanPurpose,
                selectedAssetType,
                null,
                false,
                null,
                null,
                null,
                null,
                null,
                scoreGrade,
                appliesToAllLoanPurposes,
                allowedLoanPurposes,
                allowedAssetTypes,
                allowedTenors,
                allowedScoreGrades,
                minLoanAmount,
                effectiveMaxLoanAmount
        );
    }

    private static RuleContext base(
            String customerCode,
            LocalDate dateOfBirth,
            boolean blacklist,
            BigDecimal requestedAmount,
            Integer requestedTenure,
            String loanPurpose,
            AssetType assetType,
            String licensePlate,
            boolean duplicatedAsset
    ) {
        return new RuleContext(
                customerCode,
                dateOfBirth,
                blacklist,
                requestedAmount,
                requestedTenure,
                loanPurpose,
                assetType,
                licensePlate,
                duplicatedAsset,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(),
                null,
                null
        );
    }
}
