package com.mopl.batch.cleanup.orphan.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mopl.batch.cleanup.orphan")
public record OrphanCleanupProperties(
    PolicyProperties defaults,
    PolicyProperties notification,
    PolicyProperties follow,
    PolicyProperties playlistSubscriber,
    PolicyProperties playlistContent,
    PolicyProperties playlist,
    PolicyProperties review,
    PolicyProperties readStatus,
    PolicyProperties directMessage,
    PolicyProperties conversation,
    PolicyProperties contentTag,
    PolicyProperties contentExternalMapping
) {
    public OrphanCleanupProperties {
        if (defaults == null || defaults.chunkSize() == null || defaults.chunkSize() <= 0) {
            throw new IllegalArgumentException(
                "mopl.batch.cleanup.orphan.defaults.chunk-size is required");
        }
        if (defaults.gracePeriodDays() == null || defaults.gracePeriodDays() < 0) {
            throw new IllegalArgumentException(
                "mopl.batch.cleanup.orphan.defaults.grace-period-days is required");
        }
    }

    public record PolicyProperties(
        Integer chunkSize,
        Long gracePeriodDays
    ) {
    }
}
