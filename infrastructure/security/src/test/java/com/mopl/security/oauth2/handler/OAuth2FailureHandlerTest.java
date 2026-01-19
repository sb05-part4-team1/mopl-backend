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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.RedirectStrategy;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

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
        @DisplayName("OAuth2 인증 실패 시 에러 메시지와 함께 /sign-in으로 리다이렉트")
        void withAuthenticationException_redirectsToSignInWithError() throws Exception {
            // given
            String errorMessage = "인증에 실패했습니다.";
            AuthenticationException exception = new OAuth2AuthenticationException(
                new OAuth2Error("authentication_failed"),
                errorMessage
            );

            // when
            failureHandler.onAuthenticationFailure(request, response, exception);

            // then
            ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
            then(redirectStrategy).should().sendRedirect(eq(request), eq(response), urlCaptor
                .capture());

            String redirectUrl = urlCaptor.getValue();
            assertThat(redirectUrl).startsWith(FRONTEND_REDIRECT_URI + "/#/sign-in");
            assertThat(redirectUrl).contains("error=");

            String encodedError = redirectUrl.substring(redirectUrl.indexOf("error=") + 6);
            String decodedError = URLDecoder.decode(encodedError, StandardCharsets.UTF_8);
            assertThat(decodedError).isEqualTo(errorMessage);
        }

        @Test
        @DisplayName("provider_mismatch 에러 시 해당 메시지로 리다이렉트")
        void withProviderMismatch_redirectsWithErrorMessage() throws Exception {
            // given
            String errorMessage = "이미 EMAIL(으)로 가입된 계정입니다.";
            AuthenticationException exception = new OAuth2AuthenticationException(
                new OAuth2Error("provider_mismatch"),
                errorMessage
            );

            // when
            failureHandler.onAuthenticationFailure(request, response, exception);

            // then
            ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
            then(redirectStrategy).should().sendRedirect(any(), any(), urlCaptor.capture());

            String redirectUrl = urlCaptor.getValue();
            assertThat(redirectUrl).contains("/#/sign-in");

            String encodedError = redirectUrl.substring(redirectUrl.indexOf("error=") + 6);
            String decodedError = URLDecoder.decode(encodedError, StandardCharsets.UTF_8);
            assertThat(decodedError).isEqualTo(errorMessage);
        }

        @Test
        @DisplayName("특수문자가 포함된 에러 메시지도 URL 인코딩됨")
        void withSpecialCharacters_encodesErrorMessage() throws Exception {
            // given
            String errorMessage = "Error: 인증 실패 & 재시도 필요!";
            AuthenticationException exception = new OAuth2AuthenticationException(
                new OAuth2Error("error"),
                errorMessage
            );

            // when
            failureHandler.onAuthenticationFailure(request, response, exception);

            // then
            ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
            then(redirectStrategy).should().sendRedirect(any(), any(), urlCaptor.capture());

            String redirectUrl = urlCaptor.getValue();
            // URL에 인코딩되지 않은 특수문자가 없어야 함
            assertThat(redirectUrl).doesNotContain("&재시도");
            assertThat(redirectUrl).doesNotContain(" ");

            // 디코딩하면 원본 메시지가 나와야 함
            String encodedError = redirectUrl.substring(redirectUrl.indexOf("error=") + 6);
            String decodedError = URLDecoder.decode(encodedError, StandardCharsets.UTF_8);
            assertThat(decodedError).isEqualTo(errorMessage);
        }

        @Test
        @DisplayName("리다이렉트 URL 형식이 올바름")
        void redirectUrl_hasCorrectFormat() throws Exception {
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
            assertThat(redirectUrl)
                .startsWith(FRONTEND_REDIRECT_URI)
                .contains("/#/sign-in")
                .contains("?error=");
        }
    }
}
