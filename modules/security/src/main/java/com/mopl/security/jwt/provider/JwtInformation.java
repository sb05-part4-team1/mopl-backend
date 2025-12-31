package com.mopl.security.jwt.provider;

import java.util.UUID;

public record JwtInformation(
    String accessToken,
    String refreshToken,
    JwtPayload accessTokenPayload,
    JwtPayload refreshTokenPayload
) {

    public UUID userId() {
        return accessTokenPayload.sub();
    }

    public UUID refreshTokenJti() {
        return refreshTokenPayload.jti();
    }
}
