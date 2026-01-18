package com.mopl.external.tsdb.config;

import com.mopl.external.tsdb.properties.TsdbProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(TsdbProperties.class)
public class TsdbWebClientConfig {

    @Bean
    public WebClient tsdbWebClient(TsdbProperties props) {
        return WebClient.builder()
            .baseUrl(props.getApi().getBaseUrl() + "/" + props.getApi().getApiKey())
            .build();
    }
}
