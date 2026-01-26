package com.mopl.redis.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(RedisConfig.class)
@ComponentScan(basePackages = {
    "com.mopl.redis.repository",
    "com.mopl.redis.pubsub"
})
public class RedisAutoConfig {
}
