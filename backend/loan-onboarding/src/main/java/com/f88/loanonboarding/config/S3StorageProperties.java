package com.f88.loanonboarding.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage.s3")
public record S3StorageProperties(
        String accessKeyId,
        String secretAccessKey,
        String region,
        String bucket,
        String endpoint,
        String publicUrlBase,
        boolean pathStyleAccessEnabled,
        long presignedUrlDurationMinutes
) {
}
