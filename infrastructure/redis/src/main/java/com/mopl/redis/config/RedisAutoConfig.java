package com.mopl.redis.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@EnableConfigurationProperties(RedisProperties.class)
@ComponentScan(basePackages = {
    "com.mopl.redis.repository",
    "com.mopl.redis.pubsub"
})
@Import(RedisConfig.class)
public class RedisAutoConfig {
}
