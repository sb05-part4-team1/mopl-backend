package com.mopl.kafka.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(KafkaConfig.class)
public class KafkaAutoConfig {
}
