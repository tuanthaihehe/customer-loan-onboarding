package com.f88.loanonboarding.service.impl;

import org.springframework.stereotype.Service;

import com.f88.loanonboarding.common.error.ErrorCode;
import com.f88.loanonboarding.dto.request.asset.AssetValuationPreviewRequest;
import com.f88.loanonboarding.dto.response.asset.AssetValuationPreviewResponse;
import com.f88.loanonboarding.exception.BusinessException;
import com.f88.loanonboarding.repository.LoanApplicationRepository;
import com.f88.loanonboarding.service.AssetValuationService;

@Service
public class AssetValuationServiceImpl implements AssetValuationService {

    private final LoanApplicationRepository loanApplicationRepository;

    public AssetValuationServiceImpl(LoanApplicationRepository loanApplicationRepository) {
        this.loanApplicationRepository = loanApplicationRepository;
    }

    @Override
    public AssetValuationPreviewResponse preview(String applicationCode, AssetValuationPreviewRequest request) {
        ensureApplicationExists(applicationCode);
        throw valuationSchemaMissing();
    }

    @Override
    public AssetValuationPreviewResponse savePreview(String applicationCode, AssetValuationPreviewRequest request) {
        ensureApplicationExists(applicationCode);
        throw valuationSchemaMissing();
    }

    private BusinessException valuationSchemaMissing() {
        return new BusinessException(
                ErrorCode.BUSINESS_RULE_VIOLATION,
                "Chua co bang asset valuation trong database that, khong tinh hoac luu du lieu gia."
        );
    }

    private void ensureApplicationExists(String applicationCode) {
        if (loanApplicationRepository.findByLoanApplicationCode(applicationCode).isEmpty()) {
            throw new BusinessException(ErrorCode.LOAN_APPLICATION_NOT_FOUND);
        }
    }
}
