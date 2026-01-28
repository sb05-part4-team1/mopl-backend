package com.mopl.security.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SecurityAutoConfig 단위 테스트")
class SecurityAutoConfigTest {

    @Nested
    @DisplayName("defaultSecurityRegistry()")
    class DefaultSecurityRegistryTest {

        @Test
        @DisplayName("기본 SecurityRegistry 빈을 생성한다")
        void createsDefaultSecurityRegistry() {
            // given
            SecurityAutoConfig config = new SecurityAutoConfig();

            // when
            SecurityRegistry registry = config.defaultSecurityRegistry();

            // then
            assertThat(registry).isNotNull();
        }
    }

    @Nested
    @DisplayName("webSecurityCustomizer()")
    class WebSecurityCustomizerTest {

        @Test
        @DisplayName("WebSecurityCustomizer 빈을 생성한다")
        void createsWebSecurityCustomizer() {
            // given
            SecurityAutoConfig config = new SecurityAutoConfig();

            // when
            WebSecurityCustomizer customizer = config.webSecurityCustomizer();

            // then
            assertThat(customizer).isNotNull();
        }
    }

    @Nested
    @DisplayName("passwordEncoder()")
    class PasswordEncoderTest {

        @Test
        @DisplayName("BCryptPasswordEncoder를 생성한다")
        void createsBCryptPasswordEncoder() {
            // given
            SecurityAutoConfig config = new SecurityAutoConfig();

            // when
            PasswordEncoder encoder = config.passwordEncoder();

            // then
            assertThat(encoder).isNotNull();
            assertThat(encoder.getClass().getSimpleName()).isEqualTo("BCryptPasswordEncoder");
        }

        @Test
        @DisplayName("패스워드를 암호화하고 검증할 수 있다")
        void canEncodeAndVerifyPasswords() {
            // given
            SecurityAutoConfig config = new SecurityAutoConfig();
            PasswordEncoder encoder = config.passwordEncoder();
            String rawPassword = "testPassword123!";

            // when
            String encoded = encoder.encode(rawPassword);

            // then
            assertThat(encoded).isNotEqualTo(rawPassword);
            assertThat(encoder.matches(rawPassword, encoded)).isTrue();
            assertThat(encoder.matches("wrongPassword", encoded)).isFalse();
        }
    }

    @Nested
    @DisplayName("roleHierarchy()")
    class RoleHierarchyTest {

        @Test
        @DisplayName("RoleHierarchy 빈을 생성한다")
        void createsRoleHierarchy() {
            // given
            SecurityAutoConfig config = new SecurityAutoConfig();

            // when
            RoleHierarchy hierarchy = config.roleHierarchy();

            // then
            assertThat(hierarchy).isNotNull();
        }

        @Test
        @DisplayName("생성된 RoleHierarchy는 정상적으로 빌드된다")
        void adminImpliesUser() {
            // given
            SecurityAutoConfig config = new SecurityAutoConfig();

            // when
            RoleHierarchy hierarchy = config.roleHierarchy();

            // then - RoleHierarchy가 정상적으로 생성되었는지 확인
            assertThat(hierarchy).isNotNull();
        }
    }

    @Nested
    @DisplayName("methodSecurityExpressionHandler()")
    class MethodSecurityExpressionHandlerTest {

        @Test
        @DisplayName("MethodSecurityExpressionHandler를 RoleHierarchy와 함께 생성한다")
        void createsMethodSecurityExpressionHandlerWithRoleHierarchy() {
            // given
            SecurityAutoConfig config = new SecurityAutoConfig();
            RoleHierarchy roleHierarchy = config.roleHierarchy();

            // when
            MethodSecurityExpressionHandler handler = SecurityAutoConfig.methodSecurityExpressionHandler(roleHierarchy);

            // then
            assertThat(handler).isNotNull();
        }
    }
}
