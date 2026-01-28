package com.mopl.batch.cleanup.orphan.config;

import com.mopl.batch.cleanup.orphan.config.OrphanCleanupProperties.PolicyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(OrphanCleanupProperties.class)
@RequiredArgsConstructor
public class OrphanCleanupPolicyResolver {

    private final OrphanCleanupProperties props;

    public int chunkSize(PolicyProperties policy) {
        if (policy != null && policy.chunkSize() != null && policy.chunkSize() > 0) {
            return policy.chunkSize();
        }
        return props.defaults().chunkSize();
    }

    public long gracePeriodDays(PolicyProperties policy) {
        if (policy != null && policy.gracePeriodDays() != null && policy.gracePeriodDays() >= 0) {
            return policy.gracePeriodDays();
        }
        return props.defaults().gracePeriodDays();
    }
}
