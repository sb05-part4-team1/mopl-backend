package com.mopl.batch.cleanup.retention.config;

import com.mopl.batch.cleanup.retention.config.RetentionCleanupProperties.PolicyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(RetentionCleanupProperties.class)
@RequiredArgsConstructor
public class RetentionCleanupPolicyResolver {

    private final RetentionCleanupProperties props;

    public int chunkSize(PolicyProperties policy) {
        if (policy != null && policy.chunkSize() != null && policy.chunkSize() > 0) {
            return policy.chunkSize();
        }
        return props.defaults().chunkSize();
    }

    public long retentionDays(PolicyProperties policy) {
        if (policy != null && policy.retentionDays() != null && policy.retentionDays() >= 0) {
            return policy.retentionDays();
        }
        return props.defaults().retentionDays();
    }
}
