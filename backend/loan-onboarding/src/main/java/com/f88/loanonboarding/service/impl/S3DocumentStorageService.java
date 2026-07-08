package com.f88.loanonboarding.service.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
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
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
public class S3DocumentStorageService implements DocumentStorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3StorageProperties properties;

    public S3DocumentStorageService(S3Client s3Client, S3Presigner s3Presigner, S3StorageProperties properties) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
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

    @Override
    public String createReadUrl(String storedUrl) {
        if (storedUrl == null || storedUrl.isBlank()) {
            return storedUrl;
        }
        if (properties.bucket() == null || properties.bucket().isBlank()) {
            return storedUrl;
        }

        return extractObjectKey(storedUrl)
                .map(this::presignGetObject)
                .orElse(storedUrl);
    }

    @Override
    public void delete(String storedUrl) {
        if (storedUrl == null || storedUrl.isBlank()) {
            return;
        }
        if (properties.bucket() == null || properties.bucket().isBlank()) {
            return;
        }

        extractObjectKey(storedUrl).ifPresent((key) -> {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(properties.bucket())
                    .key(key)
                    .build();

            try {
                s3Client.deleteObject(request);
            } catch (S3Exception ex) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Không xóa được chứng từ trên S3: " + ex.awsErrorDetails().errorMessage());
            }
        });
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

    private String presignGetObject(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(properties.bucket())
                .key(key)
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(resolvePresignedUrlDurationMinutes()))
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    private long resolvePresignedUrlDurationMinutes() {
        return properties.presignedUrlDurationMinutes() > 0
                ? properties.presignedUrlDurationMinutes()
                : 60L;
    }

    private Optional<String> extractObjectKey(String storedUrl) {
        try {
            String decodedUrl = URLDecoder.decode(storedUrl, StandardCharsets.UTF_8);

            if (properties.publicUrlBase() != null && !properties.publicUrlBase().isBlank()) {
                String publicBase = trimTrailingSlash(properties.publicUrlBase());
                if (decodedUrl.startsWith(publicBase + "/")) {
                    return Optional.of(decodedUrl.substring(publicBase.length() + 1));
                }
            }

            URI uri = URI.create(storedUrl);
            String path = uri.getPath();
            if (path == null || path.isBlank()) {
                return Optional.empty();
            }

            String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);
            String key = decodedPath.startsWith("/") ? decodedPath.substring(1) : decodedPath;
            String bucketPrefix = properties.bucket() + "/";

            if (key.startsWith(bucketPrefix)) {
                key = key.substring(bucketPrefix.length());
            }

            return key.isBlank() ? Optional.empty() : Optional.of(key);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String encodeKey(String key) {
        return URLEncoder.encode(key, StandardCharsets.UTF_8).replace("+", "%20").replace("%2F", "/");
    }
}
