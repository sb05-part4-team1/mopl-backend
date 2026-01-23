package com.mopl.sse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
    "com.mopl.sse",
    "com.mopl.jpa",
    "com.mopl.redis",
    "com.mopl.security"
})
@EnableScheduling
public class SseApplication {

    public static void main(String[] args) {
        SpringApplication.run(SseApplication.class, args);
    }
}
