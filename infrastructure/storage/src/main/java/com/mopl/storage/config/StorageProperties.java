package com.mopl.storage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.util.Assert;

import java.nio.file.Path;
import java.time.Duration;

@ConfigurationProperties("mopl.storage")
public record StorageProperties(
    @DefaultValue("local") StorageType type,
    Local local,
    S3 s3
) {

    public StorageProperties {
        if (type == StorageType.LOCAL) {
            Assert.notNull(local, "mopl.storage.local must not be null when type is LOCAL");
            Assert.notNull(local.rootPath(), "mopl.storage.local.root-path must not be null when type is LOCAL");
            Assert.hasText(local.baseUrl(), "mopl.storage.local.base-url must not be empty when type is LOCAL");
        }
        if (type == StorageType.S3) {
            Assert.notNull(s3, "mopl.storage.s3 must not be null when type is S3");
            Assert.hasText(s3.accessKey(), "mopl.storage.s3.access-key must not be empty when type is S3");
            Assert.hasText(s3.secretKey(), "mopl.storage.s3.secret-key must not be empty when type is S3");
            Assert.hasText(s3.region(), "mopl.storage.s3.region must not be empty when type is S3");
            Assert.hasText(s3.bucket(), "mopl.storage.s3.bucket must not be empty when type is S3");
        }
    }

    public enum StorageType {
        LOCAL, S3
    }

    public record Local(
        Path rootPath,
        String baseUrl
    ) {
    }

    public record S3(
        String accessKey,
        String secretKey,
        String region,
        String bucket,
        Duration presignedUrlExpiration
    ) {
    }
}
