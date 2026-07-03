package com.f88.loanonboarding.dto.request.asset;

import jakarta.validation.constraints.NotBlank;

public record SaveAssetLegalInfoRequest(
        @NotBlank(message = "Số khung là bắt buộc")
        String frameNumber,

        @NotBlank(message = "Số máy là bắt buộc")
        String engineNumber
) {
}
