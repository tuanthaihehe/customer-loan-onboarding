package com.f88.loanonboarding.dto.request.asset;

import com.f88.loanonboarding.enums.AssetType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AssetLookupRequest(
        @NotNull(message = "Loại tài sản là bắt buộc")
        AssetType assetType,

        @NotBlank(message = "Biển số xe là bắt buộc")
        String licensePlate,

        String chassisNumber,
        String engineNumber
) {
}
