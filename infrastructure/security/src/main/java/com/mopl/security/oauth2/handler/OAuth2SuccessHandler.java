package com.mopl.security.oauth2.handler;

import com.mopl.security.config.OAuth2Properties;
import com.mopl.security.jwt.provider.JwtCookieProvider;
import com.mopl.security.jwt.provider.JwtInformation;
import com.mopl.security.jwt.provider.JwtProvider;
import com.mopl.security.jwt.registry.JwtRegistry;
import com.mopl.security.oauth2.OAuth2UserPrincipal;
import com.mopl.security.userdetails.MoplUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final OAuth2Properties oAuth2Properties;
    private final JwtProvider jwtProvider;
    private final JwtCookieProvider cookieProvider;
    private final JwtRegistry jwtRegistry;

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException {
        OAuth2UserPrincipal principal = (OAuth2UserPrincipal) authentication.getPrincipal();
        MoplUserDetails userDetails = principal.getUserDetails();

        JwtInformation jwtInformation = jwtProvider.issueTokenPair(
            userDetails.userId(),
            userDetails.role()
        );
        jwtRegistry.register(jwtInformation);

        response.addCookie(cookieProvider.createRefreshTokenCookie(jwtInformation.refreshToken()));

        String redirectUrl = buildRedirectUrl();
        log.info("OAuth2 로그인 성공: userId={}", userDetails.userId());

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private String buildRedirectUrl() {
        return UriComponentsBuilder.fromUriString(oAuth2Properties.frontendRedirectUri())
            .path("/#/contents")
            .build()
            .toUriString();
    }
}
