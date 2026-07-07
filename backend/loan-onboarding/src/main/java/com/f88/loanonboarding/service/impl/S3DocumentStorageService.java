package com.f88.loanonboarding.service.impl;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.f88.loanonboarding.common.error.ErrorCode;
import com.f88.loanonboarding.config.AwsS3Properties;
import com.f88.loanonboarding.exception.BusinessException;
import com.f88.loanonboarding.service.DocumentStorageService;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3DocumentStorageService implements DocumentStorageService {

    private static final String S3_URI_PREFIX = "s3://";

    private final S3Client s3Client;
    private final AwsS3Properties properties;
    private final Path legacyBaseUploadDir;

    public S3DocumentStorageService(S3Client s3Client, AwsS3Properties properties) {
        this.s3Client = s3Client;
        this.properties = properties;
        this.legacyBaseUploadDir = Paths.get(System.getProperty("java.io.tmpdir"), "loan-onboarding-uploads");
    }

    @Override
    public StoredObject store(
            String ownerType,
            String ownerCode,
            String documentCode,
            String fileId,
            String extension,
            String contentType,
            MultipartFile file
    ) {
        ensureBucketConfigured();
        String objectKey = "%s/%s/%s/%s.%s".formatted(ownerType, ownerCode, documentCode, fileId, extension);
        PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(objectKey)
                .contentLength(file.getSize());

        if (StringUtils.hasText(contentType)) {
            requestBuilder.contentType(contentType);
        }

        try {
            s3Client.putObject(requestBuilder.build(), RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return new StoredObject(s3Uri(objectKey), fileUrl(objectKey));
        } catch (IOException | SdkException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Không upload được file chứng từ lên S3, vui lòng thử lại");
        }
    }

    @Override
    public void delete(String storagePathOrFileUrl) {
        if (!StringUtils.hasText(storagePathOrFileUrl)) {
            return;
        }
        if (storagePathOrFileUrl.startsWith(S3_URI_PREFIX)) {
            deleteS3ObjectByKey(objectKeyFromS3Uri(storagePathOrFileUrl));
            return;
        }
        String objectKey = objectKeyFromFileUrl(storagePathOrFileUrl);
        if (objectKey != null) {
            deleteS3ObjectByKey(objectKey);
            return;
        }
        try {
            deleteLegacyLocalFile(storagePathOrFileUrl);
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Không xóa được file chứng từ, vui lòng thử lại");
        }
    }

    @Override
    public void deleteQuietly(String storagePath) {
        try {
            delete(storagePath);
        } catch (RuntimeException ignored) {
            // Xóa file cũ không thành công thì không chặn luồng nghiệp vụ upload/xóa chứng từ.
        }
    }

    private void deleteS3ObjectByKey(String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            return;
        }
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(objectKey)
                    .build());
        } catch (SdkException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Không xóa được file chứng từ trên S3, vui lòng thử lại");
        }
    }

    private String objectKeyFromS3Uri(String storagePath) {
        String expectedPrefix = S3_URI_PREFIX + properties.getBucket() + "/";
        if (!storagePath.startsWith(expectedPrefix)) {
            return null;
        }
        return storagePath.substring(expectedPrefix.length());
    }

    private String objectKeyFromFileUrl(String fileUrl) {
        String normalizedUrl = stripQuery(trimTrailingSlash(fileUrl));
        String publicBase = trimTrailingSlash(properties.getPublicUrlBase());
        if (StringUtils.hasText(publicBase) && normalizedUrl.startsWith(publicBase + "/")) {
            return decodeObjectKey(normalizedUrl.substring(publicBase.length() + 1));
        }
        String endpoint = trimTrailingSlash(properties.getEndpoint());
        if (StringUtils.hasText(endpoint)) {
            String pathStylePrefix = endpoint + "/" + properties.getBucket() + "/";
            if (properties.isPathStyleAccessEnabled() && normalizedUrl.startsWith(pathStylePrefix)) {
                return decodeObjectKey(normalizedUrl.substring(pathStylePrefix.length()));
            }
            if (normalizedUrl.startsWith(endpoint + "/")) {
                return decodeObjectKey(normalizedUrl.substring(endpoint.length() + 1));
            }
        }
        String awsPrefix = "https://" + properties.getBucket() + ".s3." + properties.getRegion() + ".amazonaws.com/";
        if (normalizedUrl.startsWith(awsPrefix)) {
            return decodeObjectKey(normalizedUrl.substring(awsPrefix.length()));
        }
        return null;
    }

    private String stripQuery(String value) {
        int queryIndex = value.indexOf('?');
        return queryIndex < 0 ? value : value.substring(0, queryIndex);
    }

    private String decodeObjectKey(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private void deleteLegacyLocalFile(String storagePath) throws IOException {
        Path filePath = Paths.get(storagePath).normalize();
        if (filePath.startsWith(legacyBaseUploadDir.normalize())) {
            Files.deleteIfExists(filePath);
        }
    }

    private void ensureBucketConfigured() {
        if (!StringUtils.hasText(properties.getBucket())) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Chưa cấu hình AWS_S3_BUCKET để lưu chứng từ");
        }
    }

    private String s3Uri(String objectKey) {
        return S3_URI_PREFIX + properties.getBucket() + "/" + objectKey;
    }

    private String fileUrl(String objectKey) {
        if (StringUtils.hasText(properties.getPublicUrlBase())) {
            return trimTrailingSlash(properties.getPublicUrlBase()) + "/" + objectKey;
        }
        if (StringUtils.hasText(properties.getEndpoint())) {
            String endpoint = trimTrailingSlash(properties.getEndpoint());
            if (properties.isPathStyleAccessEnabled()) {
                return endpoint + "/" + properties.getBucket() + "/" + objectKey;
            }
            return endpoint + "/" + objectKey;
        }
        return "https://" + properties.getBucket() + ".s3." + properties.getRegion() + ".amazonaws.com/" + objectKey;
    }

    private String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        String result = value;
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
