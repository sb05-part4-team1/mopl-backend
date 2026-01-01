package com.mopl.jpa.entity.user;

import com.mopl.domain.model.user.UserModel;
import org.springframework.stereotype.Component;

@Component
public class UserEntityMapper {

    public UserModel toModel(UserEntity userEntity) {
        if (userEntity == null) {
            return null;
        }

        return UserModel.builder()
            .id(userEntity.getId())
            .createdAt(userEntity.getCreatedAt())
            .deletedAt(userEntity.getDeletedAt())
            .updatedAt(userEntity.getUpdatedAt())
            .authProvider(userEntity.getAuthProvider())
            .email(userEntity.getEmail())
            .name(userEntity.getName())
            .password(userEntity.getPassword())
            .profileImageUrl(userEntity.getProfileImageUrl())
            .role(userEntity.getRole())
            .locked(userEntity.isLocked())
            .build();
    }

    public UserEntity toEntity(UserModel userModel) {
        if (userModel == null) {
            return null;
        }

        return UserEntity.builder()
            .id(userModel.getId())
            .createdAt(userModel.getCreatedAt())
            .deletedAt(userModel.getDeletedAt())
            .updatedAt(userModel.getUpdatedAt())
            .authProvider(userModel.getAuthProvider())
            .email(userModel.getEmail())
            .name(userModel.getName())
            .password(userModel.getPassword())
            .profileImageUrl(userModel.getProfileImageUrl())
            .role(userModel.getRole())
            .locked(userModel.isLocked())
            .build();
    }
}
