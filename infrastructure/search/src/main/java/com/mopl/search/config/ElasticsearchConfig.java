package com.mopl.search.config;

import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.search.config.properties.ElasticsearchProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.lang.NonNull;

@Configuration
@EnableConfigurationProperties(ElasticsearchProperties.class)
@EnableElasticsearchRepositories(basePackages = "com.mopl.search.content.repository")
@RequiredArgsConstructor
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    private final ElasticsearchProperties props;
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final ObjectMapper objectMapper;

    @Override
    @NonNull
    public ClientConfiguration clientConfiguration() {
        ClientConfiguration.TerminalClientConfigurationBuilder builder = ClientConfiguration.builder()
            .connectedTo(props.getUris())
            .withConnectTimeout(props.getConnectTimeout())
            .withSocketTimeout(props.getSocketTimeout());

        if (props.getUsername() != null && !props.getUsername().isBlank()) {
            builder = builder.withBasicAuth(props.getUsername(), props.getPassword());
        }

        return builder.build();
    }

    @Override
    @NonNull
    public JsonpMapper jsonpMapper() {
        return new JacksonJsonpMapper(objectMapper.copy());
    }
}
