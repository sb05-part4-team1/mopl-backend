package com.mopl.batch.sync.es.config;

import com.mopl.batch.sync.es.config.EsSyncProperties.PolicyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(EsSyncProperties.class)
@RequiredArgsConstructor
public class EsSyncPolicyResolver {

    private final EsSyncProperties props;

    public int chunkSize(PolicyProperties policy) {
        if (policy != null && policy.chunkSize() != null && policy.chunkSize() > 0) {
            return policy.chunkSize();
        }
        return props.defaults().chunkSize();
    }
}
