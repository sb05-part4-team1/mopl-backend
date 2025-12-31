package com.mopl.security.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@ConfigurationProperties("mopl.jwt")
@Validated
public record JwtProperties(
    @NotNull @Valid Config accessToken,
    @NotNull @Valid Config refreshToken,
    @Positive int maxSessions,
    @NotNull JwtRegistryType registryType,
    @NotBlank String refreshTokenCookieName
) {

    public record Config(
        @NotBlank String secret,
        @NotNull Duration expiration,
        String previousSecret
    ) {
    }

    public enum JwtRegistryType {
        IN_MEMORY, REDIS
    }
}
