package com.mopl.api.config;

import com.mopl.security.config.SecurityRegistry;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class SecurityRegistryImpl implements SecurityRegistry {

    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth
            .requestMatchers(HttpMethod.GET, "/api/auth/csrf-token").permitAll()
            .requestMatchers(HttpMethod.POST,
                "/api/users",
                "/api/auth/sign-in",
                "/api/auth/reset-password"
            ).permitAll()
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
            .anyRequest().authenticated();
    }
}
