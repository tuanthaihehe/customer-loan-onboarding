package com.f88.loanonboarding.dto.request.asset;

import com.f88.loanonboarding.enums.AssetType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SaveAssetSnapshotRequest(
        @NotNull(message = "Loại tài sản là bắt buộc")
        AssetType assetType,

        @NotBlank(message = "Biển số xe là bắt buộc")
        String licensePlate,

        @NotBlank(message = "Hãng xe là bắt buộc")
        String brand,

        @NotBlank(message = "Dòng xe là bắt buộc")
        String model,

        String vehicleVariant,

        @NotNull(message = "Năm sản xuất là bắt buộc")
        Integer manufactureYear,

        String vehicleColor
) {
}
