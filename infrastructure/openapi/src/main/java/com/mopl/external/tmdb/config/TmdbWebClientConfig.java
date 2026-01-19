package com.mopl.external.tmdb.config;

import com.mopl.external.tmdb.properties.TmdbProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(TmdbProperties.class)
public class TmdbWebClientConfig {

    @Bean
    public WebClient tmdbWebClient(TmdbProperties props) {
        return WebClient.builder()
            .baseUrl(props.getApi().getBaseUrl())
            .defaultHeader(
                "Authorization",
                "Bearer " + props.getApi().getAccessToken()
            )
            .build();
    }

    @Bean
    public WebClient tmdbImageClient(TmdbProperties props) {
        return WebClient.builder()
            .baseUrl(props.getImage().getBaseUrl())
            .build();
    }
}
