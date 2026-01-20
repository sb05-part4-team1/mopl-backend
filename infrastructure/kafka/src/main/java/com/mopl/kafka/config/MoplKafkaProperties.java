package com.mopl.kafka.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mopl.kafka")
public record MoplKafkaProperties(
    Topics topics
) {

    public record Topics(
        String notification,
        String user,
        String content,
        String analytics
    ) {
    }
}
