package com.f88.loanonboarding.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.f88.loanonboarding.dto.response.loan.LoanApplicationDocumentListResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDocumentUploadResponse;

public interface LoanApplicationDocumentService {

    LoanApplicationDocumentUploadResponse uploadDocuments(
            String applicationCode,
            List<String> documentTypeCodes,
            List<MultipartFile> files,
            String uploadedBy
    );

    LoanApplicationDocumentListResponse findDocuments(String applicationCode);

    void deleteDocument(String applicationCode, String documentId);
}
