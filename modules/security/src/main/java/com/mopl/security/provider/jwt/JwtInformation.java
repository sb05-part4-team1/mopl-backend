package com.mopl.security.provider.jwt;

public record JwtInformation(
    JwtPayload payload,
    String accessToken,
    String refreshToken
) {
}
