package com.mopl.kafka.dlq;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DlqConfig {

    @Bean
    @ConditionalOnMissingBean(DlqAlertPublisher.class)
    public DlqAlertPublisher dlqAlertPublisher() {
        return new LoggingDlqAlertPublisher();
    }
}
