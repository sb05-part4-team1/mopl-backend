package com.mopl.security.config;

import com.mopl.domain.model.user.UserModel;
import com.mopl.security.authentication.handler.SignInFailureHandler;
import com.mopl.security.authentication.handler.SignInSuccessHandler;
import com.mopl.security.authentication.handler.SignOutHandler;
import com.mopl.security.csrf.SpaCsrfTokenRequestHandler;
import com.mopl.security.exception.AccessDeniedExceptionHandler;
import com.mopl.security.exception.UnauthorizedEntryPoint;
import com.mopl.security.jwt.filter.JwtAuthenticationFilter;
import com.mopl.security.oauth2.CustomOAuth2UserService;
import com.mopl.security.oauth2.handler.OAuth2FailureHandler;
import com.mopl.security.oauth2.handler.OAuth2SuccessHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@AutoConfiguration
@EnableWebSecurity
@EnableMethodSecurity
@EnableScheduling
@EnableConfigurationProperties({JwtProperties.class, OAuth2Properties.class})
@Import(SecurityBeanConfig.class)
public class SecurityAutoConfig {

    @Bean
    @ConditionalOnMissingBean(SecurityRegistry.class)
    public SecurityRegistry defaultSecurityRegistry() {
        return auth -> auth
            .requestMatchers(
                new AntPathRequestMatcher("h2-console/**")
            ).permitAll()
            .anyRequest().authenticated();
    }

    @Bean
    public SecurityFilterChain filterChain(
        HttpSecurity http,
        SecurityRegistry securityRegistry,
        SpaCsrfTokenRequestHandler spaCsrfTokenRequestHandler,
        UnauthorizedEntryPoint unauthorizedEntryPoint,
        AccessDeniedExceptionHandler accessDeniedHandler,
        SignInSuccessHandler signInSuccessHandler,
        SignInFailureHandler signInFailureHandler,
        SignOutHandler signOutHandler,
        JwtAuthenticationFilter jwtAuthenticationFilter,
        CustomOAuth2UserService customOAuth2UserService,
        OAuth2SuccessHandler oAuth2SuccessHandler,
        OAuth2FailureHandler oAuth2FailureHandler
    ) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**"))
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(spaCsrfTokenRequestHandler)
            )
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(unauthorizedEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            .authorizeHttpRequests(securityRegistry::configure)
            .formLogin(login -> login
                .loginProcessingUrl("/api/auth/sign-in")
                .successHandler(signInSuccessHandler)
                .failureHandler(signInFailureHandler)
            )
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(authorization -> authorization
                    .baseUri("/oauth2/authorization")
                )
                .redirectionEndpoint(redirection -> redirection
                    .baseUri("/oauth2/callback/*")
                )
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .successHandler(oAuth2SuccessHandler)
                .failureHandler(oAuth2FailureHandler)
            )
            .logout(logout -> logout
                .logoutUrl("/api/auth/sign-out")
                .addLogoutHandler(signOutHandler)
                .logoutSuccessHandler(
                    new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)
                )
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        // MDC Filter, ExceptionTranslationFilter, Rate Limiting Filters can be added here as needed

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
            .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public static MethodSecurityExpressionHandler methodSecurityExpressionHandler(
        RoleHierarchy roleHierarchy
    ) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy);
        return handler;
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
            .role(UserModel.Role.ADMIN.name())
            .implies(UserModel.Role.USER.name())
            .build();
    }
}
