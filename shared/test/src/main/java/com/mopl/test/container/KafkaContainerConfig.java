package com.mopl.test.container;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class KafkaContainerConfig {

    private static final String KAFKA_IMAGE = "apache/kafka:3.8.0";

    @Bean(destroyMethod = "stop")
    @ServiceConnection
    @SuppressWarnings("resource") // Spring 컨테이너가 생명주기 관리
    KafkaContainer kafkaContainer() {
        return new KafkaContainer(DockerImageName.parse(KAFKA_IMAGE))
            .withReuse(true);
    }
}
