package com.mopl.security.handler.jwt;

import com.mopl.security.handler.ApiResponseHandler;
import com.mopl.security.provider.jwt.JwtCookieProvider;
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

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProvider tokenProvider;
    private final JwtCookieProvider cookieProvider;
    private final ApiResponseHandler responseWriter;
    private final JwtRegistry jwtRegistry;

    @Override
    public void onAuthenticationSuccess(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull Authentication authentication
    ) throws IOException {
        if (!(authentication.getPrincipal() instanceof MoplUserDetails userDetails)) {
            log.error("인증 실패: 예상치 못한 Principal 타입");
            return;
        }

        try {
            JwtDto jwtDto = generateAndRegisterTokens(userDetails);
            UserDto userDto = userService.findById(userDetails.getUserDetailsDto().id());

            // response.addCookie(cookieProvider.createRefreshTokenCookie(jwtDto.refreshToken()));
            responseWriter.writeSuccess(response, new JwtResponse(userDto, jwtDto.accessToken()));

            eventPublisher.publishEvent(new LoginEvent(
                userDto.id(),
                userDto.username(),
                extractIpAddress(request),
                extractUserAgent(request),
                duration
            ));

            log.info("JWT 토큰 발급 완료: username={}", userDetails.getUsername());
        } catch (Exception e) {
            eventPublisher.publishEvent(new LoginFailureEvent(duration));

            log.error("JWT 토큰 생성 실패: username={}", userDetails.getUsername(), e);
        }
    }

    private JwtDto generateAndRegisterTokens(DiscodeitUserDetails userDetails) {
        String accessToken = tokenProvider.generateAccessToken(userDetails);
        String refreshToken = tokenProvider.generateRefreshToken(userDetails);

        JwtDto jwtDto = new JwtDto(
            userDetails.getUserDetailsDto(),
            accessToken,
            refreshToken
        );

        jwtRegistry.registerJwtInformation(jwtDto);
        return jwtDto;
    }
}
