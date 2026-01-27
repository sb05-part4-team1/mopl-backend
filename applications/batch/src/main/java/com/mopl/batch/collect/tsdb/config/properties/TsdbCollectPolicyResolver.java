package com.mopl.batch.collect.tsdb.config.properties;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(TsdbCollectProperties.class)
@RequiredArgsConstructor
public class TsdbCollectPolicyResolver {

    private final TsdbCollectProperties props;

    public int sleepMs(TsdbCollectPolicyProperties policy) {
        Integer v = policy.getSleepMs();
        if (v != null && v >= 0) {
            return v;
        }
        return props.getDefaults().getSleepMs();
    }
}
