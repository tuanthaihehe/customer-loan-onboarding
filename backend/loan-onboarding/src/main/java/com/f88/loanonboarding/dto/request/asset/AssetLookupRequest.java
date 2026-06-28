package com.f88.loanonboarding.dto.request.asset;

import jakarta.validation.constraints.NotBlank;

public record AssetLookupRequest(
        @NotBlank(message = "Loại tài sản là bắt buộc")
        String assetType,

        @NotBlank(message = "Biển số xe là bắt buộc")
        String licensePlate,

        String chassisNumber,
        String engineNumber
) {
}
