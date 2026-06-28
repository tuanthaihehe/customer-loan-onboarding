package com.f88.loanonboarding.dto.response.asset;

public record AssetLookupResponse(
        boolean found,
        String assetCode,
        String assetState,
        boolean eligibleForPledge,
        String reasonCode
) {
}
