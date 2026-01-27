package com.mopl.batch.collect.tmdb.config.properties;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mopl.batch.collect.tmdb")
@Getter
@Setter
public class TmdbCollectProperties {

    private TmdbCollectPolicyProperties defaults = new TmdbCollectPolicyProperties();

    private TmdbCollectPolicyProperties movieContent = new TmdbCollectPolicyProperties();
    private TmdbCollectPolicyProperties tvContent = new TmdbCollectPolicyProperties();

    @PostConstruct
    public void validate() {
        Integer maxPage = defaults.getMaxPage();

        if (maxPage == null || maxPage < 1) {
            throw new IllegalStateException(
                "mopl.batch.collect.tmdb.defaults.max-page is required");
        }
    }
}
