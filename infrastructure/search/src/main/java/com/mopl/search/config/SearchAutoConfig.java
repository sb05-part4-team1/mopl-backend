package com.mopl.search.config;

import com.mopl.domain.support.search.ContentSearchSyncPort;
import com.mopl.search.config.properties.ElasticsearchProperties;
import com.mopl.search.config.properties.SearchIndexProperties;
import com.mopl.search.intrastructure.search.NoOpContentSearchSyncAdapter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@EnableConfigurationProperties({
    SearchIndexProperties.class,
    ElasticsearchProperties.class
})
public class SearchAutoConfig {

    @AutoConfiguration
    @ConditionalOnProperty(prefix = "mopl.search", name = "enabled", havingValue = "true")
    @Import(ElasticsearchConfig.class)
    @ComponentScan(basePackages = {
        "com.mopl.search.content",
        "com.mopl.search.intrastructure"
    })
    static class ElasticsearchEnabledConfig {
    }

    @Bean
    @ConditionalOnMissingBean(ContentSearchSyncPort.class)
    public ContentSearchSyncPort noOpContentSearchSyncPort() {
        return new NoOpContentSearchSyncAdapter();
    }
}
