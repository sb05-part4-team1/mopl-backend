package com.mopl.jpa.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({JpaConfig.class, QuerydslConfig.class})
@ComponentScan(basePackages = {
    "com.mopl.jpa.repository",
    "com.mopl.jpa.entity",
    "com.mopl.jpa.support",
    "com.mopl.jpa.infrastructure.popularity"
})
public class JpaAutoConfig {
}
