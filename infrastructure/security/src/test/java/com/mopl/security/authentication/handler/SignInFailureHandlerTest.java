package com.mopl.security.authentication.handler;

import com.mopl.domain.exception.InternalServerException;
import com.mopl.domain.exception.MoplException;
import com.mopl.domain.exception.auth.AccountLockedException;
import com.mopl.domain.exception.auth.InvalidCredentialsException;
import com.mopl.security.exception.ApiResponseHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.ProviderNotFoundException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("SignInFailureHandler 단위 테스트")
class SignInFailureHandlerTest {

    @Mock
    private ApiResponseHandler apiResponseHandler;

    private SignInFailureHandler failureHandler;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        failureHandler = new SignInFailureHandler(apiResponseHandler);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Nested
    @DisplayName("onAuthenticationFailure()")
    class OnAuthenticationFailureTest {

        @Test
        @DisplayName("잠긴 계정 예외는 AccountLockedException으로 변환된다")
        void withLockedException_convertsToAccountLockedException() throws IOException {
            // given
            String email = "locked@example.com";
            request.setParameter("email", email);
            LockedException lockedException = new LockedException("Account is locked");

            // when
            failureHandler.onAuthenticationFailure(request, response, lockedException);

            // then
            ArgumentCaptor<MoplException> exceptionCaptor = ArgumentCaptor.forClass(MoplException.class);
            then(apiResponseHandler).should().writeError(eq(response), exceptionCaptor.capture());

            assertThat(exceptionCaptor.getValue()).isInstanceOf(AccountLockedException.class);
        }

        @Test
        @DisplayName("AuthenticationServiceException은 InternalServerException으로 변환된다")
        void withAuthenticationServiceException_convertsToInternalServerException() throws IOException {
            // given
            request.setParameter("email", "test@example.com");
            AuthenticationServiceException serviceException = new AuthenticationServiceException("Service error");

            // when
            failureHandler.onAuthenticationFailure(request, response, serviceException);

            // then
            ArgumentCaptor<MoplException> exceptionCaptor = ArgumentCaptor.forClass(MoplException.class);
            then(apiResponseHandler).should().writeError(eq(response), exceptionCaptor.capture());

            assertThat(exceptionCaptor.getValue()).isInstanceOf(InternalServerException.class);
        }

        @Test
        @DisplayName("ProviderNotFoundException은 InternalServerException으로 변환된다")
        void withProviderNotFoundException_convertsToInternalServerException() throws IOException {
            // given
            request.setParameter("email", "test@example.com");
            ProviderNotFoundException providerException = new ProviderNotFoundException("No provider found");

            // when
            failureHandler.onAuthenticationFailure(request, response, providerException);

            // then
            ArgumentCaptor<MoplException> exceptionCaptor = ArgumentCaptor.forClass(MoplException.class);
            then(apiResponseHandler).should().writeError(eq(response), exceptionCaptor.capture());

            assertThat(exceptionCaptor.getValue()).isInstanceOf(InternalServerException.class);
        }

        @Test
        @DisplayName("BadCredentialsException은 InvalidCredentialsException으로 변환된다")
        void withBadCredentialsException_convertsToInvalidCredentialsException() throws IOException {
            // given
            request.setParameter("email", "test@example.com");
            BadCredentialsException badCredentials = new BadCredentialsException("Bad credentials");

            // when
            failureHandler.onAuthenticationFailure(request, response, badCredentials);

            // then
            ArgumentCaptor<MoplException> exceptionCaptor = ArgumentCaptor.forClass(MoplException.class);
            then(apiResponseHandler).should().writeError(eq(response), exceptionCaptor.capture());

            assertThat(exceptionCaptor.getValue()).isInstanceOf(InvalidCredentialsException.class);
        }

        @Test
        @DisplayName("기타 인증 예외는 InvalidCredentialsException으로 변환된다")
        void withOtherAuthenticationException_convertsToInvalidCredentialsException() throws IOException {
            // given
            request.setParameter("email", "test@example.com");

            // when
            failureHandler.onAuthenticationFailure(request, response,
                new BadCredentialsException("Unknown error"));

            // then
            ArgumentCaptor<MoplException> exceptionCaptor = ArgumentCaptor.forClass(MoplException.class);
            then(apiResponseHandler).should().writeError(eq(response), exceptionCaptor.capture());

            assertThat(exceptionCaptor.getValue()).isInstanceOf(InvalidCredentialsException.class);
        }

        @Test
        @DisplayName("이메일 파라미터가 없어도 정상 처리된다")
        void withoutEmailParameter_handlesProperly() throws IOException {
            // given
            BadCredentialsException exception = new BadCredentialsException("Bad credentials");

            // when
            failureHandler.onAuthenticationFailure(request, response, exception);

            // then
            then(apiResponseHandler).should().writeError(eq(response), any(InvalidCredentialsException.class));
        }
    }
}
