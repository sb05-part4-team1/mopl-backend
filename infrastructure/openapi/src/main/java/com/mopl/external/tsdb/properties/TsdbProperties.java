package com.mopl.external.tsdb.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "tsdb")
public class TsdbProperties {

    private Api api;
    private Image image;

    @Getter
    @Setter
    public static class Api {

        private String baseUrl;
        private String apiKey;
    }

    @Getter
    @Setter
    public static class Image {

        private String defaultSize;
    }
}
