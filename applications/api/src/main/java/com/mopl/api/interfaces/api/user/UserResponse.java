package com.mopl.api.interfaces.api.user;

import com.mopl.domain.model.user.Role;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
    UUID id,
    Instant createdAt,
    String email,
    String name,
    String profileImageUrl,
    Role role,
    boolean locked
) {
}
