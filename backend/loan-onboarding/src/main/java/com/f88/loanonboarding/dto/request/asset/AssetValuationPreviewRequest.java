package com.f88.loanonboarding.dto.request.asset;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record AssetValuationPreviewRequest(
        @Valid
        @NotNull(message = "Thông tin tài sản là bắt buộc")
        SaveAssetSnapshotRequest assetSnapshot,

        @Valid
        List<ValuationDeductionItemRequest> deductionItems
) {
}
