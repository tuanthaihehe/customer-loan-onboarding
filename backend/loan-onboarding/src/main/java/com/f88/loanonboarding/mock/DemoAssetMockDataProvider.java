package com.f88.loanonboarding.mock;

import org.springframework.stereotype.Component;

import com.f88.loanonboarding.dto.request.asset.SaveAssetSnapshotRequest;
import com.f88.loanonboarding.dto.response.asset.AssetLookupResponse;
import com.f88.loanonboarding.dto.response.asset.AssetSnapshotResponse;

@Component
public class DemoAssetMockDataProvider {

    public AssetLookupResponse eligibleAsset() {
        return new AssetLookupResponse(false, null, null, true, null);
    }

    public AssetSnapshotResponse snapshot(String applicationCode, SaveAssetSnapshotRequest request) {
        return new AssetSnapshotResponse(
                applicationCode,
                request.assetType(),
                request.licensePlate(),
                request.brand(),
                request.model(),
                request.vehicleVariant(),
                request.manufactureYear(),
                request.vehicleColor()
        );
    }
}
