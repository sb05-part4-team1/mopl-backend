package com.mopl.jpa.entity.user;

import com.mopl.domain.model.user.AuthProvider;
import com.mopl.domain.model.user.Role;
import com.mopl.domain.model.user.UserModel;
import com.mopl.jpa.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import static com.mopl.domain.model.user.UserModel.AUTH_PROVIDER_MAX_LENGTH;
import static com.mopl.domain.model.user.UserModel.EMAIL_MAX_LENGTH;
import static com.mopl.domain.model.user.UserModel.ENCODED_PASSWORD_MAX_LENGTH;
import static com.mopl.domain.model.user.UserModel.NAME_MAX_LENGTH;
import static com.mopl.domain.model.user.UserModel.PROFILE_IMAGE_URL_MAX_LENGTH;
import static com.mopl.domain.model.user.UserModel.ROLE_MAX_LENGTH;

@Entity
@Table(name = "users")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseUpdatableEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = AUTH_PROVIDER_MAX_LENGTH)
    private AuthProvider authProvider;

    @Column(nullable = false, unique = true, length = EMAIL_MAX_LENGTH)
    private String email;

    @Column(nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    @Column(nullable = false, length = ENCODED_PASSWORD_MAX_LENGTH)
    private String password;

    @Column(length = PROFILE_IMAGE_URL_MAX_LENGTH)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = ROLE_MAX_LENGTH)
    private Role role;

    @Column(nullable = false)
    private boolean locked;

    public static UserEntity from(UserModel userModel) {
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

    public UserModel toModel() {
        return UserModel.builder()
            .id(getId())
            .createdAt(getCreatedAt())
            .deletedAt(getDeletedAt())
            .updatedAt(getUpdatedAt())
            .authProvider(authProvider)
            .email(email)
            .name(name)
            .password(password)
            .profileImageUrl(profileImageUrl)
            .role(role)
            .locked(locked)
            .build();
    }
}
