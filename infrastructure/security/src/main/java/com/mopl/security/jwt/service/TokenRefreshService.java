package com.mopl.security.jwt.service;

import com.mopl.domain.exception.auth.AccountLockedException;
import com.mopl.domain.exception.auth.InvalidTokenException;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.service.user.UserService;
import com.mopl.logging.context.LogContext;
import com.mopl.security.jwt.dto.JwtResponse;
import com.mopl.security.jwt.provider.JwtCookieProvider;
import com.mopl.security.jwt.provider.JwtInformation;
import com.mopl.security.jwt.provider.JwtPayload;
import com.mopl.security.jwt.provider.JwtProvider;
import com.mopl.security.jwt.provider.TokenType;
import com.mopl.security.jwt.registry.JwtRegistry;
import com.mopl.security.userdetails.MoplUserDetails;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TokenRefreshService {

    private final JwtProvider jwtProvider;
    private final JwtCookieProvider cookieProvider;
    private final JwtRegistry jwtRegistry;
    private final UserService userService;

    public TokenRefreshResult refresh(String refreshToken) {
        JwtPayload payload = jwtProvider.verifyAndParse(refreshToken, TokenType.REFRESH);

        if (jwtRegistry.isRefreshTokenNotInWhitelist(payload.sub(), payload.jti())) {
            throw InvalidTokenException.create();
        }

        UserModel user = userService.getById(payload.sub());

        if (user.isLocked()) {
            throw AccountLockedException.withId(user.getId());
        }

        MoplUserDetails userDetails = MoplUserDetails.from(user);

        JwtInformation newJwtInformation = jwtProvider.issueTokenPair(
            userDetails.userId(),
            userDetails.role()
        );
        jwtRegistry.rotate(payload.jti(), newJwtInformation);

        Cookie refreshTokenCookie = cookieProvider.createRefreshTokenCookie(
            newJwtInformation.refreshToken()
        );
        JwtResponse jwtResponse = JwtResponse.from(userDetails, newJwtInformation.accessToken());

        LogContext.with("userId", userDetails.userId()).debug("Token refreshed");

        return new TokenRefreshResult(jwtResponse, refreshTokenCookie);
    }

    public record TokenRefreshResult(JwtResponse jwtResponse, Cookie refreshTokenCookie) {
    }
}
