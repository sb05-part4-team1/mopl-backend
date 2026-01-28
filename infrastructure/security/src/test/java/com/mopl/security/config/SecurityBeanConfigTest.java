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
import com.mopl.security.jwt.service.TokenRefreshService;
import com.mopl.security.oauth2.CustomOAuth2UserService;
import com.mopl.security.oauth2.handler.OAuth2FailureHandler;
import com.mopl.security.oauth2.handler.OAuth2SuccessHandler;
import com.mopl.security.userdetails.MoplUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityBeanConfig 단위 테스트")
class SecurityBeanConfigTest {

    private SecurityBeanConfig securityBeanConfig;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TemporaryPasswordRepository temporaryPasswordRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    private JwtProperties jwtProperties;
    private OAuth2Properties oAuth2Properties;

    @BeforeEach
    void setUp() {
        securityBeanConfig = new SecurityBeanConfig();
        jwtProperties = new JwtProperties(
            new JwtProperties.Config("test-access-secret-key-minimum-32-bytes-length", Duration.ofMinutes(30), null),
            new JwtProperties.Config("test-refresh-secret-key-minimum-32-bytes-length", Duration.ofDays(7), null),
            3,
            JwtProperties.JwtRegistryType.IN_MEMORY,
            "REFRESH_TOKEN"
        );
        oAuth2Properties = new OAuth2Properties(
            "http://localhost:3000/oauth2/callback"
        );
    }

    @Nested
    @DisplayName("기본 빈 생성 메서드")
    class BasicBeanCreationTest {

