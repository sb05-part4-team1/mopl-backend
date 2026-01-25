package com.mopl.search.config;

import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.lang.NonNull;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(ElasticsearchProperties.class)
@EnableElasticsearchRepositories(basePackages = "com.mopl.search.content.repository")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    private final ElasticsearchProperties props;

    @Override
    @NonNull
    public ClientConfiguration clientConfiguration() {
        ClientConfiguration.TerminalClientConfigurationBuilder builder = ClientConfiguration.builder()
            .connectedTo(props.getUris())
            .withConnectTimeout(props.getConnectTimeoutMillis())
            .withSocketTimeout(props.getSocketTimeoutMillis());

        if (props.getUsername() != null && !props.getUsername().isBlank()) {
            builder = builder.withBasicAuth(props.getUsername(), props.getPassword());
        }

        return builder.build();
    }

    @Override
    @NonNull
    public JsonpMapper jsonpMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return new JacksonJsonpMapper(mapper);
    }
}
