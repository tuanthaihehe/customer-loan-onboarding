package com.f88.loanonboarding.dto.response.asset;

import com.f88.loanonboarding.enums.AssetType;

public record AssetSnapshotResponse(
        String applicationCode,
        AssetType assetType,
        String licensePlate,
        String brand,
        String model,
        String vehicleVariant,
        Integer manufactureYear,
        String vehicleColor
) {
}
