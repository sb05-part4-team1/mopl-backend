package com.mopl.shared.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class MonitoringAutoConfiguration {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> commonTagsCustomizer(
        @Value("${spring.application.name:unknown}") String applicationName,
        @Value("${spring.profiles.active:local}") String activeProfile
    ) {
        return registry -> registry.config().commonTags(
            "application", applicationName,
            "environment", activeProfile
        );
    }
}
