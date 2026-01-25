package com.mopl.search.config;

import com.mopl.domain.support.search.ContentSearchSyncPort;
import com.mopl.search.content.sync.NoOpContentSearchSyncAdapter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
public class SearchAutoConfig {

    @Configuration
    @ConditionalOnProperty(prefix = "mopl.search", name = "enabled", havingValue = "true")
    @Import(ElasticsearchConfig.class)
    @ComponentScan(basePackages = "com.mopl.search.content")
    static class ElasticsearchEnabledConfig {
    }

    @Bean
    @ConditionalOnMissingBean(ContentSearchSyncPort.class)
    public ContentSearchSyncPort noOpContentSearchSyncPort() {
        return new NoOpContentSearchSyncAdapter();
    }
}
