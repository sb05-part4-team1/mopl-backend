package com.mopl.batch.cleanup.retention.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(RetentionCleanupProperties.class)
@RequiredArgsConstructor
public class RetentionCleanupPolicyResolver {

    private final RetentionCleanupProperties cleanupProperties;

    public int chunkSize(RetentionCleanupPolicyProperties policy) {
        Integer value = policy.getChunkSize();
        if (value != null && value > 0) {
            return value;
        }
        return cleanupProperties.getDefaults().getChunkSize();
    }

    public long retentionDaysRequired(RetentionCleanupPolicyProperties policy) {
        Long value = policy.getRetentionDays();
        if (value != null && value >= 0) {
            return value;
        }
        return cleanupProperties.getDefaults().getRetentionDays();
    }
}
