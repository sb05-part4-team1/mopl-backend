package com.mopl.security.exception;

import com.mopl.domain.exception.auth.InsufficientRoleException;
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
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccessDeniedExceptionHandler 단위 테스트")
class AccessDeniedExceptionHandlerTest {

    @Mock
    private ApiResponseHandler apiResponseHandler;

    private AccessDeniedExceptionHandler handler;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        handler = new AccessDeniedExceptionHandler(apiResponseHandler);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Nested
    @DisplayName("handle()")
    class HandleTest {

        @Test
        @DisplayName("접근 거부 시 InsufficientRoleException으로 응답한다")
        void writesInsufficientRoleError() throws IOException {
            // given
            AccessDeniedException exception = new AccessDeniedException("Access is denied");

            // when
            handler.handle(request, response, exception);

            // then
            ArgumentCaptor<InsufficientRoleException> exceptionCaptor = ArgumentCaptor.forClass(InsufficientRoleException.class);
            then(apiResponseHandler).should().writeError(eq(response), exceptionCaptor.capture());

            assertThat(exceptionCaptor.getValue()).isInstanceOf(InsufficientRoleException.class);
        }

        @Test
        @DisplayName("다양한 접근 거부 메시지에 대해서도 동일하게 처리한다")
        void withVariousMessages_handlesSameWay() throws IOException {
            // given
            AccessDeniedException exception = new AccessDeniedException("User does not have admin role");

            // when
            handler.handle(request, response, exception);

            // then
            ArgumentCaptor<InsufficientRoleException> exceptionCaptor = ArgumentCaptor.forClass(InsufficientRoleException.class);
            then(apiResponseHandler).should().writeError(eq(response), exceptionCaptor.capture());

            assertThat(exceptionCaptor.getValue()).isInstanceOf(InsufficientRoleException.class);
        }

        @Test
        @DisplayName("다양한 요청 경로에서도 동일하게 처리한다")
        void withVariousRequestPaths_handlesSameWay() throws IOException {
            // given
            request.setRequestURI("/api/admin/users");
            AccessDeniedException exception = new AccessDeniedException("Access is denied");

            // when
            handler.handle(request, response, exception);

            // then
            then(apiResponseHandler).should().writeError(eq(response), org.mockito.ArgumentMatchers.any(InsufficientRoleException.class));
        }

        @Test
        @DisplayName("예외 메시지가 null이어도 정상 처리한다")
        void withNullMessage_handlesProperly() throws IOException {
            // given
            AccessDeniedException exception = new AccessDeniedException(null);

            // when
            handler.handle(request, response, exception);

            // then
            then(apiResponseHandler).should().writeError(eq(response), org.mockito.ArgumentMatchers.any(InsufficientRoleException.class));
        }
    }
}
