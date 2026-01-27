package com.mopl.batch.sync.denormalized.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "mopl.batch.sync.denormalized")
public record DenormalizedSyncProperties(
    DenormalizedSyncPolicyProperties defaultPolicy,
    Map<String, DenormalizedSyncPolicyProperties> profiles
) {
}
