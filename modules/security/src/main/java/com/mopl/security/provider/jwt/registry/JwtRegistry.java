package com.mopl.security.provider.jwt.registry;

import com.mopl.security.provider.jwt.JwtInformation;

import java.util.UUID;

public interface JwtRegistry {

    void register(JwtInformation jwtInformation);

    void rotate(UUID oldRefreshTokenJti, JwtInformation newJwtInformation);

    boolean isAccessTokenBlacklisted(UUID accessTokenJti);

    boolean isRefreshTokenValid(UUID userId, UUID refreshTokenJti);

    void revokeTokenPair(JwtInformation jwtInformation);

    void revokeAllByUserId(UUID userId);

    void clearExpired();
}
