package com.mopl.batch.sync.es.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("mopl.batch.sync.es.defaults")
public record EsSyncPolicyProperties(
    int chunkSize
) {
}
