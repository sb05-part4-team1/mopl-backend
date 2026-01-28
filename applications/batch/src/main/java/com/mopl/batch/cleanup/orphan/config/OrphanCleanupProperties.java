package com.mopl.batch.cleanup.orphan.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mopl.batch.cleanup.orphan")
public record OrphanCleanupProperties(
    PolicyProperties defaults,
    PolicyProperties conversation,           // 1
    PolicyProperties directMessage,          // 2
    PolicyProperties playlist,               // 3
    PolicyProperties playlistContent,        // 4
    PolicyProperties playlistSubscriber,     // 5
    PolicyProperties review,                 // 6
    PolicyProperties contentTag,             // 7
    PolicyProperties contentExternalMapping, // 8
    PolicyProperties notification,           // 9
    PolicyProperties follow,                 // 10
    PolicyProperties readStatus              // 11
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
