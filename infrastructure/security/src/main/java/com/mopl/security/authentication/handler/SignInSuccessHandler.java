package com.mopl.security.authentication.handler;

import com.mopl.domain.exception.InternalServerException;
import com.mopl.domain.model.user.UserModel;
import com.mopl.security.exception.ApiResponseHandler;
import com.mopl.security.jwt.provider.JwtCookieProvider;
import com.mopl.security.jwt.provider.JwtInformation;
import com.mopl.security.jwt.provider.JwtProvider;
import com.mopl.security.jwt.registry.JwtRegistry;
import com.mopl.security.userdetails.MoplUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class SignInSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final JwtCookieProvider cookieProvider;
    private final JwtRegistry jwtRegistry;
    private final ApiResponseHandler apiResponseHandler;

    @Override
    public void onAuthenticationSuccess(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull Authentication authentication
    ) throws IOException {
        if (!(authentication.getPrincipal() instanceof MoplUserDetails userDetails)) {
            log.error("인증 실패: 예상치 못한 Principal 타입");
            apiResponseHandler.writeError(response, new InternalServerException());
            return;
        }

        try {
            JwtInformation jwtInformation = jwtProvider.issueTokenPair(
                userDetails.userId(),
                userDetails.role()
            );
            jwtRegistry.register(jwtInformation);

            JwtResponse jwtResponse = JwtResponse.from(userDetails, jwtInformation.accessToken());

            response.addCookie(
                cookieProvider.createRefreshTokenCookie(
                    jwtInformation.refreshToken()
                )
            );
            apiResponseHandler.writeSuccess(response, jwtResponse);

            log.info("JWT 토큰 발급 완료: userId={}", userDetails.userId());
        } catch (Exception e) {
            log.error("JWT 토큰 생성 실패: userId={}", userDetails.userId(), e);
            apiResponseHandler.writeError(response, new InternalServerException());
        }
    }

    private record JwtResponse(UserDetailsDto userDto, String accessToken) {

        public static JwtResponse from(
            MoplUserDetails userDetails,
            String accessToken
        ) {
            return new JwtResponse(UserDetailsDto.from(userDetails), accessToken);
        }

        private record UserDetailsDto(
            UUID id,
            Instant createdAt,
            String email,
            String name,
            String profileImageUrl,
            UserModel.Role role,
            Boolean locked
        ) {

            public static UserDetailsDto from(MoplUserDetails userDetails) {
                return new UserDetailsDto(
                    userDetails.userId(),
                    userDetails.createdAt(),
                    userDetails.email(),
                    userDetails.name(),
                    userDetails.profileImageUrl(),
                    userDetails.role(),
                    userDetails.locked()
                );
            }
        }
    }
}
