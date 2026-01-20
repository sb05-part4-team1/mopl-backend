package com.mopl.security.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("mopl.oauth2")
@Validated
public record OAuth2Properties(
    @NotBlank String frontendRedirectUri
) {
}
