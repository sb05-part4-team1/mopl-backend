package com.mopl.websocket.config;

import com.mopl.security.config.SecurityRegistry;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class SecurityRegistryImpl implements SecurityRegistry {

    @Override
    public void configure(
        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth
    ) {
        auth
            .requestMatchers("/ws/**").permitAll()
            .requestMatchers("/actuator/health").permitAll()
            .anyRequest().denyAll();
    }
}
