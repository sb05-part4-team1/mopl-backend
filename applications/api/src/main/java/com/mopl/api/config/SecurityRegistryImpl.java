package com.mopl.api.config;

import com.mopl.security.config.SecurityRegistry;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.stereotype.Component;

@Component
public class SecurityRegistryImpl implements SecurityRegistry {

    @Override
    public void configure(
        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth
            .requestMatchers(HttpMethod.GET, "/api/auth/csrf-token").permitAll()
            .requestMatchers(HttpMethod.POST,
                "/api/users",
                "/api/auth/sign-in",
                "/api/auth/reset-password",
                "/api/auth/refresh"
            ).permitAll()
            .requestMatchers("/api/files/display/**").permitAll()
            .requestMatchers("/oauth2/**").permitAll()
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
            .requestMatchers(
                "/actuator/health",
                "/actuator/info",
                "/actuator/prometheus",
                "/actuator/metrics",
                "/actuator/metrics/**"
            ).permitAll();

        auth
            .requestMatchers(HttpMethod.POST, "/api/contents").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PATCH, "/api/contents/{contentId}").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/contents/{contentId}").hasRole("ADMIN");

        auth
            .requestMatchers(new NegatedRequestMatcher(new AntPathRequestMatcher("/api/**"))).permitAll()
            .anyRequest().authenticated();
    }
}
