package com.mopl.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.util.Assert;

@ConfigurationProperties("mopl.admin")
public record AdminProperties(
    @DefaultValue("false") boolean enabled,
    String email,
    String name,
    String password
) {

    public AdminProperties {
        if (enabled) {
            Assert.hasText(email, "mopl.admin.email must not be empty when enabled is true");
            Assert.hasText(name, "mopl.admin.name must not be empty when enabled is true");
            Assert.hasText(password, "mopl.admin.password must not be empty when enabled is true");
        }
    }
}
