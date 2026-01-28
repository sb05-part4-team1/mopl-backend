package com.mopl.websocket.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityRegistryImpl 단위 테스트")
class SecurityRegistryImplTest {

    @Mock
    private AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth;

    @Mock
    private AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizedUrl authorizedUrl;

    private SecurityRegistryImpl securityRegistry;

    @BeforeEach
    void setUp() {
        securityRegistry = new SecurityRegistryImpl();
    }

    @Test
    @DisplayName("WebSocket 엔드포인트 permitAll 설정")
    void configure_websocketEndpoints_permitAll() {
        // given
        given(auth.requestMatchers("/ws/**")).willReturn(authorizedUrl);
        given(authorizedUrl.permitAll()).willReturn(auth);
        given(auth.requestMatchers(
            "/actuator/health",
            "/actuator/info",
            "/actuator/prometheus",
            "/actuator/metrics",
            "/actuator/metrics/**"
        )).willReturn(authorizedUrl);
        given(auth.anyRequest()).willReturn(authorizedUrl);
        given(authorizedUrl.denyAll()).willReturn(auth);

        // when
        securityRegistry.configure(auth);

        // then
        verify(auth).requestMatchers("/ws/**");
        verify(auth).requestMatchers(
            "/actuator/health",
            "/actuator/info",
            "/actuator/prometheus",
            "/actuator/metrics",
            "/actuator/metrics/**"
        );
        verify(auth).anyRequest();
        verify(authorizedUrl).denyAll();
    }
}
