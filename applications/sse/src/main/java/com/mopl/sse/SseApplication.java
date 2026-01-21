package com.mopl.sse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "com.mopl.sse",
    "com.mopl.jpa",
    "com.mopl.redis",
    "com.mopl.security"
})
public class SseApplication {

    public static void main(String[] args) {
        SpringApplication.run(SseApplication.class, args);
    }
}
