package com.mopl.kafka.config;

import com.mopl.kafka.dlq.DlqConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({KafkaConfig.class, DlqConfig.class})
public class KafkaAutoConfig {
}
