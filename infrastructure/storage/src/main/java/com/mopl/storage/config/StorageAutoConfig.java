package com.mopl.storage.config;

import com.mopl.storage.provider.LocalStorageProvider;
import com.mopl.storage.provider.S3StorageProvider;
import com.mopl.storage.provider.StorageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@AutoConfiguration
@EnableConfigurationProperties(StorageProperties.class)
@RequiredArgsConstructor
public class StorageAutoConfig {

    private final StorageProperties storageProperties;

    @Bean
    @ConditionalOnProperty(name = "mopl.storage.type", havingValue = "local", matchIfMissing = true)
    public StorageProvider localStorageProvider() {
        return new LocalStorageProvider(storageProperties.local());
    }

    @Bean
    @ConditionalOnProperty(name = "mopl.storage.type", havingValue = "s3")
    public S3Client s3Client() {
        StorageProperties.S3 s3 = storageProperties.s3();
        S3ClientBuilder builder = S3Client.builder()
            .region(Region.of(s3.region()))
            .credentialsProvider(resolveCredentialsProvider(s3));
        if (StringUtils.hasText(s3.endpoint())) {
            builder.endpointOverride(URI.create(s3.endpoint()))
                .forcePathStyle(true);
        }
        return builder.build();
    }

    @Bean
    @ConditionalOnProperty(name = "mopl.storage.type", havingValue = "s3")
    public S3Presigner s3Presigner() {
        StorageProperties.S3 s3 = storageProperties.s3();
        S3Presigner.Builder builder = S3Presigner.builder()
            .region(Region.of(s3.region()))
            .credentialsProvider(resolveCredentialsProvider(s3));
        if (StringUtils.hasText(s3.endpoint())) {
            builder.endpointOverride(URI.create(s3.endpoint()));
        }
        return builder.build();
    }

    private AwsCredentialsProvider resolveCredentialsProvider(StorageProperties.S3 s3) {
        if (StringUtils.hasText(s3.accessKey()) && StringUtils.hasText(s3.secretKey())) {
            return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(s3.accessKey(), s3.secretKey())
            );
        }
        // IAM Role 사용 (ECS Task Role, EC2 Instance Profile 등)
        return DefaultCredentialsProvider.builder().build();
    }

    @Bean
    @ConditionalOnProperty(name = "mopl.storage.type", havingValue = "s3")
    public StorageProvider s3StorageProvider(
        S3Client s3Client,
        S3Presigner s3Presigner
    ) {
        return new S3StorageProvider(
            storageProperties.s3(),
            s3Client,
            s3Presigner
        );
    }
}
