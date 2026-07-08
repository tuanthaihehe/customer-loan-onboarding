package com.f88.loanonboarding.service;

import org.springframework.web.multipart.MultipartFile;

public interface DocumentStorageService {

    StoredDocumentFile store(String applicationCode, String documentTypeCode, MultipartFile file);
}
