package com.mopl.batch.collect.tmdb.config.properties;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(TmdbCollectProperties.class)
@RequiredArgsConstructor
public class TmdbCollectPolicyResolver {

    private final TmdbCollectProperties tmdbCollectProperties;

    public int maxPage(TmdbCollectPolicyProperties policy) {
        Integer value = policy.getMaxPage();
        if (value != null && value >= 1) {
            return value;
        }
        return tmdbCollectProperties.getDefaults().getMaxPage();
    }
}
