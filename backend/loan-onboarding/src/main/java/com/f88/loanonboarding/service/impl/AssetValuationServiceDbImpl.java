package com.f88.loanonboarding.service.impl;

import org.springframework.stereotype.Service;

import com.f88.loanonboarding.common.error.ErrorCode;
import com.f88.loanonboarding.dto.request.asset.AssetValuationPreviewRequest;
import com.f88.loanonboarding.dto.response.asset.AssetValuationPreviewResponse;
import com.f88.loanonboarding.exception.BusinessException;
import com.f88.loanonboarding.service.AssetValuationService;

@Service
public class AssetValuationServiceDbImpl implements AssetValuationService {

    @Override
    public AssetValuationPreviewResponse preview(String applicationCode, AssetValuationPreviewRequest request) {
        throw unsupportedAssetSchema();
    }

    @Override
    public AssetValuationPreviewResponse savePreview(String applicationCode, AssetValuationPreviewRequest request) {
        throw unsupportedAssetSchema();
    }

    private BusinessException unsupportedAssetSchema() {
        return new BusinessException(
                ErrorCode.SCHEMA_NOT_READY,
                "Database hiện tại chưa có bảng định giá tài sản; migration đang loại asset khỏi phạm vi phiên bản này"
        );
    }
}
