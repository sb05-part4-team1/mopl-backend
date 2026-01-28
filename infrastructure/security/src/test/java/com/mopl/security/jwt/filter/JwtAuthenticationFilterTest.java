package com.mopl.security.jwt.filter;

import com.mopl.domain.exception.auth.InvalidTokenException;
import com.mopl.domain.model.user.UserModel;
import com.mopl.security.exception.ApiResponseHandler;
import com.mopl.security.jwt.provider.JwtPayload;
import com.mopl.security.jwt.provider.JwtProvider;
import com.mopl.security.jwt.provider.TokenType;
import com.mopl.security.jwt.registry.JwtRegistry;
import com.mopl.security.userdetails.MoplUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter 단위 테스트")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private JwtRegistry jwtRegistry;

    @Mock
    private ApiResponseHandler apiResponseHandler;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtProvider, jwtRegistry, apiResponseHandler);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("doFilterInternal()")
    class DoFilterInternalTest {

        @Test
        @DisplayName("Authorization 헤더가 없으면 필터 체인을 계속 진행한다")
        void withoutAuthHeader_continuesFilterChain() throws ServletException, IOException {
            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            then(filterChain).should().doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("Bearer 접두사가 없는 Authorization 헤더는 무시한다")
        void withoutBearerPrefix_continuesFilterChain() throws ServletException, IOException {
            // given
            request.addHeader("Authorization", "Basic token123");

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            then(filterChain).should().doFilter(request, response);
            then(jwtProvider).should(never()).verifyAndParse(any(), any());
        }

        @Test
        @DisplayName("유효한 토큰으로 인증에 성공한다")
        void withValidToken_authenticatesUser() throws ServletException, IOException {
            // given
            String token = "valid-token";
            UUID userId = UUID.randomUUID();
            UUID jti = UUID.randomUUID();
            JwtPayload payload = new JwtPayload(
                userId,
                jti,
                new Date(),
                new Date(System.currentTimeMillis() + 3600_000),
                UserModel.Role.USER
            );

            request.addHeader("Authorization", "Bearer " + token);
            given(jwtProvider.verifyAndParse(token, TokenType.ACCESS)).willReturn(payload);
            given(jwtRegistry.isAccessTokenInBlacklist(jti)).willReturn(false);

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            then(filterChain).should().doFilter(request, response);

            UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

            assertThat(authentication).isNotNull();
            assertThat(authentication.getPrincipal()).isInstanceOf(MoplUserDetails.class);

            MoplUserDetails userDetails = (MoplUserDetails) authentication.getPrincipal();
            assertThat(userDetails.userId()).isEqualTo(userId);
            assertThat(userDetails.role()).isEqualTo(UserModel.Role.USER);
        }

        @Test
        @DisplayName("블랙리스트에 있는 토큰은 인증에 실패한다")
        void withBlacklistedToken_failsAuthentication() throws ServletException, IOException {
            // given
            String token = "blacklisted-token";
            UUID userId = UUID.randomUUID();
            UUID jti = UUID.randomUUID();
            JwtPayload payload = new JwtPayload(
                userId,
                jti,
                new Date(),
                new Date(System.currentTimeMillis() + 3600_000),
                UserModel.Role.USER
            );

            request.addHeader("Authorization", "Bearer " + token);
            given(jwtProvider.verifyAndParse(token, TokenType.ACCESS)).willReturn(payload);
            given(jwtRegistry.isAccessTokenInBlacklist(jti)).willReturn(true);

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            then(filterChain).should(never()).doFilter(request, response);
            then(apiResponseHandler).should().writeError(eq(response), any(InvalidTokenException.class));
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("유효하지 않은 토큰은 인증에 실패한다")
        void withInvalidToken_failsAuthentication() throws ServletException, IOException {
            // given
            String token = "invalid-token";
            request.addHeader("Authorization", "Bearer " + token);
            given(jwtProvider.verifyAndParse(token, TokenType.ACCESS))
                .willThrow(InvalidTokenException.create());

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            then(filterChain).should(never()).doFilter(request, response);
            then(apiResponseHandler).should().writeError(eq(response), any(InvalidTokenException.class));
        }

        @Test
        @DisplayName("예상치 못한 예외 발생 시 InternalServerException으로 응답한다")
        void withUnexpectedException_returnsInternalError() throws ServletException, IOException {
            // given
            String token = "token";
            request.addHeader("Authorization", "Bearer " + token);
            given(jwtProvider.verifyAndParse(token, TokenType.ACCESS))
                .willThrow(new RuntimeException("unexpected error"));

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            then(filterChain).should(never()).doFilter(request, response);
            then(apiResponseHandler).should().writeError(eq(response), any());
        }

        @Test
        @DisplayName("ADMIN 역할의 토큰으로 인증에 성공한다")
        void withAdminRole_authenticatesUser() throws ServletException, IOException {
            // given
            String token = "admin-token";
            UUID userId = UUID.randomUUID();
            UUID jti = UUID.randomUUID();
            JwtPayload payload = new JwtPayload(
                userId,
                jti,
                new Date(),
                new Date(System.currentTimeMillis() + 3600_000),
                UserModel.Role.ADMIN
            );

            request.addHeader("Authorization", "Bearer " + token);
            given(jwtProvider.verifyAndParse(token, TokenType.ACCESS)).willReturn(payload);
            given(jwtRegistry.isAccessTokenInBlacklist(jti)).willReturn(false);

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            MoplUserDetails userDetails = (MoplUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            assertThat(userDetails.role()).isEqualTo(UserModel.Role.ADMIN);
        }
    }
}
