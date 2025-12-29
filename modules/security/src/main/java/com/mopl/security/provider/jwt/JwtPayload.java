package com.mopl.security.provider.jwt;

import java.util.Date;
import java.util.UUID;

public record JwtPayload(
    UUID sub,
    UUID jti,
    Date ist,
    Date exp
) {
}
