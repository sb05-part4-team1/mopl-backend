package com.mopl.batch.sync.es.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mopl.batch.sync.es")
@Getter
@Setter
public class EsSyncProperties {

    private EsSyncPolicyProperties defaults = new EsSyncPolicyProperties();
    private EsSyncPolicyProperties content = new EsSyncPolicyProperties();

    @PostConstruct
    public void validate() {
        Integer chunkSize = defaults.chunkSize();

        if (chunkSize == null || chunkSize < 1) {
            throw new IllegalStateException("mopl.batch.sync.es.defaults.chunk-size is required (>= 1)");
        }
    }
}
