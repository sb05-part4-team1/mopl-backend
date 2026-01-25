package com.mopl.search.config.index;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SearchIndexProperties.class)
@ConditionalOnProperty(prefix = "mopl.search", name = "enabled", havingValue = "true")
public class SearchIndexConfig {
}
