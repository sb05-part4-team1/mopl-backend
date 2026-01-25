package com.mopl.dto.user;

import com.mopl.domain.model.user.UserModel;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
    UUID id,
    Instant createdAt,
    String email,
    String name,
    String profileImageUrl,
    UserModel.Role role,
    boolean locked
) {
}
