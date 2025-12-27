package com.mopl.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
    String secretKey,
    Duration accessTokenExpiration,
    Duration refreshTokenExpiration
) {

    public JwtProperties {
        if (secretKey == null || secretKey.length() < 32) {
            throw new IllegalArgumentException("JWT secret key must be at least 32 characters");
        }
        if (accessTokenExpiration == null) {
            accessTokenExpiration = Duration.ofMinutes(15);
        }
        if (refreshTokenExpiration == null) {
            refreshTokenExpiration = Duration.ofDays(7);
        }
    }
}
