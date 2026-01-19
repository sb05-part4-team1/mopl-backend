package com.mopl.security.oauth2.handler;

import com.mopl.security.config.OAuth2Properties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.RedirectStrategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2FailureHandler 단위 테스트")
class OAuth2FailureHandlerTest {

    @Mock
    private RedirectStrategy redirectStrategy;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private OAuth2FailureHandler failureHandler;

    private static final String FRONTEND_REDIRECT_URI = "http://localhost:8080";

    @BeforeEach
    void setUp() {
        OAuth2Properties oAuth2Properties = new OAuth2Properties(FRONTEND_REDIRECT_URI);
        failureHandler = new OAuth2FailureHandler(oAuth2Properties);
        failureHandler.setRedirectStrategy(redirectStrategy);
    }

    @Nested
    @DisplayName("onAuthenticationFailure()")
    class OnAuthenticationFailureTest {

        @Test
        @DisplayName("OAuth2 인증 실패 시 /sign-in으로 리다이렉트")
        void withAuthenticationException_redirectsToSignIn() throws Exception {
            // given
            AuthenticationException exception = new OAuth2AuthenticationException(
                new OAuth2Error("authentication_failed"),
                "인증에 실패했습니다."
            );

            // when
            failureHandler.onAuthenticationFailure(request, response, exception);

            // then
            ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
            then(redirectStrategy).should().sendRedirect(eq(request), eq(response), urlCaptor.capture());

            String redirectUrl = urlCaptor.getValue();
            assertThat(redirectUrl).isEqualTo(FRONTEND_REDIRECT_URI + "/#/sign-in");
        }

        @Test
        @DisplayName("provider_mismatch 에러 시에도 /sign-in으로 리다이렉트")
        void withProviderMismatch_redirectsToSignIn() throws Exception {
            // given
            AuthenticationException exception = new OAuth2AuthenticationException(
                new OAuth2Error("provider_mismatch"),
                "이미 EMAIL(으)로 가입된 계정입니다."
            );

            // when
            failureHandler.onAuthenticationFailure(request, response, exception);

            // then
            ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
            then(redirectStrategy).should().sendRedirect(any(), any(), urlCaptor.capture());

            assertThat(urlCaptor.getValue()).isEqualTo(FRONTEND_REDIRECT_URI + "/#/sign-in");
        }

        @Test
        @DisplayName("계정 잠금 에러 시에도 /sign-in으로 리다이렉트")
        void withLockedException_redirectsToSignIn() throws Exception {
            // given
            AuthenticationException exception = new LockedException("계정이 잠겨 있습니다.");

            // when
            failureHandler.onAuthenticationFailure(request, response, exception);

            // then
            ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
            then(redirectStrategy).should().sendRedirect(any(), any(), urlCaptor.capture());

            assertThat(urlCaptor.getValue()).isEqualTo(FRONTEND_REDIRECT_URI + "/#/sign-in");
        }

        @Test
        @DisplayName("리다이렉트 URL에 에러 파라미터가 포함되지 않음")
        void redirectUrl_doesNotContainErrorParameter() throws Exception {
            // given
            AuthenticationException exception = new OAuth2AuthenticationException(
                new OAuth2Error("test"),
                "test error"
            );

            // when
            failureHandler.onAuthenticationFailure(request, response, exception);

            // then
            ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
            then(redirectStrategy).should().sendRedirect(any(), any(), urlCaptor.capture());

            String redirectUrl = urlCaptor.getValue();
            assertThat(redirectUrl).doesNotContain("error");
            assertThat(redirectUrl).doesNotContain("?");
        }
    }
}
