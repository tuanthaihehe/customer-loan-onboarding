package com.f88.loanonboarding.dto.response.loan;

public record FinalOfferAssetSummaryResponse(
        String assetCode,
        String assetTypeCode,
        String assetTypeName,
        String licensePlate,
        String brandCode,
        String brandName,
        String modelCode,
        String modelName,
        String versionCode,
        String versionName,
        String variantCode,
        String variantName,
        Integer manufactureYear,
        String colorCode,
        String colorName,
        String assetStatus
) {
}
