package com.mopl.security.handler.jwt;

import com.mopl.domain.model.user.UserModel;
import com.mopl.security.handler.ApiResponseHandler;
import com.mopl.security.provider.jwt.JwtCookieProvider;
import com.mopl.security.provider.jwt.JwtInformation;
import com.mopl.security.provider.jwt.JwtProvider;
import com.mopl.security.provider.jwt.MoplUserDetails;
import com.mopl.security.provider.jwt.registry.JwtRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final JwtCookieProvider cookieProvider;
    private final JwtRegistry jwtRegistry;
    private final ApiResponseHandler apiResponseHandler;

    @Override
    public void onAuthenticationSuccess(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull Authentication authentication
    ) {
        if (!(authentication.getPrincipal() instanceof MoplUserDetails userDetails)) {
            log.error("인증 실패: 예상치 못한 Principal 타입");
            return;
        }

        try {
            JwtInformation jwtInformation = jwtProvider.issueTokenPair(
                userDetails.userId(),
                userDetails.role()
            );
            jwtRegistry.register(jwtInformation);

            UserDetailsDto userDetailsDto = new UserDetailsDto(
                userDetails.userId(),
                userDetails.createdAt(),
                userDetails.email(),
                userDetails.name(),
                userDetails.profileImageUrl(),
                userDetails.role(),
                userDetails.locked()
            );

            JwtResponse jwtResponse = new JwtResponse(
                userDetailsDto,
                jwtInformation.accessToken()
            );

            response.addCookie(
                cookieProvider.createRefreshTokenCookie(
                    jwtInformation.refreshToken()
                )
            );
            apiResponseHandler.writeSuccess(response, jwtResponse);

            log.info("JWT 토큰 발급 완료: username={}", userDetails.getUsername());
        } catch (Exception e) {
            log.error("JWT 토큰 생성 실패: username={}", userDetails.getUsername(), e);
        }
    }

    private record JwtResponse(UserDetailsDto userDto, String accessToken) {
    }

    private record UserDetailsDto(
        UUID id,
        Instant createdAt,
        String email,
        String name,
        String profileImageUrl,
        UserModel.Role role,
        boolean locked
    ) {
    }
}
