package com.mopl.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.domain.exception.ErrorResponse;
import com.mopl.domain.exception.MoplException;
import com.mopl.domain.exception.auth.InvalidTokenException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApiResponseHandler 단위 테스트")
class ApiResponseHandlerTest {

    @Mock
    private HttpServletResponse response;

    @Mock
    private PrintWriter writer;

    @Mock
    private ObjectMapper objectMapper;

    private ApiResponseHandler apiResponseHandler;

    @BeforeEach
    void setUp() throws IOException {
        apiResponseHandler = new ApiResponseHandler(objectMapper);
        given(response.getWriter()).willReturn(writer);
    }

    @Nested
    @DisplayName("writeError()")
    class WriteErrorTest {

        @Test
        @DisplayName("MoplException을 ErrorResponse로 변환하여 응답을 작성한다")
        void withMoplException_writesErrorResponse() throws IOException {
            // given
            MoplException exception = InvalidTokenException.create();

            // when
            apiResponseHandler.writeError(response, exception);

            // then
            then(response).should().setStatus(exception.getErrorCode().getStatus());
            then(response).should().setContentType(MediaType.APPLICATION_JSON_VALUE);
            then(response).should().setCharacterEncoding(StandardCharsets.UTF_8.name());
            then(objectMapper).should().writeValue(eq(writer), any(ErrorResponse.class));
        }

        @Test
        @DisplayName("예외의 HTTP 상태 코드와 에러 응답을 정확히 설정한다")
        void setsCorrectStatusCodeAndErrorResponse() throws IOException {
            // given
            InvalidTokenException exception = InvalidTokenException.create();

            // when
            apiResponseHandler.writeError(response, exception);

            // then
            then(response).should().setStatus(401);
            then(response).should().setContentType(MediaType.APPLICATION_JSON_VALUE);
            then(response).should().setCharacterEncoding(StandardCharsets.UTF_8.name());
            then(objectMapper).should().writeValue(eq(writer), any(ErrorResponse.class));
        }
    }

    @Nested
    @DisplayName("writeSuccess()")
    class WriteSuccessTest {

        @Test
        @DisplayName("성공 응답을 JSON으로 작성한다")
        void withSuccessBody_writesJsonResponse() throws IOException {
            // given
            Map<String, String> successBody = Map.of("message", "Success", "status", "OK");

            // when
            apiResponseHandler.writeSuccess(response, successBody);

            // then
            then(response).should().setStatus(HttpServletResponse.SC_OK);
            then(response).should().setContentType(MediaType.APPLICATION_JSON_VALUE);
            then(response).should().setCharacterEncoding(StandardCharsets.UTF_8.name());
            then(objectMapper).should().writeValue(writer, successBody);
        }

        @Test
        @DisplayName("200 OK 상태 코드로 응답을 작성한다")
        void setsStatusCodeTo200() throws IOException {
            // given
            Object body = Map.of("data", "test");

            // when
            apiResponseHandler.writeSuccess(response, body);

            // then
            then(response).should().setStatus(200);
        }
    }

    @Nested
    @DisplayName("write() - private method 간접 테스트")
    class WriteTest {

        @Test
        @DisplayName("응답의 Content-Type을 application/json으로 설정한다")
        void setsContentTypeToJson() throws IOException {
            // given
            Object body = Map.of("test", "data");

            // when
            apiResponseHandler.writeSuccess(response, body);

            // then
            then(response).should().setContentType(MediaType.APPLICATION_JSON_VALUE);
        }

        @Test
        @DisplayName("응답의 Character Encoding을 UTF-8로 설정한다")
        void setsCharacterEncodingToUtf8() throws IOException {
            // given
            Object body = Map.of("test", "data");

            // when
            apiResponseHandler.writeSuccess(response, body);

            // then
            then(response).should().setCharacterEncoding(StandardCharsets.UTF_8.name());
        }

        @Test
        @DisplayName("ObjectMapper를 사용하여 body를 JSON으로 직렬화한다")
        void serializesBodyToJson() throws IOException {
            // given
            Map<String, String> body = Map.of("key", "value");

            // when
            apiResponseHandler.writeSuccess(response, body);

            // then
            then(objectMapper).should().writeValue(writer, body);
        }
    }
}
