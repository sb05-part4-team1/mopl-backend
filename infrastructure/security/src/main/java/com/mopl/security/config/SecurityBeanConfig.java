package com.mopl.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.domain.repository.user.TemporaryPasswordRepository;
import com.mopl.domain.repository.user.UserRepository;
import com.mopl.domain.service.user.UserService;
import com.mopl.security.authentication.TemporaryPasswordAuthenticationProvider;
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
import com.mopl.security.jwt.registry.RedisJwtRegistry;
import com.mopl.security.jwt.service.TokenRefreshService;
import com.mopl.security.oauth2.CustomOAuth2UserService;
import com.mopl.security.oauth2.handler.OAuth2FailureHandler;
import com.mopl.security.oauth2.handler.OAuth2SuccessHandler;
import com.mopl.security.userdetails.MoplUserDetailsService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    @ConditionalOnProperty(
        name = "mopl.jwt.registry-type",
        havingValue = "redis"
    )
    @ConditionalOnMissingBean(JwtRegistry.class)
    public JwtRegistry redisJwtRegistry(
        RedisTemplate<String, Object> redisTemplate,
        JwtProperties jwtProperties
    ) {
        return new RedisJwtRegistry(redisTemplate, jwtProperties);
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

    @Bean
    public TokenRefreshService tokenRefreshService(
        JwtProvider jwtProvider,
        JwtCookieProvider jwtCookieProvider,
        JwtRegistry jwtRegistry,
        UserService userService
    ) {
        return new TokenRefreshService(jwtProvider, jwtCookieProvider, jwtRegistry, userService);
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
        UserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder,
        TemporaryPasswordRepository temporaryPasswordRepository
    ) {
        TemporaryPasswordAuthenticationProvider provider = new TemporaryPasswordAuthenticationProvider(
            temporaryPasswordRepository, passwordEncoder);
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public CustomOAuth2UserService customOAuth2UserService(
        UserRepository userRepository,
        UserService userService
    ) {
        return new CustomOAuth2UserService(userRepository, userService);
    }

    @Bean
    public OAuth2SuccessHandler oAuth2SuccessHandler(
        OAuth2Properties oAuth2Properties,
        JwtProvider jwtProvider,
        JwtCookieProvider jwtCookieProvider,
        JwtRegistry jwtRegistry
    ) {
        return new OAuth2SuccessHandler(
            oAuth2Properties,
            jwtProvider,
            jwtCookieProvider,
            jwtRegistry
        );
    }

    @Bean
    public OAuth2FailureHandler oAuth2FailureHandler(OAuth2Properties oAuth2Properties) {
        return new OAuth2FailureHandler(oAuth2Properties);
    }
}
