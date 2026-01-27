package com.mopl.batch.sync.es.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(EsSyncProperties.class)
@RequiredArgsConstructor
public class EsSyncPolicyResolver {

    private final EsSyncProperties props;

    public int chunkSize(EsSyncPolicyProperties policy) {
        int value = policy.chunkSize();
        if (value >= 1) {
            return value;
        }
        return props.getDefaults().chunkSize();
    }
}
