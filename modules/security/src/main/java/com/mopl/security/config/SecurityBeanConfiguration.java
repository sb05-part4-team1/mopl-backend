package com.mopl.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.domain.service.user.UserService;
import com.mopl.security.filter.jwt.JwtAuthenticationFilter;
import com.mopl.security.handler.ApiResponseHandler;
import com.mopl.security.handler.Http401UnauthorizedEntryPoint;
import com.mopl.security.handler.Http403ForbiddenAccessDeniedHandler;
import com.mopl.security.handler.LoginFailureHandler;
import com.mopl.security.handler.SpaCsrfTokenRequestHandler;
import com.mopl.security.handler.jwt.JwtLoginSuccessHandler;
import com.mopl.security.handler.jwt.JwtLogoutHandler;
import com.mopl.security.provider.jwt.JwtCookieProvider;
import com.mopl.security.provider.jwt.JwtProvider;
import com.mopl.security.provider.jwt.MoplUserDetailsService;
import com.mopl.security.provider.jwt.registry.InMemoryJwtRegistry;
import com.mopl.security.provider.jwt.registry.JwtRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
public class SecurityBeanConfiguration {

    @Bean
    public ApiResponseHandler apiResponseHandler(ObjectMapper objectMapper) {
        return new ApiResponseHandler(objectMapper);
    }

    @Bean
    public SpaCsrfTokenRequestHandler spaCsrfTokenRequestHandler() {
        return new SpaCsrfTokenRequestHandler();
    }

    @Bean
    public Http401UnauthorizedEntryPoint http401UnauthorizedEntryPoint(
        ApiResponseHandler apiResponseHandler
    ) {
        return new Http401UnauthorizedEntryPoint(apiResponseHandler);
    }

    @Bean
    public Http403ForbiddenAccessDeniedHandler http403ForbiddenAccessDeniedHandler(
        ApiResponseHandler apiResponseHandler
    ) {
        return new Http403ForbiddenAccessDeniedHandler(apiResponseHandler);
    }

    @Bean
    public LoginFailureHandler loginFailureHandler(ApiResponseHandler apiResponseHandler) {
        return new LoginFailureHandler(apiResponseHandler);
    }

    @Bean
    public JwtProvider jwtProvider(JwtProperties jwtProperties) {
        return new JwtProvider(jwtProperties);
    }

    @Bean
    public JwtCookieProvider jwtCookieProvider(JwtProperties jwtProperties) {
        return new JwtCookieProvider(jwtProperties);
    }

    @Bean
    @ConditionalOnProperty(
        name = "mopl.jwt.registry-type",
        havingValue = "in-memory",
        matchIfMissing = true
    )
    @ConditionalOnMissingBean(JwtRegistry.class)
    public JwtRegistry inMemoryJwtRegistry(JwtProperties jwtProperties) {
        return new InMemoryJwtRegistry(jwtProperties);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
        JwtProvider jwtProvider,
        JwtRegistry jwtRegistry,
        ApiResponseHandler apiResponseHandler
    ) {
        return new JwtAuthenticationFilter(jwtProvider, jwtRegistry, apiResponseHandler);
    }

    @Bean
    public JwtLogoutHandler jwtLogoutHandler(
        JwtProvider jwtProvider,
        JwtCookieProvider jwtCookieProvider,
        JwtRegistry jwtRegistry,
        JwtProperties jwtProperties
    ) {
        return new JwtLogoutHandler(jwtProvider, jwtCookieProvider, jwtRegistry, jwtProperties);
    }

    @Bean
    public JwtLoginSuccessHandler jwtLoginSuccessHandler(
        JwtProvider jwtProvider,
        JwtCookieProvider jwtCookieProvider,
        ApiResponseHandler apiResponseHandler,
        JwtRegistry jwtRegistry
    ) {
        return new JwtLoginSuccessHandler(jwtProvider, jwtCookieProvider, jwtRegistry,
            apiResponseHandler);
    }

    @Bean
    public UserDetailsService userDetailsService(UserService userService) {
        return new MoplUserDetailsService(userService);
    }
}
