package com.mopl.batch.collect.tsdb.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mopl.batch.collect.tsdb")
public record TsdbCollectProperties(
    PolicyProperties defaults,
    PolicyProperties leagueEvent,
    PolicyProperties leagueSync
) {
    public TsdbCollectProperties {
        if (defaults == null || defaults.sleepMs() == null || defaults.sleepMs() < 0) {
            throw new IllegalArgumentException(
                "mopl.batch.collect.tsdb.defaults.sleep-ms is required and must be >= 0");
        }
    }

    public record PolicyProperties(
        Integer sleepMs
    ) {
    }
}
