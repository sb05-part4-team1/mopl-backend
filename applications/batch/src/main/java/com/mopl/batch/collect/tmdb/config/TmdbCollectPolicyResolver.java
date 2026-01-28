package com.mopl.batch.collect.tmdb.config;

import com.mopl.batch.collect.tmdb.config.TmdbCollectProperties.PolicyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(TmdbCollectProperties.class)
@RequiredArgsConstructor
public class TmdbCollectPolicyResolver {

    private final TmdbCollectProperties props;

    public int maxPage(PolicyProperties policy) {
        if (policy != null && policy.maxPage() != null && policy.maxPage() >= 1) {
            return policy.maxPage();
        }
        return props.defaults().maxPage();
    }
}
