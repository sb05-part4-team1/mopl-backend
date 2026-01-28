package com.mopl.batch.collect.tmdb.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mopl.batch.collect.tmdb")
public record TmdbCollectProperties(
    PolicyProperties defaults,
    PolicyProperties movieContent,
    PolicyProperties tvContent
) {

    public TmdbCollectProperties {
        if (defaults == null || defaults.maxPage() == null || defaults.maxPage() < 1) {
            throw new IllegalArgumentException(
                "mopl.batch.collect.tmdb.defaults.max-page is required");
        }
    }

    public record PolicyProperties(
        Integer maxPage
    ) {
    }
}
