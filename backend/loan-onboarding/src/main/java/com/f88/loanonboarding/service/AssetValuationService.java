package com.f88.loanonboarding.service;

import com.f88.loanonboarding.dto.request.asset.AssetValuationPreviewRequest;
import com.f88.loanonboarding.dto.response.asset.AssetValuationPreviewResponse;

public interface AssetValuationService {

    AssetValuationPreviewResponse preview(String applicationCode, AssetValuationPreviewRequest request);

    AssetValuationPreviewResponse savePreview(String applicationCode, AssetValuationPreviewRequest request);
}
