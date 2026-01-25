package com.mopl.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "com.mopl.websocket",
    "com.mopl.jpa",
    "com.mopl.domain",
    "com.mopl.redis"
})
public class WebSocketApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebSocketApplication.class, args);
    }
}
