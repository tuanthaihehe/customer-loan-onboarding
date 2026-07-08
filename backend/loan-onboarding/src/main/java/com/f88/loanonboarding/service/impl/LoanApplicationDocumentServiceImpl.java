package com.f88.loanonboarding.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.f88.loanonboarding.common.error.ErrorCode;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDocumentUploadResponse;
import com.f88.loanonboarding.entity.DocumentType;
import com.f88.loanonboarding.entity.LoanApplication;
import com.f88.loanonboarding.entity.LoanApplicationDocument;
import com.f88.loanonboarding.exception.BusinessException;
import com.f88.loanonboarding.repository.DocumentTypeRepository;
import com.f88.loanonboarding.repository.LoanApplicationDocumentRepository;
import com.f88.loanonboarding.repository.LoanApplicationRepository;
import com.f88.loanonboarding.service.DocumentStorageService;
import com.f88.loanonboarding.service.LoanApplicationDocumentService;
import com.f88.loanonboarding.service.StoredDocumentFile;

@Service
public class LoanApplicationDocumentServiceImpl implements LoanApplicationDocumentService {

    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024L * 1024L;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "application/pdf",
            "video/mp4",
            "video/webm",
            "video/quicktime"
    );
    private static final Set<String> NON_EDITABLE_STATES = Set.of(
            "APP_SUBMITTED",
            "APP_CANCELLED",
            "APP_EXPIRED",
            "APP_CLOSED"
    );

    private final LoanApplicationRepository loanApplicationRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final LoanApplicationDocumentRepository documentRepository;
    private final DocumentStorageService documentStorageService;

    public LoanApplicationDocumentServiceImpl(
            LoanApplicationRepository loanApplicationRepository,
            DocumentTypeRepository documentTypeRepository,
            LoanApplicationDocumentRepository documentRepository,
            DocumentStorageService documentStorageService
    ) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.documentTypeRepository = documentTypeRepository;
        this.documentRepository = documentRepository;
        this.documentStorageService = documentStorageService;
    }

    @Override
    @Transactional
    public LoanApplicationDocumentUploadResponse uploadDocuments(
            String applicationCode,
            List<String> documentTypeCodes,
            List<MultipartFile> files,
            String uploadedBy
    ) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Cần upload ít nhất một chứng từ.");
        }
        if (documentTypeCodes == null || documentTypeCodes.size() != files.size()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Số lượng documentTypeCodes phải khớp với số lượng files.");
        }

        LoanApplication application = loanApplicationRepository.findByLoanApplicationCode(applicationCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOAN_APPLICATION_NOT_FOUND));
        ensureEditableApplication(application);

        List<LoanApplicationDocumentUploadResponse.DocumentItem> uploadedDocuments = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int index = 0; index < files.size(); index++) {
            MultipartFile file = files.get(index);
            String documentTypeCode = normalizeDocumentTypeCode(documentTypeCodes.get(index));
            validateFile(file);

            DocumentType documentType = documentTypeRepository.findByCode(documentTypeCode)
                    .filter(DocumentType::isActive)
                    .orElseThrow(() -> new BusinessException(
                            ErrorCode.RESOURCE_NOT_FOUND,
                            "Loại chứng từ không tồn tại hoặc không còn hoạt động: " + documentTypeCode
                    ));

            StoredDocumentFile storedFile = documentStorageService.store(applicationCode, documentTypeCode, file);
            LoanApplicationDocument document = documentRepository
                    .findByLoanApplicationAndDocumentType(application, documentType)
                    .orElseGet(LoanApplicationDocument::new);

            document.setLoanApplication(application);
            document.setDocumentType(documentType);
            document.setFileUrl(storedFile.url());
            document.setFileName(file.getOriginalFilename());
            document.setUploadedAt(now);
            document.setUploadedBy(uploadedBy);

            LoanApplicationDocument saved = documentRepository.save(document);
            uploadedDocuments.add(toDocumentItem(saved));
        }

        return new LoanApplicationDocumentUploadResponse(
                application.getLoanApplicationCode(),
                uploadedDocuments.size(),
                uploadedDocuments
        );
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "File chứng từ không được rỗng.");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Dung lượng file chứng từ tối đa là 10MB.");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "File chứng từ chỉ hỗ trợ JPG, PNG, WEBP, PDF hoặc video MP4/WEBM/MOV.");
        }
    }

    private String normalizeDocumentTypeCode(String documentTypeCode) {
        if (documentTypeCode == null || documentTypeCode.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "documentTypeCode là bắt buộc.");
        }
        return documentTypeCode.trim().toUpperCase();
    }

    private void ensureEditableApplication(LoanApplication application) {
        if (application.getCurrentState() == null || NON_EDITABLE_STATES.contains(application.getCurrentState().getCode())) {
            throw new BusinessException(
                    ErrorCode.INVALID_LOAN_APPLICATION_STATE,
                    "Không được upload chứng từ khi hồ sơ đã nộp, đã hủy hoặc đã hết hạn."
            );
        }
    }

    private LoanApplicationDocumentUploadResponse.DocumentItem toDocumentItem(LoanApplicationDocument document) {
        return new LoanApplicationDocumentUploadResponse.DocumentItem(
                document.getId(),
                document.getDocumentType().getCode(),
                document.getDocumentType().getName(),
                document.getFileUrl(),
                document.getFileName(),
                document.getUploadedAt()
        );
    }
}
