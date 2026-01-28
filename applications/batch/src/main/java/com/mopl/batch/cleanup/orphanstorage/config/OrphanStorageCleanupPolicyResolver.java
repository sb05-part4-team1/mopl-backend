package com.mopl.batch.cleanup.orphanstorage.config;

import com.mopl.batch.cleanup.orphanstorage.config.OrphanStorageCleanupProperties.PolicyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(OrphanStorageCleanupProperties.class)
@RequiredArgsConstructor
public class OrphanStorageCleanupPolicyResolver {

    private final OrphanStorageCleanupProperties props;

    public int chunkSize(PolicyProperties policy) {
        if (policy != null && policy.chunkSize() != null && policy.chunkSize() > 0) {
            return policy.chunkSize();
        }
        return props.defaults().chunkSize();
    }
}
