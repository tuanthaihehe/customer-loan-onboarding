package com.f88.loanonboarding.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import com.f88.loanonboarding.dto.response.loan.LoanApplicationDocumentUploadResponse;
import com.f88.loanonboarding.service.LoanApplicationDocumentService;
import com.f88.loanonboarding.service.LoanApplicationOnboardingService;

@WebMvcTest(LoanApplicationOnboardingController.class)
class LoanApplicationOnboardingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoanApplicationOnboardingService onboardingService;

    @MockBean
    private LoanApplicationDocumentService documentService;

    @Test
    void uploadDocumentsBindsMultipartTextFieldsAndFiles() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "front.jpg",
                "image/jpeg",
                "fake-image".getBytes()
        );
        LoanApplicationDocumentUploadResponse response = new LoanApplicationDocumentUploadResponse(
                "APP-2026-000001",
                1,
                List.of()
        );

        when(documentService.uploadDocuments(
                eq("APP-2026-000001"),
                eq(List.of("CITIZEN_ID_FRONT")),
                org.mockito.ArgumentMatchers.<List<MultipartFile>>any(),
                eq("tester")
        )).thenReturn(response);

        mockMvc.perform(multipart("/api/v1/loan-applications/onboarding/{applicationCode}/documents", "APP-2026-000001")
                        .file(file)
                        .param("documentTypeCodes", "CITIZEN_ID_FRONT")
                        .param("uploadedBy", "tester"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.applicationCode").value("APP-2026-000001"))
                .andExpect(jsonPath("$.data.uploadedCount").value(1));

        verify(documentService).uploadDocuments(
                eq("APP-2026-000001"),
                eq(List.of("CITIZEN_ID_FRONT")),
                org.mockito.ArgumentMatchers.<List<MultipartFile>>any(),
                eq("tester")
        );
    }
}
