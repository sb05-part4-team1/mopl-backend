package com.mopl.search.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "mopl.search.elasticsearch")
public class ElasticsearchProperties {

    private String uris;

    private String username;
    private String password;

    private int connectTimeoutMillis = 1000;
    private int socketTimeoutMillis = 3000;
}
