package com.f88.loanonboarding.service.impl;

import org.springframework.stereotype.Service;

import com.f88.loanonboarding.common.error.ErrorCode;
import com.f88.loanonboarding.dto.request.asset.AssetLookupRequest;
import com.f88.loanonboarding.dto.request.asset.SaveAssetSnapshotRequest;
import com.f88.loanonboarding.dto.response.asset.AssetLookupResponse;
import com.f88.loanonboarding.dto.response.asset.AssetSnapshotResponse;
import com.f88.loanonboarding.exception.BusinessException;
import com.f88.loanonboarding.service.AssetService;

@Service
public class AssetServiceDbImpl implements AssetService {

    @Override
    public AssetLookupResponse lookup(AssetLookupRequest request) {
        throw unsupportedAssetSchema();
    }

    @Override
    public AssetSnapshotResponse saveSnapshot(String applicationCode, SaveAssetSnapshotRequest request) {
        throw unsupportedAssetSchema();
    }

    private BusinessException unsupportedAssetSchema() {
        return new BusinessException(
                ErrorCode.SCHEMA_NOT_READY,
                "Database hiện tại chưa có bảng tài sản; migration đang loại asset khỏi phạm vi phiên bản này"
        );
    }
}
