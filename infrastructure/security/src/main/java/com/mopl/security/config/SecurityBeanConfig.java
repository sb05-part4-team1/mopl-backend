package com.mopl.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.domain.service.user.UserService;
import com.mopl.security.authentication.handler.SignInFailureHandler;
import com.mopl.security.authentication.handler.SignInSuccessHandler;
import com.mopl.security.authentication.handler.SignOutHandler;
import com.mopl.security.csrf.SpaCsrfTokenRequestHandler;
import com.mopl.security.exception.AccessDeniedExceptionHandler;
import com.mopl.security.exception.ApiResponseHandler;
import com.mopl.security.exception.UnauthorizedEntryPoint;
import com.mopl.security.jwt.filter.JwtAuthenticationFilter;
import com.mopl.security.jwt.provider.JwtCookieProvider;
import com.mopl.security.jwt.provider.JwtProvider;
import com.mopl.security.jwt.registry.InMemoryJwtRegistry;
import com.mopl.security.jwt.registry.JwtRegistry;
import com.mopl.security.userdetails.MoplUserDetailsService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
public class SecurityBeanConfig {

    @Bean
    public ApiResponseHandler apiResponseHandler(ObjectMapper objectMapper) {
        return new ApiResponseHandler(objectMapper);
    }

    @Bean
    public SpaCsrfTokenRequestHandler spaCsrfTokenRequestHandler() {
        return new SpaCsrfTokenRequestHandler();
    }

    @Bean
    public UserDetailsService userDetailsService(UserService userService) {
        return new MoplUserDetailsService(userService);
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
    public UnauthorizedEntryPoint unauthorizedEntryPoint(
        ApiResponseHandler apiResponseHandler
    ) {
        return new UnauthorizedEntryPoint(apiResponseHandler);
    }

    @Bean
    public AccessDeniedExceptionHandler accessDeniedExceptionHandler(
        ApiResponseHandler apiResponseHandler
    ) {
        return new AccessDeniedExceptionHandler(apiResponseHandler);
    }

    @Bean
    public SignInFailureHandler signInFailureHandler(ApiResponseHandler apiResponseHandler) {
        return new SignInFailureHandler(apiResponseHandler);
    }

    @Bean
    public SignInSuccessHandler signInSuccessHandler(
        JwtProvider jwtProvider,
        JwtCookieProvider jwtCookieProvider,
        ApiResponseHandler apiResponseHandler,
        JwtRegistry jwtRegistry
    ) {
        return new SignInSuccessHandler(jwtProvider, jwtCookieProvider, jwtRegistry,
            apiResponseHandler);
    }

    @Bean
    public SignOutHandler signOutHandler(
        JwtProvider jwtProvider,
        JwtCookieProvider jwtCookieProvider,
        JwtRegistry jwtRegistry,
        JwtProperties jwtProperties
    ) {
        return new SignOutHandler(jwtProvider, jwtCookieProvider, jwtRegistry, jwtProperties);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
        JwtProvider jwtProvider,
        JwtRegistry jwtRegistry,
        ApiResponseHandler apiResponseHandler
    ) {
        return new JwtAuthenticationFilter(jwtProvider, jwtRegistry, apiResponseHandler);
    }
}
