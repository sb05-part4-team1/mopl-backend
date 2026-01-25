package com.mopl.batch.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "mopl.batch")
public class BatchStartupProperties {

    private RunOnStartup runOnStartup = new RunOnStartup();

    @Getter
    @Setter
    public static class RunOnStartup {

        private boolean enabled = false;
        private List<String> jobs = List.of();
    }
}
