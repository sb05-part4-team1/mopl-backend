package com.mopl.batch.cleanup.config.properties;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(CleanupProperties.class)
@RequiredArgsConstructor
public class CleanupPolicyResolver {

    private final CleanupProperties cleanupProperties;

    public int chunkSize(CleanupPolicyProperties policy) {
        Integer value = policy.getChunkSize();
        if (value != null && value > 0) {
            return value;
        }
        return cleanupProperties.getDefaults().getChunkSize();
    }

    public long retentionDaysRequired(CleanupPolicyProperties policy) {
        Long value = policy.getRetentionDays();
        if (value != null && value >= 0) {
            return value;
        }
        return cleanupProperties.getDefaults().getRetentionDays();
    }
}
