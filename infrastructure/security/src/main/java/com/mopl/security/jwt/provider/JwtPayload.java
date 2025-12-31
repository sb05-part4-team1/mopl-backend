package com.mopl.security.jwt.provider;

import com.mopl.domain.model.user.UserModel;

import java.util.Date;
import java.util.UUID;

public record JwtPayload(
    UUID sub,
    UUID jti,
    Date iat,
    Date exp,
    UserModel.Role role
) {
}
