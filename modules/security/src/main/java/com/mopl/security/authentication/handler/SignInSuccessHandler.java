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
        MoplUserDetails userDetails = extractUserDetails(authentication);
        JwtInformation jwtInformation = issueAndRegisterToken(userDetails);

        response.addCookie(cookieProvider.createRefreshTokenCookie(jwtInformation.refreshToken()));
        apiResponseHandler.writeSuccess(
            response,
            JwtResponse.from(userDetails, jwtInformation.accessToken())
        );

        log.info("JWT 토큰 발급 완료: userId={}", userDetails.userId());
    }

    private MoplUserDetails extractUserDetails(Authentication authentication) {
        if (authentication.getPrincipal() instanceof MoplUserDetails userDetails) {
            return userDetails;
        }
        log.error("JWT 발급 실패: 예상치 못한 Principal 타입={}", authentication.getPrincipal().getClass());
        throw new InternalServerException();
    }

    private JwtInformation issueAndRegisterToken(MoplUserDetails userDetails) {
        JwtInformation jwtInformation = jwtProvider.issueTokenPair(
            userDetails.userId(),
            userDetails.role()
        );
        jwtRegistry.register(jwtInformation);
        return jwtInformation;
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
