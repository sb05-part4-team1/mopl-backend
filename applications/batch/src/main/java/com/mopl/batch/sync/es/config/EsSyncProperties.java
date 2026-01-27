package com.mopl.batch.sync.es.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mopl.batch.sync.es")
public record EsSyncProperties(
    PolicyProperties defaults,
    PolicyProperties content
) {

    public EsSyncProperties {
        if (defaults == null || defaults.chunkSize() == null || defaults.chunkSize() <= 0) {
            throw new IllegalArgumentException(
                "mopl.batch.sync.es.defaults.chunk-size is required");
        }
    }

    public record PolicyProperties(
        Integer chunkSize
    ) {
    }
}
