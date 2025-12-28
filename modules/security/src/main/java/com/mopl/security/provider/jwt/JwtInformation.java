package com.mopl.security.provider.jwt;

import java.util.Date;
import java.util.UUID;

public record JwtInformation(
    UUID userId,
    UUID accessTokenJti,
    UUID refreshTokenJti,
    Date accessTokenExpiry,
    Date refreshTokenExpiry
) {
}
