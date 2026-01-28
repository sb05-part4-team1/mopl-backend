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
@EnableElasticsearchRepositories(basePackages = "com.mopl.search.content.repository")
@EnableConfigurationProperties(ElasticsearchProperties.class)
@RequiredArgsConstructor
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    private final ElasticsearchProperties props;
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final ObjectMapper objectMapper;

    @Override
    @NonNull
    public ClientConfiguration clientConfiguration() {
        String uri = props.getUris();
        boolean useSsl = uri.startsWith("https://");

        // 프로토콜 제거 후 host:port 추출
        String hostAndPort = uri.replaceFirst("^https?://", "");

        // 포트가 없으면 HTTPS는 443, HTTP는 9200
        if (!hostAndPort.contains(":")) {
            hostAndPort += ":" + (useSsl ? 443 : 9200);
        }

        ClientConfiguration.MaybeSecureClientConfigurationBuilder connectedBuilder = ClientConfiguration.builder().connectedTo(hostAndPort);

        ClientConfiguration.TerminalClientConfigurationBuilder builder = useSsl
            ? connectedBuilder.usingSsl()
            : connectedBuilder;

        builder = builder
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
