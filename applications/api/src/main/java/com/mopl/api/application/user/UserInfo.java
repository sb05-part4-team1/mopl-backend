package com.mopl.api.application.user;

import com.mopl.domain.model.user.Role;
import com.mopl.domain.model.user.UserModel;

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
    public static UserInfo from(UserModel userModel) {
        return new UserInfo(
            userModel.getId(),
            userModel.getCreatedAt(),
            userModel.getEmail(),
            userModel.getName(),
            userModel.getProfileImageUrl(),
            userModel.getRole(),
            userModel.isLocked()
        );
    }
}
