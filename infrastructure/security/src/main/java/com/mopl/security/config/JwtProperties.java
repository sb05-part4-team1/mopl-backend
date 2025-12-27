package com.mopl.security.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

import static org.springframework.util.StringUtils.hasText;

@ConfigurationProperties("mopl.jwt")
@Validated
public record JwtProperties(
    @NotNull @Valid AccessToken accessToken,
    @NotNull @Valid RefreshToken refreshToken,
    @Positive int maxSessions,
    @NotNull JwtRegistryType registryType,
    @NotBlank String refreshTokenCookieName
) {

    public record AccessToken(
        @NotBlank String secret,
        @NotNull Duration expiration,
        String previousSecret
    ) {
        public boolean hasPreviousSecret() {
            return hasText(previousSecret);
        }
    }

    public record RefreshToken(
        @NotBlank String secret,
        @NotNull Duration expiration,
        String previousSecret
    ) {
        public boolean hasPreviousSecret() {
            return hasText(previousSecret);
        }
    }

    public enum JwtRegistryType {
        IN_MEMORY, REDIS
    }
}
