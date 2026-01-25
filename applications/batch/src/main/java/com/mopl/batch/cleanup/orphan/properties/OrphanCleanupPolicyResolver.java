package com.mopl.batch.cleanup.orphan.properties;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrphanCleanupPolicyResolver {

    private final OrphanCleanupProperties orphanCleanupProperties;

    public int chunkSize(OrphanCleanupPolicyProperties policy) {
        Integer value = policy.getChunkSize();
        if (value != null && value > 0) {
            return value;
        }
        return orphanCleanupProperties.getDefaults().getChunkSize();
    }

    public long gracePeriodDays(OrphanCleanupPolicyProperties policy) {
        Long value = policy.getGracePeriodDays();
        if (value != null && value >= 0) {
            return value;
        }
        return orphanCleanupProperties.getDefaults().getGracePeriodDays();
    }
}
