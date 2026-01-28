package com.mopl.test.container;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class ElasticsearchContainerConfig {

    private static final String ELASTICSEARCH_IMAGE = "elasticsearch:8.15.0";

    @Bean(destroyMethod = "stop")
    @ServiceConnection
    @SuppressWarnings("resource") // Spring 컨테이너가 생명주기 관리
    ElasticsearchContainer elasticsearchContainer() {
        return new ElasticsearchContainer(DockerImageName.parse(ELASTICSEARCH_IMAGE))
            .withEnv("discovery.type", "single-node")
            .withEnv("xpack.security.enabled", "false")
            .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m")
            .withReuse(true);
    }
}
