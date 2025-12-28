package com.mopl.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.security.filter.JwtAuthenticationFilter;
import com.mopl.security.handler.Http403ForbiddenAccessDeniedHandler;
import com.mopl.security.handler.JwtLoginSuccessHandler;
import com.mopl.security.handler.SpaCsrfTokenRequestHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@AutoConfiguration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SecurityRegistry.class)
    public SecurityRegistry defaultSecurityRegistry() {
        return auth -> auth.anyRequest().authenticated();
    }

    @Bean
    public SecurityFilterChain filterChain(
        HttpSecurity http,
        ObjectMapper objectMapper,
        SecurityRegistry securityRegistry,
        JwtLoginSuccessHandler jwtLoginSuccessHandler,
        LoginFailureHandler loginFailureHandler,
        JwtLogoutHandler jwtLogoutHandler,
        JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                .accessDeniedHandler(new Http403ForbiddenAccessDeniedHandler(objectMapper))
            )
            .authorizeHttpRequests(securityRegistry::configure)
            .formLogin(login -> login
                .loginProcessingUrl("/api/auth/login")
                .successHandler(jwtLoginSuccessHandler)
                .failureHandler(loginFailureHandler)
            )
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .addLogoutHandler(jwtLogoutHandler)
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(
                    HttpStatus.NO_CONTENT)
                )
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        // MDC Filter, ExceptionTranslationFilter, Rate Limiting Filters can be added here as needed

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
