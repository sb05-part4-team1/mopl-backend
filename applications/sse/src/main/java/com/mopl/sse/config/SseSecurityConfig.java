package com.mopl.sse.config;

import com.mopl.security.config.SecurityRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;

@Configuration
public class SseSecurityConfig {

    @Bean
    public SecurityRegistry sseSecurityRegistry() {
        return auth -> auth
            .requestMatchers(new AntPathRequestMatcher("/swagger-ui/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/v3/api-docs/**")).permitAll()
            .requestMatchers(new NegatedRequestMatcher(
                new AntPathRequestMatcher("/api/**"))
            ).permitAll()
            .anyRequest().authenticated();
    }
}
