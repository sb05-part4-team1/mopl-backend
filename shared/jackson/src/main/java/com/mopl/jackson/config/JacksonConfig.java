package com.mopl.jackson.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration
class JacksonConfig {

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            builder.findModulesViaServiceLoader(true);

            builder.serializationInclusion(JsonInclude.Include.NON_NULL);
            builder.featuresToEnable(
                JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT,
                JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN
            );
            builder.featuresToDisable(
                SerializationFeature.FAIL_ON_EMPTY_BEANS,
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
            );

            builder.featuresToEnable(
                DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,
                DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
                DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES
            );
            builder.featuresToDisable(
                DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL,
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
            );
        };
    }
}
