package com.mopl.security.authentication.handler;

import com.mopl.domain.exception.InternalServerException;
import com.mopl.domain.model.user.UserModel;
import com.mopl.security.exception.ApiResponseHandler;
import com.mopl.security.jwt.dto.JwtResponse;
import com.mopl.security.jwt.provider.JwtCookieProvider;
import com.mopl.security.jwt.provider.JwtInformation;
import com.mopl.security.jwt.provider.JwtPayload;
import com.mopl.security.jwt.provider.JwtProvider;
import com.mopl.security.jwt.registry.JwtRegistry;
import com.mopl.security.userdetails.MoplUserDetails;
import jakarta.servlet.http.Cookie;
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
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("SignInSuccessHandler 단위 테스트")
class SignInSuccessHandlerTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private JwtCookieProvider cookieProvider;

    @Mock
    private JwtRegistry jwtRegistry;

    @Mock
    private ApiResponseHandler apiResponseHandler;

    @Mock
    private Authentication authentication;

    private SignInSuccessHandler successHandler;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        successHandler = new SignInSuccessHandler(jwtProvider, cookieProvider, jwtRegistry, apiResponseHandler);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Nested
    @DisplayName("onAuthenticationSuccess()")
    class OnAuthenticationSuccessTest {

        @Test
        @DisplayName("인증 성공 시 JWT를 발급하고 쿠키를 설정한다")
        void withValidAuthentication_issuesJwtAndSetsCookie() throws IOException {
            // given
            UUID userId = UUID.randomUUID();
            UserModel.Role role = UserModel.Role.USER;
            MoplUserDetails userDetails = createUserDetails(userId, role);

            JwtInformation jwtInfo = createJwtInformation(userId);
            Cookie refreshCookie = new Cookie("REFRESH_TOKEN", jwtInfo.refreshToken());

            given(authentication.getPrincipal()).willReturn(userDetails);
            given(jwtProvider.issueTokenPair(userId, role)).willReturn(jwtInfo);
            given(cookieProvider.createRefreshTokenCookie(jwtInfo.refreshToken())).willReturn(refreshCookie);

            // when
            successHandler.onAuthenticationSuccess(request, response, authentication);

            // then
            then(jwtRegistry).should().register(jwtInfo);
            assertThat(response.getCookies()).contains(refreshCookie);
            then(apiResponseHandler).should().writeSuccess(eq(response), any(JwtResponse.class));
        }

        @Test
        @DisplayName("ADMIN 역할로 인증 성공 시 JWT를 발급한다")
        void withAdminRole_issuesJwt() throws IOException {
            // given
            UUID userId = UUID.randomUUID();
            UserModel.Role role = UserModel.Role.ADMIN;
            MoplUserDetails userDetails = createUserDetails(userId, role);

            JwtInformation jwtInfo = createJwtInformation(userId);
            Cookie refreshCookie = new Cookie("REFRESH_TOKEN", jwtInfo.refreshToken());

            given(authentication.getPrincipal()).willReturn(userDetails);
            given(jwtProvider.issueTokenPair(userId, role)).willReturn(jwtInfo);
            given(cookieProvider.createRefreshTokenCookie(jwtInfo.refreshToken())).willReturn(refreshCookie);

            // when
            successHandler.onAuthenticationSuccess(request, response, authentication);

            // then
            then(jwtProvider).should().issueTokenPair(userId, role);
        }

        @Test
        @DisplayName("JWT 응답에 사용자 정보가 포함된다")
        void jwtResponse_containsUserInfo() throws IOException {
            // given
            UUID userId = UUID.randomUUID();
            MoplUserDetails userDetails = createUserDetails(userId, UserModel.Role.USER);

            JwtInformation jwtInfo = createJwtInformation(userId);
            Cookie refreshCookie = new Cookie("REFRESH_TOKEN", jwtInfo.refreshToken());

            given(authentication.getPrincipal()).willReturn(userDetails);
            given(jwtProvider.issueTokenPair(any(), any())).willReturn(jwtInfo);
            given(cookieProvider.createRefreshTokenCookie(any())).willReturn(refreshCookie);

            // when
            successHandler.onAuthenticationSuccess(request, response, authentication);

            // then
            ArgumentCaptor<JwtResponse> responseCaptor = ArgumentCaptor.forClass(JwtResponse.class);
            then(apiResponseHandler).should().writeSuccess(any(), responseCaptor.capture());

            JwtResponse capturedResponse = responseCaptor.getValue();
            assertThat(capturedResponse.accessToken()).isEqualTo(jwtInfo.accessToken());
            assertThat(capturedResponse.userDto().id()).isEqualTo(userId);
        }

        @Test
        @DisplayName("Principal이 MoplUserDetails가 아니면 InternalServerException을 발생시킨다")
        void withInvalidPrincipal_throwsException() {
            // given
            given(authentication.getPrincipal()).willReturn("invalid-principal");

            // when & then
            assertThatThrownBy(() -> successHandler.onAuthenticationSuccess(request, response, authentication))
                .isInstanceOf(InternalServerException.class);
        }
    }

    private MoplUserDetails createUserDetails(UUID userId, UserModel.Role role) {
        return MoplUserDetails.builder()
            .userId(userId)
            .role(role)
            .createdAt(Instant.now())
            .password("encoded-password")
            .email("test@example.com")
            .name("Test User")
            .profileImagePath(null)
            .locked(false)
            .build();
    }

    private JwtInformation createJwtInformation(UUID userId) {
        Date now = new Date();
        Date accessExpiry = new Date(now.getTime() + 1800_000);
        Date refreshExpiry = new Date(now.getTime() + 604800_000);

        JwtPayload accessPayload = new JwtPayload(userId, UUID.randomUUID(), now, accessExpiry, UserModel.Role.USER);
        JwtPayload refreshPayload = new JwtPayload(userId, UUID.randomUUID(), now, refreshExpiry, UserModel.Role.USER);

        return new JwtInformation("access-token", "refresh-token", accessPayload, refreshPayload);
    }
}
