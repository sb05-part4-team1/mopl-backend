package com.mopl.batch.cleanup.retention.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mopl.batch.cleanup.retention")
public record RetentionCleanupProperties(
    PolicyProperties defaults,
    PolicyProperties content,
    PolicyProperties storage,
    PolicyProperties deletionLog
) {

    public RetentionCleanupProperties {
        if (defaults == null || defaults.chunkSize() == null || defaults.chunkSize() <= 0) {
            throw new IllegalArgumentException(
                "mopl.batch.cleanup.retention.defaults.chunk-size is required");
        }
        if (defaults.retentionDays() == null || defaults.retentionDays() < 0) {
            throw new IllegalArgumentException(
                "mopl.batch.cleanup.retention.defaults.retention-days is required");
        }
    }

    public record PolicyProperties(
        Integer chunkSize,
        Long retentionDays
    ) {
    }
}
