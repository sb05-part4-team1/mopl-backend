package com.mopl.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "com.mopl.api",
    "com.mopl.sse",
    "com.mopl.jpa",
    "com.mopl.storage",
    "com.mopl.redis",
    "com.mopl.cache"
})
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}
