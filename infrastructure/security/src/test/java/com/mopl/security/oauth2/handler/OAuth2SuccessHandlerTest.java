package com.mopl.security.oauth2.handler;

import com.mopl.domain.model.user.UserModel;
import com.mopl.security.config.OAuth2Properties;
import com.mopl.security.jwt.provider.JwtCookieProvider;
import com.mopl.security.jwt.provider.JwtInformation;
import com.mopl.security.jwt.provider.JwtPayload;
import com.mopl.security.jwt.provider.JwtProvider;
import com.mopl.security.jwt.registry.JwtRegistry;
import com.mopl.security.oauth2.OAuth2UserPrincipal;
import com.mopl.security.userdetails.MoplUserDetails;
import jakarta.servlet.http.Cookie;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.web.RedirectStrategy;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2SuccessHandler 단위 테스트")
class OAuth2SuccessHandlerTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private JwtCookieProvider cookieProvider;

    @Mock
    private JwtRegistry jwtRegistry;

    @Mock
    private RedirectStrategy redirectStrategy;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    private OAuth2SuccessHandler successHandler;

    private static final String FRONTEND_REDIRECT_URI = "http://localhost:8080";

    @BeforeEach
    void setUp() {
        OAuth2Properties oAuth2Properties = new OAuth2Properties(FRONTEND_REDIRECT_URI);
        successHandler = new OAuth2SuccessHandler(
            oAuth2Properties,
            jwtProvider,
            cookieProvider,
            jwtRegistry
        );
        successHandler.setRedirectStrategy(redirectStrategy);
    }

    @Nested
    @DisplayName("onAuthenticationSuccess()")
    class OnAuthenticationSuccessTest {

        @Test
        @DisplayName("OAuth2 인증 성공 시 JWT 발급 및 쿠키 설정 후 /contents로 리다이렉트")
        void withValidAuthentication_issuesJwtAndRedirectsToContents() throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            UserModel.Role role = UserModel.Role.USER;
            MoplUserDetails userDetails = createUserDetails(userId, role);
            OAuth2UserPrincipal principal = new OAuth2UserPrincipal(userDetails, Map.of());

            String accessToken = "access-token";
            String refreshToken = "refresh-token";
            JwtInformation jwtInformation = createJwtInformation(userId, accessToken, refreshToken);
            Cookie refreshTokenCookie = new Cookie("REFRESH_TOKEN", refreshToken);

            given(authentication.getPrincipal()).willReturn(principal);
            given(jwtProvider.issueTokenPair(userId, role)).willReturn(jwtInformation);
            given(cookieProvider.createRefreshTokenCookie(refreshToken)).willReturn(
                refreshTokenCookie);

            // when
            successHandler.onAuthenticationSuccess(request, response, authentication);

            // then
            then(jwtProvider).should().issueTokenPair(userId, role);
            then(jwtRegistry).should().register(jwtInformation);
            then(response).should().addCookie(refreshTokenCookie);

            ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
            then(redirectStrategy).should().sendRedirect(eq(request), eq(response), urlCaptor
                .capture());

            String redirectUrl = urlCaptor.getValue();
            assertThat(redirectUrl).isEqualTo(FRONTEND_REDIRECT_URI + "/#/contents");
            assertThat(redirectUrl).doesNotContain("token=");
        }

        @Test
        @DisplayName("관리자 역할의 OAuth2 인증 성공 시에도 동일하게 처리")
        void withAdminRole_issuesJwtAndRedirectsToContents() throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            UserModel.Role role = UserModel.Role.ADMIN;
            MoplUserDetails userDetails = createUserDetails(userId, role);
            OAuth2UserPrincipal principal = new OAuth2UserPrincipal(userDetails, Map.of());

            String accessToken = "admin-access-token";
            String refreshToken = "admin-refresh-token";
            JwtInformation jwtInformation = createJwtInformation(userId, accessToken, refreshToken);
            Cookie refreshTokenCookie = new Cookie("REFRESH_TOKEN", refreshToken);

            given(authentication.getPrincipal()).willReturn(principal);
            given(jwtProvider.issueTokenPair(userId, role)).willReturn(jwtInformation);
            given(cookieProvider.createRefreshTokenCookie(refreshToken)).willReturn(
                refreshTokenCookie);

            // when
            successHandler.onAuthenticationSuccess(request, response, authentication);

            // then
            then(jwtProvider).should().issueTokenPair(userId, role);
            then(jwtRegistry).should().register(jwtInformation);

            ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
            then(redirectStrategy).should().sendRedirect(any(), any(), urlCaptor.capture());

            assertThat(urlCaptor.getValue()).isEqualTo(FRONTEND_REDIRECT_URI + "/#/contents");
        }

        @Test
        @DisplayName("리다이렉트 URL에 access token이 포함되지 않음")
        void redirectUrl_doesNotContainAccessToken() throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            MoplUserDetails userDetails = createUserDetails(userId, UserModel.Role.USER);
            OAuth2UserPrincipal principal = new OAuth2UserPrincipal(userDetails, Map.of());

            String accessToken = "secret-access-token";
            JwtInformation jwtInformation = createJwtInformation(userId, accessToken, "refresh");
            Cookie cookie = new Cookie("REFRESH_TOKEN", "refresh");

            given(authentication.getPrincipal()).willReturn(principal);
            given(jwtProvider.issueTokenPair(any(), any())).willReturn(jwtInformation);
            given(cookieProvider.createRefreshTokenCookie(any())).willReturn(cookie);

            // when
            successHandler.onAuthenticationSuccess(request, response, authentication);

            // then
            ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
            then(redirectStrategy).should().sendRedirect(any(), any(), urlCaptor.capture());

            String redirectUrl = urlCaptor.getValue();
            assertThat(redirectUrl).doesNotContain(accessToken);
            assertThat(redirectUrl).doesNotContain("token");
            assertThat(redirectUrl).doesNotContain("access");
        }
    }

    private MoplUserDetails createUserDetails(UUID userId, UserModel.Role role) {
        return MoplUserDetails.builder()
            .userId(userId)
            .role(role)
            .createdAt(Instant.now())
            .password("{oauth2}")
            .email("test@example.com")
            .name("Test User")
            .profileImagePath(null)
            .locked(false)
            .build();
    }

    private JwtInformation createJwtInformation(UUID userId, String accessToken,
        String refreshToken) {
        Date now = new Date();
        Date accessExpiry = new Date(now.getTime() + 3600_000);
        Date refreshExpiry = new Date(now.getTime() + 86400_000);

        JwtPayload accessPayload = new JwtPayload(
            userId,
            UUID.randomUUID(),
            now,
            accessExpiry,
            UserModel.Role.USER
        );
        JwtPayload refreshPayload = new JwtPayload(
            userId,
            UUID.randomUUID(),
            now,
            refreshExpiry,
            UserModel.Role.USER
        );
        return new JwtInformation(accessToken, refreshToken, accessPayload, refreshPayload);
    }
}
