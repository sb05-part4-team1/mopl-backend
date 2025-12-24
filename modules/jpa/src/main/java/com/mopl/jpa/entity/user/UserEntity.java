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

import java.time.Instant;
import java.util.UUID;

import static com.mopl.domain.model.user.UserModel.EMAIL_MAX_LENGTH;
import static com.mopl.domain.model.user.UserModel.ENCODED_PASSWORD_MAX_LENGTH;
import static com.mopl.domain.model.user.UserModel.NAME_MAX_LENGTH;
import static com.mopl.domain.model.user.UserModel.PROFILE_IMAGE_URL_MAX_LENGTH;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseUpdatableEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
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
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean locked;

    public UserEntity(
        UUID id,
        Instant createdAt,
        Instant deletedAt,
        Instant updatedAt,
        AuthProvider authProvider,
        String email,
        String name,
        String password,
        String profileImageUrl,
        Role role,
        boolean locked
    ) {
        super(id, createdAt, updatedAt, deletedAt);
        this.authProvider = authProvider;
        this.email = email;
        this.name = name;
        this.password = password;
        this.profileImageUrl = profileImageUrl;
        this.role = role;
        this.locked = locked;
    }

    public static UserEntity from(UserModel userModel) {
        return new UserEntity(
            userModel.getId(),
            userModel.getCreatedAt(),
            userModel.getDeletedAt(),
            userModel.getUpdatedAt(),
            userModel.getAuthProvider(),
            userModel.getEmail(),
            userModel.getName(),
            userModel.getPassword(),
            userModel.getProfileImageUrl(),
            userModel.getRole(),
            userModel.isLocked()
        );
    }

    public UserModel toModel() {
        return new UserModel(
            getId(),
            getCreatedAt(),
            getUpdatedAt(),
            getDeletedAt(),
            authProvider,
            email,
            name,
            password,
            profileImageUrl,
            role,
            locked
        );
    }
}
