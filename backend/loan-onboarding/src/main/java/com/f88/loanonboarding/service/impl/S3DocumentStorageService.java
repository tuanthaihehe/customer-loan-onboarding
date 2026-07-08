package com.f88.loanonboarding.service.impl;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.f88.loanonboarding.common.error.ErrorCode;
import com.f88.loanonboarding.config.S3StorageProperties;
import com.f88.loanonboarding.exception.BusinessException;
import com.f88.loanonboarding.service.DocumentStorageService;
import com.f88.loanonboarding.service.StoredDocumentFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class S3DocumentStorageService implements DocumentStorageService {

    private final S3Client s3Client;
    private final S3StorageProperties properties;

    public S3DocumentStorageService(S3Client s3Client, S3StorageProperties properties) {
        this.s3Client = s3Client;
        this.properties = properties;
    }

    @Override
    public StoredDocumentFile store(String applicationCode, String documentTypeCode, MultipartFile file) {
        if (properties.bucket() == null || properties.bucket().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "AWS_S3_BUCKET is required to upload documents.");
        }

        String key = buildObjectKey(applicationCode, documentTypeCode, file.getOriginalFilename());
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        try {
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Không đọc được file chứng từ: " + file.getOriginalFilename());
        } catch (S3Exception ex) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Không upload được chứng từ lên S3: " + ex.awsErrorDetails().errorMessage());
        }

        return new StoredDocumentFile(key, buildFileUrl(key));
    }

    private String buildObjectKey(String applicationCode, String documentTypeCode, String originalFilename) {
        String filename = sanitizeFilename(originalFilename);
        return "loan-applications/%s/documents/%s/%s-%s".formatted(
                sanitizePathSegment(applicationCode),
                sanitizePathSegment(documentTypeCode),
                UUID.randomUUID(),
                filename
        );
    }

    private String sanitizeFilename(String originalFilename) {
        String filename = originalFilename == null || originalFilename.isBlank()
                ? "document"
                : originalFilename;
        return filename
                .replace('\\', '/')
                .substring(filename.replace('\\', '/').lastIndexOf('/') + 1)
                .replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private String sanitizePathSegment(String value) {
        return value == null
                ? "unknown"
                : value.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9._-]", "_");
    }

    private String buildFileUrl(String key) {
        if (properties.publicUrlBase() != null && !properties.publicUrlBase().isBlank()) {
            return trimTrailingSlash(properties.publicUrlBase()) + "/" + encodeKey(key);
        }

        if (properties.endpoint() != null && !properties.endpoint().isBlank()) {
            String endpoint = trimTrailingSlash(properties.endpoint());
            if (properties.pathStyleAccessEnabled()) {
                return endpoint + "/" + properties.bucket() + "/" + encodeKey(key);
            }
        }

        String region = properties.region() == null || properties.region().isBlank()
                ? "ap-southeast-1"
                : properties.region();
        return "https://%s.s3.%s.amazonaws.com/%s".formatted(properties.bucket(), region, encodeKey(key));
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String encodeKey(String key) {
        return URLEncoder.encode(key, StandardCharsets.UTF_8).replace("+", "%20").replace("%2F", "/");
    }
}
