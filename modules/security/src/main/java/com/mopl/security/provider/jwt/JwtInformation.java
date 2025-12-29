package com.mopl.security.provider.jwt;

public record JwtInformation(
    JwtPayload accessTokenPayload,
    JwtPayload refreshTokenPayload
) {
}
