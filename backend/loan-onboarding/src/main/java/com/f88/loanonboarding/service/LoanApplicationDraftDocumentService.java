package com.f88.loanonboarding.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.f88.loanonboarding.dto.response.loan.DraftDocumentResponses;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDraftStepActionResponse;

public interface LoanApplicationDraftDocumentService {

    List<DraftDocumentResponses.RequirementGroup> getRequirements();

    DraftDocumentResponses.State getState(String draftCode);

    DraftDocumentResponses.UploadUrlResult upload(String draftCode, String documentCode, MultipartFile file);

    DraftDocumentResponses.State delete(String draftCode, String documentCode);

    LoanApplicationDraftStepActionResponse complete(String draftCode);
}
