package com.mopl.api.application.user;

import com.mopl.domain.model.user.Role;

import java.time.Instant;
import java.util.UUID;

public record UserInfo(
    UUID id,
    Instant createdAt,
    String email,
    String name,
    String profileImageUrl,
    Role role,
    boolean locked
) {
}
