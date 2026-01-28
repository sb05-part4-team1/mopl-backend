package com.mopl.security.csrf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("SpaCsrfTokenRequestHandler 단위 테스트")
class SpaCsrfTokenRequestHandlerTest {

    private SpaCsrfTokenRequestHandler handler;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    private static final String HEADER_NAME = "X-XSRF-TOKEN";
    private static final String PARAMETER_NAME = "_csrf";
    private static final String TOKEN_VALUE = "test-csrf-token";

    @BeforeEach
    void setUp() {
        handler = new SpaCsrfTokenRequestHandler();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Nested
    @DisplayName("handle()")
    class HandleTest {

        @Test
        @DisplayName("CSRF 토큰을 요청 속성에 설정한다")
        void setsCsrfTokenAttribute() {
            // given
            CsrfToken csrfToken = new DefaultCsrfToken(HEADER_NAME, PARAMETER_NAME, TOKEN_VALUE);
            Supplier<CsrfToken> tokenSupplier = () -> csrfToken;

            // when
            handler.handle(request, response, tokenSupplier);

            // then
            // XorCsrfTokenRequestAttributeHandler가 토큰을 설정했는지 확인
            // 실제로는 XOR 인코딩된 값이 설정됨
            assertThat(request.getAttribute(CsrfToken.class.getName())).isNotNull();
        }
    }

    @Nested
    @DisplayName("resolveCsrfTokenValue()")
    class ResolveCsrfTokenValueTest {

        @Test
        @DisplayName("헤더에 토큰이 있으면 plain handler로 처리한다")
        void withHeaderToken_usesPlainHandler() {
            // given
            CsrfToken csrfToken = new DefaultCsrfToken(HEADER_NAME, PARAMETER_NAME, TOKEN_VALUE);
            request.addHeader(HEADER_NAME, TOKEN_VALUE);

            // when
            String resolved = handler.resolveCsrfTokenValue(request, csrfToken);

            // then
            // plain handler는 헤더 값을 그대로 반환
            assertThat(resolved).isEqualTo(TOKEN_VALUE);
        }

        @Test
        @DisplayName("헤더에 토큰이 없으면 xor handler로 처리한다")
        void withoutHeaderToken_usesXorHandler() {
            // given
            CsrfToken csrfToken = new DefaultCsrfToken(HEADER_NAME, PARAMETER_NAME, TOKEN_VALUE);

            // when
            String resolved = handler.resolveCsrfTokenValue(request, csrfToken);

            // then
            // xor handler는 파라미터에서 토큰을 찾고 XOR 디코딩을 시도
            // 파라미터가 없으므로 null 반환
            assertThat(resolved).isNull();
        }

        @Test
        @DisplayName("빈 헤더 값은 xor handler로 처리한다")
        void withEmptyHeaderValue_usesXorHandler() {
            // given
            CsrfToken csrfToken = new DefaultCsrfToken(HEADER_NAME, PARAMETER_NAME, TOKEN_VALUE);
            request.addHeader(HEADER_NAME, "");

            // when
            String resolved = handler.resolveCsrfTokenValue(request, csrfToken);

            // then
            // 빈 문자열은 hasText가 false이므로 xor handler 사용
            assertThat(resolved).isNull();
        }

        @Test
        @DisplayName("공백만 있는 헤더 값은 xor handler로 처리한다")
        void withWhitespaceOnlyHeader_usesXorHandler() {
            // given
            CsrfToken csrfToken = new DefaultCsrfToken(HEADER_NAME, PARAMETER_NAME, TOKEN_VALUE);
            request.addHeader(HEADER_NAME, "   ");

            // when
            String resolved = handler.resolveCsrfTokenValue(request, csrfToken);

            // then
            assertThat(resolved).isNull();
        }
    }
}
