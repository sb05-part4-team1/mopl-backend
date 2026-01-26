package com.mopl.search.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "mopl.search.elasticsearch")
public class ElasticsearchProperties {

    private String uris;

    private String username;
    private String password;

    private Duration connectTimeout = Duration.ofSeconds(1);
    private Duration socketTimeout = Duration.ofSeconds(3);
}
