package com.mopl.batch.sync.denormalized.config;

import com.mopl.batch.sync.denormalized.config.DenormalizedSyncProperties.PolicyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(DenormalizedSyncProperties.class)
@RequiredArgsConstructor
public class DenormalizedSyncPolicyResolver {

    private final DenormalizedSyncProperties props;

    public int chunkSize(PolicyProperties policy) {
        if (policy != null && policy.chunkSize() != null && policy.chunkSize() > 0) {
            return policy.chunkSize();
        }
        return props.defaults().chunkSize();
    }
}
