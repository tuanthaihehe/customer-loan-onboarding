package com.f88.loanonboarding.service;

import com.f88.loanonboarding.dto.request.asset.AssetValuationPreviewRequest;
import com.f88.loanonboarding.dto.response.asset.AssetValuationPreviewResponse;
import com.f88.loanonboarding.dto.response.asset.VehicleMarketPriceResponse;

public interface AssetValuationService {

    AssetValuationPreviewResponse preview(AssetValuationPreviewRequest request);

    VehicleMarketPriceResponse getMarketPrice(String vehicleVariant);

    AssetValuationPreviewResponse savePreview(String applicationCode, AssetValuationPreviewRequest request);
}
