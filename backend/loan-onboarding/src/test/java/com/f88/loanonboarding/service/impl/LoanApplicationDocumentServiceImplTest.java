package com.f88.loanonboarding.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.f88.loanonboarding.entity.DocumentType;
import com.f88.loanonboarding.entity.LoanApplication;
import com.f88.loanonboarding.entity.LoanApplicationDocument;
import com.f88.loanonboarding.entity.LoanApplicationState;
import com.f88.loanonboarding.exception.BusinessException;
import com.f88.loanonboarding.repository.DocumentTypeRepository;
import com.f88.loanonboarding.repository.LoanApplicationDocumentRepository;
import com.f88.loanonboarding.repository.LoanApplicationRepository;
import com.f88.loanonboarding.service.DocumentStorageService;
import com.f88.loanonboarding.service.StoredDocumentFile;

@ExtendWith(MockitoExtension.class)
class LoanApplicationDocumentServiceImplTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private DocumentTypeRepository documentTypeRepository;

    @Mock
    private LoanApplicationDocumentRepository documentRepository;

    @Mock
    private DocumentStorageService documentStorageService;

    private LoanApplicationDocumentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new LoanApplicationDocumentServiceImpl(
                loanApplicationRepository,
                documentTypeRepository,
                documentRepository,
                documentStorageService
        );
    }

    @Test
    void uploadDocumentsStoresFileAndCreatesDocumentReference() {
        LoanApplication application = application("APP-2026-000001");
        DocumentType documentType = documentType("CITIZEN_ID_FRONT", "CCCD mặt trước");
        MockMultipartFile file = image("front.jpg");

        when(loanApplicationRepository.findByLoanApplicationCode("APP-2026-000001"))
                .thenReturn(Optional.of(application));
        when(documentTypeRepository.findByCode("CITIZEN_ID_FRONT"))
                .thenReturn(Optional.of(documentType));
        when(documentStorageService.store("APP-2026-000001", "CITIZEN_ID_FRONT", file))
                .thenReturn(new StoredDocumentFile("s3-key", "https://bucket.s3.ap-southeast-1.amazonaws.com/s3-key"));
        when(documentRepository.findByLoanApplicationAndDocumentType(application, documentType))
                .thenReturn(Optional.empty());
        when(documentRepository.save(any(LoanApplicationDocument.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.uploadDocuments(
                "APP-2026-000001",
                List.of("CITIZEN_ID_FRONT"),
                List.of(file),
                "tester"
        );

        assertThat(response.uploadedCount()).isEqualTo(1);
        assertThat(response.documents()).singleElement()
                .satisfies(document -> {
                    assertThat(document.documentTypeCode()).isEqualTo("CITIZEN_ID_FRONT");
                    assertThat(document.fileUrl()).isEqualTo("https://bucket.s3.ap-southeast-1.amazonaws.com/s3-key");
                    assertThat(document.fileName()).isEqualTo("front.jpg");
                });

        ArgumentCaptor<LoanApplicationDocument> captor = ArgumentCaptor.forClass(LoanApplicationDocument.class);
        verify(documentRepository).save(captor.capture());
        assertThat(captor.getValue().getLoanApplication()).isSameAs(application);
        assertThat(captor.getValue().getDocumentType()).isSameAs(documentType);
        assertThat(captor.getValue().getUploadedBy()).isEqualTo("tester");
    }

    @Test
    void uploadDocumentsReplacesExistingDocumentReferenceForSameType() {
        LoanApplication application = application("APP-2026-000001");
        DocumentType documentType = documentType("CITIZEN_ID_FRONT", "CCCD mặt trước");
        LoanApplicationDocument existing = new LoanApplicationDocument();
        existing.setId(UUID.randomUUID());
        existing.setLoanApplication(application);
        existing.setDocumentType(documentType);
        existing.setFileUrl("old-url");
        existing.setFileName("old.jpg");
        MockMultipartFile file = image("front-new.jpg");

        when(loanApplicationRepository.findByLoanApplicationCode("APP-2026-000001"))
                .thenReturn(Optional.of(application));
        when(documentTypeRepository.findByCode("CITIZEN_ID_FRONT"))
                .thenReturn(Optional.of(documentType));
        when(documentStorageService.store("APP-2026-000001", "CITIZEN_ID_FRONT", file))
                .thenReturn(new StoredDocumentFile("new-key", "new-url"));
        when(documentRepository.findByLoanApplicationAndDocumentType(application, documentType))
                .thenReturn(Optional.of(existing));
        when(documentRepository.save(any(LoanApplicationDocument.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.uploadDocuments("APP-2026-000001", List.of("CITIZEN_ID_FRONT"), List.of(file), null);

        ArgumentCaptor<LoanApplicationDocument> captor = ArgumentCaptor.forClass(LoanApplicationDocument.class);
        verify(documentRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(existing.getId());
        assertThat(captor.getValue().getFileUrl()).isEqualTo("new-url");
        assertThat(captor.getValue().getFileName()).isEqualTo("front-new.jpg");
    }

    @Test
    void uploadDocumentsRejectsMismatchedTypesAndFiles() {
        assertThatThrownBy(() -> service.uploadDocuments(
                "APP-2026-000001",
                List.of("CITIZEN_ID_FRONT"),
                List.of(image("front.jpg"), image("back.jpg")),
                null
        )).isInstanceOf(BusinessException.class)
                .hasMessageContaining("documentTypeCodes");
    }

    @Test
    void uploadDocumentsRejectsSubmittedApplication() {
        LoanApplication application = application("APP-2026-000001", "APP_SUBMITTED");

        when(loanApplicationRepository.findByLoanApplicationCode("APP-2026-000001"))
                .thenReturn(Optional.of(application));

        assertThatThrownBy(() -> service.uploadDocuments(
                "APP-2026-000001",
                List.of("CITIZEN_ID_FRONT"),
                List.of(image("front.jpg")),
                null
        )).isInstanceOf(BusinessException.class)
                .hasMessageContaining("Không được upload chứng từ");
    }

    @Test
    void findDocumentsReturnsDocumentReferencesForApplication() {
        LoanApplication application = application("APP-2026-000001");
        DocumentType documentType = documentType("CITIZEN_ID_FRONT", "CCCD mặt trước");
        LoanApplicationDocument document = new LoanApplicationDocument();
        document.setId(UUID.randomUUID());
        document.setLoanApplication(application);
        document.setDocumentType(documentType);
        document.setFileUrl("https://bucket/front.jpg");
        document.setFileName("front.jpg");
        document.setUploadedAt(LocalDateTime.of(2026, 7, 8, 10, 0));
        document.setUploadedBy("tester");

        when(loanApplicationRepository.findByLoanApplicationCode("APP-2026-000001"))
                .thenReturn(Optional.of(application));
        when(documentRepository.findByLoanApplicationIdOrderByUploadedAtDesc(application.getId()))
                .thenReturn(List.of(document));

        var response = service.findDocuments("APP-2026-000001");

        assertThat(response.applicationCode()).isEqualTo("APP-2026-000001");
        assertThat(response.documentCount()).isEqualTo(1);
        assertThat(response.documents()).singleElement()
                .satisfies(item -> {
                    assertThat(item.documentId()).isEqualTo(document.getId());
                    assertThat(item.documentTypeCode()).isEqualTo("CITIZEN_ID_FRONT");
                    assertThat(item.documentTypeName()).isEqualTo("CCCD mặt trước");
                    assertThat(item.fileUrl()).isEqualTo("https://bucket/front.jpg");
                    assertThat(item.fileName()).isEqualTo("front.jpg");
                    assertThat(item.uploadedBy()).isEqualTo("tester");
                });
    }

    private LoanApplication application(String applicationCode) {
        return application(applicationCode, "APP_DRAFT");
    }

    private LoanApplication application(String applicationCode, String stateCode) {
        LoanApplication application = new LoanApplication();
        application.setId(UUID.randomUUID());
        application.setLoanApplicationCode(applicationCode);
        LoanApplicationState state = new LoanApplicationState();
        state.setCode(stateCode);
        state.setName(stateCode);
        application.setCurrentState(state);
        return application;
    }

    private DocumentType documentType(String code, String name) {
        DocumentType documentType = new DocumentType();
        documentType.setId(UUID.randomUUID());
        documentType.setCode(code);
        documentType.setName(name);
        documentType.setActive(true);
        return documentType;
    }

    private MockMultipartFile image(String name) {
        return new MockMultipartFile(
                "files",
                name,
                "image/jpeg",
                "fake-image".getBytes(StandardCharsets.UTF_8)
        );
    }
}
