package com.f88.loanonboarding.service;

import org.springframework.web.multipart.MultipartFile;

public interface DocumentStorageService {

    StoredObject store(
            String ownerType,
            String ownerCode,
            String documentCode,
            String fileId,
            String extension,
            String contentType,
            MultipartFile file
    );

    void delete(String storagePathOrFileUrl);

    void deleteQuietly(String storagePath);

    record StoredObject(String storagePath, String fileUrl) {
    }
}
