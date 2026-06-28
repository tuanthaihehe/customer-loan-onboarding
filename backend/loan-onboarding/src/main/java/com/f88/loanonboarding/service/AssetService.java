package com.f88.loanonboarding.service;

import com.f88.loanonboarding.dto.request.asset.AssetLookupRequest;
import com.f88.loanonboarding.dto.request.asset.SaveAssetSnapshotRequest;
import com.f88.loanonboarding.dto.response.asset.AssetLookupResponse;
import com.f88.loanonboarding.dto.response.asset.AssetSnapshotResponse;

public interface AssetService {

    AssetLookupResponse lookup(AssetLookupRequest request);

    AssetSnapshotResponse saveSnapshot(String applicationCode, SaveAssetSnapshotRequest request);
}
