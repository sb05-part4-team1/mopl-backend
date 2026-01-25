package com.mopl.search.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@ConditionalOnProperty(
    prefix = "mopl.search",
    name = "enabled",
    havingValue = "true"
)
@EnableElasticsearchRepositories(
    basePackages = "com.mopl.search.content.repository"
)
public class ElasticsearchRepositoryConfig {
}
