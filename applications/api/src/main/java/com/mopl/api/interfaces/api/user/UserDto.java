package com.mopl.api.interfaces.api.user;

import com.mopl.domain.model.user.Role;
import com.mopl.domain.model.user.UserModel;

import java.time.Instant;
import java.util.UUID;

public record UserDto(
    UUID id,
    Instant createdAt,
    String email,
    String name,
    String profileImageUrl,
    Role role,
    boolean locked
) {

    public static UserDto from(UserModel userModel) {
        return new UserDto(
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
