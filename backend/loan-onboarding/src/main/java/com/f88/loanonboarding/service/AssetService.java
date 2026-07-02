package com.f88.loanonboarding.service;

import com.f88.loanonboarding.dto.request.asset.AssetLookupRequest;
import com.f88.loanonboarding.dto.request.asset.SaveAssetLegalInfoRequest;
import com.f88.loanonboarding.dto.request.asset.SaveAssetSnapshotRequest;
import com.f88.loanonboarding.dto.request.asset.SaveVehicleRegistrationRequest;
import com.f88.loanonboarding.dto.response.asset.AssetLookupResponse;
import com.f88.loanonboarding.dto.response.asset.AssetLegalInfoResponse;
import com.f88.loanonboarding.dto.response.asset.AssetSnapshotResponse;

public interface AssetService {

    AssetLookupResponse lookup(AssetLookupRequest request);

    AssetSnapshotResponse saveSnapshot(String applicationCode, SaveAssetSnapshotRequest request);

    AssetLegalInfoResponse saveLegalInfo(String applicationCode, SaveAssetLegalInfoRequest request);

    AssetLegalInfoResponse saveVehicleRegistration(String applicationCode, SaveVehicleRegistrationRequest request);
}
