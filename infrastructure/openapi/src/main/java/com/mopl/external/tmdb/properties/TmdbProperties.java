package com.mopl.external.tmdb.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "tmdb")
public class TmdbProperties {

    private Api api;
    private Image image;

    @Getter
    @Setter
    public static class Api {

        private String baseUrl;
        private String accessToken;
        private String apiKey;
    }

    @Getter
    @Setter
    public static class Image {

        private String baseUrl;
        private String defaultSize;
    }
}
