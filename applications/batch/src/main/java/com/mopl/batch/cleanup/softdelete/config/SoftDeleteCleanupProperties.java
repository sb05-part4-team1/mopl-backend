package com.mopl.batch.cleanup.softdelete.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mopl.batch.cleanup.soft-delete")
public record SoftDeleteCleanupProperties(
    PolicyProperties defaults,
    PolicyProperties content,
    PolicyProperties storage,
    PolicyProperties deletionLog
) {

    public SoftDeleteCleanupProperties {
        if (defaults == null || defaults.chunkSize() == null || defaults.chunkSize() <= 0) {
            throw new IllegalArgumentException(
                "mopl.batch.cleanup.soft-delete.defaults.chunk-size is required");
        }
        if (defaults.retentionDays() == null || defaults.retentionDays() < 0) {
            throw new IllegalArgumentException(
                "mopl.batch.cleanup.soft-delete.defaults.retention-days is required");
        }
    }

    public record PolicyProperties(
        Integer chunkSize,
        Long retentionDays
    ) {
    }
}
