package com.mopl.batch.collect.tsdb.config;

import com.mopl.batch.collect.tsdb.config.TsdbCollectProperties.PolicyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(TsdbCollectProperties.class)
@RequiredArgsConstructor
public class TsdbCollectPolicyResolver {

    private final TsdbCollectProperties props;

    public int sleepMs(PolicyProperties policy) {
        if (policy != null && policy.sleepMs() != null && policy.sleepMs() >= 0) {
            return policy.sleepMs();
        }
        return props.defaults().sleepMs();
    }
}
