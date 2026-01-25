package com.mopl.cache.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(CacheConfig.class)
public class CacheAutoConfig {
}
