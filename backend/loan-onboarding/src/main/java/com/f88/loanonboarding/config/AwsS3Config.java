package com.f88.loanonboarding.config;

import java.net.URI;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableConfigurationProperties(AwsS3Properties.class)
public class AwsS3Config {

    @Bean
    public S3Client s3Client(AwsS3Properties properties) {
        var builder = S3Client.builder()
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(credentialsProvider(properties));

        if (StringUtils.hasText(properties.getEndpoint())) {
            builder.endpointOverride(URI.create(properties.getEndpoint()));
            builder.forcePathStyle(properties.isPathStyleAccessEnabled());
        }

        return builder.build();
    }

    private AwsCredentialsProvider credentialsProvider(AwsS3Properties properties) {
        if (StringUtils.hasText(properties.getAccessKeyId()) && StringUtils.hasText(properties.getSecretAccessKey())) {
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(
                    properties.getAccessKeyId(),
                    properties.getSecretAccessKey()
            ));
        }
        return DefaultCredentialsProvider.create();
    }
}
