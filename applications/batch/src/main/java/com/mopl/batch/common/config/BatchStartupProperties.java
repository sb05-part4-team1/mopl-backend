package com.mopl.batch.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "mopl.batch")
@Getter
@Setter
public class BatchStartupProperties {

    private RunOnStartup runOnStartup = new RunOnStartup();

    @Getter
    @Setter
    public static class RunOnStartup {

        private boolean enabled = false;
        private List<String> jobs = List.of();
    }
}
