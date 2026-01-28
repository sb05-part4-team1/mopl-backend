package com.mopl.security.exception;

import com.mopl.domain.exception.auth.UnauthorizedException;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("UnauthorizedEntryPoint 단위 테스트")
class UnauthorizedEntryPointTest {

    @Mock
    private ApiResponseHandler apiResponseHandler;

    private UnauthorizedEntryPoint entryPoint;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        entryPoint = new UnauthorizedEntryPoint(apiResponseHandler);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Nested
    @DisplayName("commence()")
    class CommenceTest {

        @Test
        @DisplayName("인증되지 않은 요청에 대해 UnauthorizedException으로 응답한다")
        void writesUnauthorizedError() throws IOException {
            // given
            AuthenticationException exception = new InsufficientAuthenticationException("Not authenticated");

            // when
            entryPoint.commence(request, response, exception);

            // then
            ArgumentCaptor<UnauthorizedException> exceptionCaptor = ArgumentCaptor.forClass(UnauthorizedException.class);
            then(apiResponseHandler).should().writeError(eq(response), exceptionCaptor.capture());

            assertThat(exceptionCaptor.getValue()).isInstanceOf(UnauthorizedException.class);
        }

        @Test
        @DisplayName("BadCredentialsException에 대해서도 UnauthorizedException으로 응답한다")
        void withBadCredentials_writesUnauthorizedError() throws IOException {
            // given
            AuthenticationException exception = new BadCredentialsException("Bad credentials");

            // when
            entryPoint.commence(request, response, exception);

            // then
            ArgumentCaptor<UnauthorizedException> exceptionCaptor = ArgumentCaptor.forClass(UnauthorizedException.class);
            then(apiResponseHandler).should().writeError(eq(response), exceptionCaptor.capture());

            assertThat(exceptionCaptor.getValue()).isInstanceOf(UnauthorizedException.class);
        }

        @Test
        @DisplayName("다양한 요청 경로에서도 동일하게 처리한다")
        void withVariousRequestPaths_handlesSameWay() throws IOException {
            // given
            request.setRequestURI("/api/protected/resource");
            AuthenticationException exception = new InsufficientAuthenticationException("Not authenticated");

            // when
            entryPoint.commence(request, response, exception);

            // then
            then(apiResponseHandler).should().writeError(eq(response), org.mockito.ArgumentMatchers.any(UnauthorizedException.class));
        }
    }
}
