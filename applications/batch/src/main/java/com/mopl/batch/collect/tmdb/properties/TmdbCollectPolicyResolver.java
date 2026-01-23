package com.mopl.batch.collect.tmdb.properties;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
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
