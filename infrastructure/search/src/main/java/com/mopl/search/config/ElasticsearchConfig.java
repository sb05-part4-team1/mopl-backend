package com.mopl.search.config;

import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mopl.search.config.properties.ElasticsearchProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.lang.NonNull;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.mopl.search.content.repository")
@RequiredArgsConstructor
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    private final ElasticsearchProperties props;
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
        ObjectMapper copy = objectMapper.copy();
        copy.registerModule(new JavaTimeModule());
        copy.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return new JacksonJsonpMapper(copy);
    }
}
