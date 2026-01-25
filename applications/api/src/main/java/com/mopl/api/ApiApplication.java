package com.mopl.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "com.mopl.api",
    "com.mopl.cache",
    "com.mopl.jpa",
    "com.mopl.mail",
    "com.mopl.redis"
})
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}
