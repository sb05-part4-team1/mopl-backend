package com.mopl.batch.cleanup.softdelete.config;

import com.mopl.batch.cleanup.softdelete.config.SoftDeleteCleanupProperties.PolicyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(SoftDeleteCleanupProperties.class)
@RequiredArgsConstructor
public class SoftDeleteCleanupPolicyResolver {

    private final SoftDeleteCleanupProperties props;

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
