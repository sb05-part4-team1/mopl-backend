package com.mopl.batch.cleanup.orphanstorage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mopl.batch.cleanup.orphan-storage")
public record OrphanStorageCleanupProperties(
    PolicyProperties defaults,
    PolicyProperties contentThumbnail,  // 1
    PolicyProperties userProfileImage   // 2
) {

    public OrphanStorageCleanupProperties {
        if (defaults == null || defaults.chunkSize() == null || defaults.chunkSize() <= 0) {
            throw new IllegalArgumentException(
                "mopl.batch.cleanup.orphan-storage.defaults.chunk-size is required");
        }
    }

    public record PolicyProperties(
        Integer chunkSize
    ) {
    }
}
