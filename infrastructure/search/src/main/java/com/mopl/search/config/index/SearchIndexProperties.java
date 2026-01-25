package com.mopl.search.config.index;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "mopl.search.index")
public class SearchIndexProperties {

    private boolean recreateOnStartup = false;
}
