package com.mopl.batch.collect.tsdb.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mopl.batch.collect.tsdb")
@Getter
@Setter
public class TsdbCollectProperties {

    private TsdbCollectPolicyProperties defaults = new TsdbCollectPolicyProperties();

    private TsdbCollectPolicyProperties leagueEvent = new TsdbCollectPolicyProperties();
    private TsdbCollectPolicyProperties leagueSync = new TsdbCollectPolicyProperties();

    @PostConstruct
    public void validate() {
        Integer sleepMs = defaults.getSleepMs();

        if (sleepMs == null || sleepMs < 0) {
            throw new IllegalStateException(
                "mopl.batch.collect.tsdb.defaults.sleep-ms is required and must be >= 0");
        }
    }
}
