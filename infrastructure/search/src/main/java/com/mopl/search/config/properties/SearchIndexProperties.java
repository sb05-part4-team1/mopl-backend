package com.mopl.search.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mopl.search.index")
@Getter
@Setter
public class SearchIndexProperties {

    private boolean recreateOnStartup = false;
}