        @Test
        @DisplayName("ApiResponseHandler 빈을 생성한다")
        void createsApiResponseHandler() {
            // given
            ObjectMapper objectMapper = new ObjectMapper();

            // when
            ApiResponseHandler handler = securityBeanConfig.apiResponseHandler(objectMapper);

            // then
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("SpaCsrfTokenRequestHandler 빈을 생성한다")
        void createsSpaCsrfTokenRequestHandler() {
            // when
            SpaCsrfTokenRequestHandler handler = securityBeanConfig.spaCsrfTokenRequestHandler();

            // then
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("UserDetailsService 빈을 생성한다")
        void createsUserDetailsService() {
            // when
            UserDetailsService service = securityBeanConfig.userDetailsService(userService);

            // then
            assertThat(service).isNotNull();
            assertThat(service).isInstanceOf(MoplUserDetailsService.class);
        }
    }

    @Nested
    @DisplayName("JWT 관련 빈 생성")
    class JwtBeanCreationTest {

        @Test
        @DisplayName("JwtProvider 빈을 생성한다")
        void createsJwtProvider() {
            // when
            JwtProvider provider = securityBeanConfig.jwtProvider(jwtProperties);

            // then
            assertThat(provider).isNotNull();
        }

        @Test
        @DisplayName("JwtCookieProvider 빈을 생성한다")
        void createsJwtCookieProvider() {
            // when
            JwtCookieProvider provider = securityBeanConfig.jwtCookieProvider(jwtProperties);

            // then
            assertThat(provider).isNotNull();
        }

        @Test
        @DisplayName("InMemoryJwtRegistry 빈을 생성한다")
        void createsInMemoryJwtRegistry() {
            // when
            JwtRegistry registry = securityBeanConfig.inMemoryJwtRegistry(jwtProperties);

            // then
            assertThat(registry).isNotNull();
            assertThat(registry).isInstanceOf(InMemoryJwtRegistry.class);
        }

        @Test
        @DisplayName("RedisJwtRegistry 빈을 생성한다")
        void createsRedisJwtRegistry() {
            // when
            JwtRegistry registry = securityBeanConfig.redisJwtRegistry(
                stringRedisTemplate,
                new ObjectMapper(),
                jwtProperties
            );

            // then
            assertThat(registry).isNotNull();
        }
    }

    @Nested
    @DisplayName("Exception Handler 빈 생성")
    class ExceptionHandlerBeanCreationTest {

        @Test
        @DisplayName("UnauthorizedEntryPoint 빈을 생성한다")
        void createsUnauthorizedEntryPoint() {
            // given
            ApiResponseHandler apiResponseHandler = new ApiResponseHandler(new ObjectMapper());

            // when
            UnauthorizedEntryPoint entryPoint = securityBeanConfig.unauthorizedEntryPoint(apiResponseHandler);

            // then
            assertThat(entryPoint).isNotNull();
        }

        @Test
        @DisplayName("AccessDeniedExceptionHandler 빈을 생성한다")
        void createsAccessDeniedExceptionHandler() {
            // given
            ApiResponseHandler apiResponseHandler = new ApiResponseHandler(new ObjectMapper());

            // when
            AccessDeniedExceptionHandler handler = securityBeanConfig.accessDeniedExceptionHandler(apiResponseHandler);

            // then
            assertThat(handler).isNotNull();
        }
    }

    @Nested
    @DisplayName("Authentication Handler 빈 생성")
    class AuthenticationHandlerBeanCreationTest {

        @Test
        @DisplayName("SignInFailureHandler 빈을 생성한다")
        void createsSignInFailureHandler() {
            // given
            ApiResponseHandler apiResponseHandler = new ApiResponseHandler(new ObjectMapper());

            // when
            SignInFailureHandler handler = securityBeanConfig.signInFailureHandler(apiResponseHandler);

            // then
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("SignInSuccessHandler 빈을 생성한다")
        void createsSignInSuccessHandler() {
            // given
            JwtProvider jwtProvider = new JwtProvider(jwtProperties);
            JwtCookieProvider jwtCookieProvider = new JwtCookieProvider(jwtProperties);
            ApiResponseHandler apiResponseHandler = new ApiResponseHandler(new ObjectMapper());
            JwtRegistry jwtRegistry = new InMemoryJwtRegistry(jwtProperties);

            // when
            SignInSuccessHandler handler = securityBeanConfig.signInSuccessHandler(
                jwtProvider,
                jwtCookieProvider,
                apiResponseHandler,
                jwtRegistry
            );

            // then
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("SignOutHandler 빈을 생성한다")
        void createsSignOutHandler() {
            // given
            JwtProvider jwtProvider = new JwtProvider(jwtProperties);
            JwtCookieProvider jwtCookieProvider = new JwtCookieProvider(jwtProperties);
            JwtRegistry jwtRegistry = new InMemoryJwtRegistry(jwtProperties);

            // when
            SignOutHandler handler = securityBeanConfig.signOutHandler(
                jwtProvider,
                jwtCookieProvider,
                jwtRegistry,
                jwtProperties
            );

            // then
            assertThat(handler).isNotNull();
        }
    }

    @Nested
    @DisplayName("Filter 및 Service 빈 생성")
    class FilterAndServiceBeanCreationTest {

        @Test
        @DisplayName("JwtAuthenticationFilter 빈을 생성한다")
        void createsJwtAuthenticationFilter() {
            // given
            JwtProvider jwtProvider = new JwtProvider(jwtProperties);
            JwtRegistry jwtRegistry = new InMemoryJwtRegistry(jwtProperties);
            ApiResponseHandler apiResponseHandler = new ApiResponseHandler(new ObjectMapper());

            // when
            JwtAuthenticationFilter filter = securityBeanConfig.jwtAuthenticationFilter(
                jwtProvider,
                jwtRegistry,
                apiResponseHandler
            );

            // then
            assertThat(filter).isNotNull();
        }

        @Test
        @DisplayName("TokenRefreshService 빈을 생성한다")
        void createsTokenRefreshService() {
            // given
            JwtProvider jwtProvider = new JwtProvider(jwtProperties);
            JwtCookieProvider jwtCookieProvider = new JwtCookieProvider(jwtProperties);
            JwtRegistry jwtRegistry = new InMemoryJwtRegistry(jwtProperties);

            // when
            TokenRefreshService service = securityBeanConfig.tokenRefreshService(
                jwtProvider,
                jwtCookieProvider,
                jwtRegistry,
                userService
            );

            // then
            assertThat(service).isNotNull();
        }

        @Test
        @DisplayName("AuthenticationProvider 빈을 생성한다")
        void createsAuthenticationProvider() {
            // given
            UserDetailsService userDetailsService = new MoplUserDetailsService(userService);

            // when
            AuthenticationProvider provider = securityBeanConfig.authenticationProvider(
                userDetailsService,
                passwordEncoder,
                temporaryPasswordRepository
            );

            // then
            assertThat(provider).isNotNull();
            assertThat(provider).isInstanceOf(TemporaryPasswordAuthenticationProvider.class);
        }
    }

    @Nested
    @DisplayName("OAuth2 빈 생성")
    class OAuth2BeanCreationTest {

        @Test
        @DisplayName("CustomOAuth2UserService 빈을 생성한다")
        void createsCustomOAuth2UserService() {
            // when
            CustomOAuth2UserService service = securityBeanConfig.customOAuth2UserService(
                userRepository,
                userService
            );

            // then
            assertThat(service).isNotNull();
        }

        @Test
        @DisplayName("OAuth2SuccessHandler 빈을 생성한다")
        void createsOAuth2SuccessHandler() {
            // given
            JwtProvider jwtProvider = new JwtProvider(jwtProperties);
            JwtCookieProvider jwtCookieProvider = new JwtCookieProvider(jwtProperties);
            JwtRegistry jwtRegistry = new InMemoryJwtRegistry(jwtProperties);

            // when
            OAuth2SuccessHandler handler = securityBeanConfig.oAuth2SuccessHandler(
                oAuth2Properties,
                jwtProvider,
                jwtCookieProvider,
                jwtRegistry
            );

            // then
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("OAuth2FailureHandler 빈을 생성한다")
        void createsOAuth2FailureHandler() {
            // when
            OAuth2FailureHandler handler = securityBeanConfig.oAuth2FailureHandler(oAuth2Properties);

            // then
            assertThat(handler).isNotNull();
        }
    }
}
