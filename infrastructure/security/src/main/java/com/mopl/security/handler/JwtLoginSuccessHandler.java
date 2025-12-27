package com.mopl.security.handler;

import com.mopl.domain.service.user.UserService;
import com.mopl.security.provider.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final JwtCookieProvider cookieProvider;
    private final JwtResponseWriter responseWriter;
    private final JwtRegistry jwtRegistry;
    private final UserService userService;

    @Override
    public void onAuthenticationSuccess(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull Authentication authentication
    ) throws IOException {
        if (!(authentication.getPrincipal() instanceof DiscodeitUserDetails userDetails)) {
            log.error("인증 실패: 예상치 못한 Principal 타입");
            return;
        }

        String startTimeStr = MDC.get(KEY_REQUEST_START_TIME);
        long duration = -1L;
        try {
            duration = System.currentTimeMillis() - Long.parseLong(startTimeStr);
        } catch (NumberFormatException | NullPointerException e) {
            log.warn("로그인 감사: 로그인 시작 시간 파싱 실패", e);
        }

        try {
            JwtDto jwtDto = generateAndRegisterTokens(userDetails);
            UserDto userDto = userService.findById(userDetails.getUserDetailsDto().id());

            response.addCookie(cookieProvider.createRefreshTokenCookie(jwtDto.refreshToken()));
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
