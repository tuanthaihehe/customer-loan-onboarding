package com.f88.loanonboarding.config;

import java.net.URI;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@EnableConfigurationProperties(S3StorageProperties.class)
public class S3StorageConfig {

    @Bean
    S3Client s3Client(S3StorageProperties properties) {
        var builder = S3Client.builder()
                .region(Region.of(blankToDefault(properties.region(), "ap-southeast-1")))
                .forcePathStyle(properties.pathStyleAccessEnabled());

        if (hasText(properties.accessKeyId()) && hasText(properties.secretAccessKey())) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(properties.accessKeyId(), properties.secretAccessKey())
            ));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        if (hasText(properties.endpoint())) {
            builder.endpointOverride(URI.create(properties.endpoint()));
        }

        return builder.build();
    }

    @Bean
    S3Presigner s3Presigner(S3StorageProperties properties) {
        var builder = S3Presigner.builder()
                .region(Region.of(blankToDefault(properties.region(), "ap-southeast-1")));

        if (hasText(properties.accessKeyId()) && hasText(properties.secretAccessKey())) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(properties.accessKeyId(), properties.secretAccessKey())
            ));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        if (hasText(properties.endpoint())) {
            builder.endpointOverride(URI.create(properties.endpoint()));
        }

        return builder.build();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String blankToDefault(String value, String defaultValue) {
        return hasText(value) ? value : defaultValue;
    }
}
