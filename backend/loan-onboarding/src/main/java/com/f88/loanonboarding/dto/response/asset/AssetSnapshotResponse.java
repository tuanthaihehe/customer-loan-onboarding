package com.f88.loanonboarding.dto.response.asset;

public record AssetSnapshotResponse(
        String applicationCode,
        String assetType,
        String licensePlate,
        String brand,
        String model,
        String vehicleVariant,
        Integer manufactureYear,
        String vehicleColor
) {
}
