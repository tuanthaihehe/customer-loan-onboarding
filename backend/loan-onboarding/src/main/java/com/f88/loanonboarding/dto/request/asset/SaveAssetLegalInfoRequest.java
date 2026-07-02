package com.f88.loanonboarding.dto.request.asset;

import jakarta.validation.constraints.NotBlank;

public record SaveAssetLegalInfoRequest(
        @NotBlank(message = "So khung la bat buoc")
        String frameNumber,

        @NotBlank(message = "So may la bat buoc")
        String engineNumber
) {
}
