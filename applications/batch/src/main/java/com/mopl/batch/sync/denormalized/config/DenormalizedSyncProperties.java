package com.mopl.batch.sync.denormalized.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mopl.batch.sync.denormalized")
public record DenormalizedSyncProperties(
    PolicyProperties defaults,
    PolicyProperties contentReviewStats,  // 1
    PolicyProperties playlistSubscriberCount  // 2
) {
    public DenormalizedSyncProperties {
        if (defaults == null || defaults.chunkSize() == null || defaults.chunkSize() <= 0) {
            throw new IllegalArgumentException(
                "mopl.batch.sync.denormalized.defaults.chunk-size is required");
        }
    }

    public record PolicyProperties(
        Integer chunkSize
    ) {
    }
}
