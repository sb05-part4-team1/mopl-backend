package com.mopl.batch.sync.es.properties;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EsSyncPolicyResolver {

    private final EsSyncProperties props;

    public int chunkSize(EsSyncPolicyProperties policy) {
        Integer value = policy.getChunkSize();
        if (value != null && value >= 1) {
            return value;
        }
        return props.getDefaults().getChunkSize();
    }
}
