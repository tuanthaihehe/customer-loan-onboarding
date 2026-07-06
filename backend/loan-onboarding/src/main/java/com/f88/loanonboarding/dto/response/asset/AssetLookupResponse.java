package com.f88.loanonboarding.dto.response.asset;

import com.f88.loanonboarding.enums.AssetStatus;

public record AssetLookupResponse(
        boolean found,
        String assetCode,
        AssetStatus assetState,
        boolean eligibleForPledge,
        String reasonCode,
        String reasonMessage
) {
}
