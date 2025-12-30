package com.mopl.security.jwt.registry;

import com.mopl.security.jwt.provider.JwtInformation;

import java.util.Date;
import java.util.UUID;

public interface JwtRegistry {

    void register(JwtInformation jwtInformation);

    void rotate(UUID oldRefreshTokenJti, JwtInformation newJwtInformation);

    boolean isAccessTokenInBlacklist(UUID accessTokenJti);

    boolean isRefreshTokenNotInWhitelist(UUID userId, UUID refreshTokenJti);

    void revokeAccessToken(UUID accessTokenJti, Date expiration);

    void revokeRefreshToken(UUID userId, UUID refreshTokenJti);

    void revokeAllByUserId(UUID userId);

    void clearExpired();
}
