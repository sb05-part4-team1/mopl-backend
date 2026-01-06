package com.mopl.security.jwt.service;

import com.mopl.domain.exception.auth.AccountLockedException;
import com.mopl.domain.exception.auth.InvalidTokenException;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.service.user.UserService;
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
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class TokenRefreshService {

    private final JwtProvider jwtProvider;
    private final JwtCookieProvider cookieProvider;
    private final JwtRegistry jwtRegistry;
    private final UserService userService;

    public TokenRefreshResult refresh(String refreshToken) {
        JwtPayload payload = jwtProvider.verifyAndParse(refreshToken, TokenType.REFRESH);

        if (jwtRegistry.isRefreshTokenNotInWhitelist(payload.sub(), payload.jti())) {
            log.debug("리프레시 토큰이 화이트리스트에 없음: userId={}, jti={}", payload.sub(), payload.jti());
            throw new InvalidTokenException();
        }

        UserModel user = userService.getById(payload.sub());

        if (user.isLocked()) {
            log.debug("차단된 사용자의 토큰 갱신 시도: userId={}", payload.sub());
            throw new AccountLockedException();
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

        log.info("토큰 갱신 완료: userId={}", userDetails.userId());

        return new TokenRefreshResult(jwtResponse, refreshTokenCookie);
    }

    public record TokenRefreshResult(JwtResponse jwtResponse, Cookie refreshTokenCookie) {
    }
}
