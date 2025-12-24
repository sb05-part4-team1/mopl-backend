package com.mopl.domain.model.user;

import com.mopl.domain.model.base.BaseUpdatableModel;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class UserModel extends BaseUpdatableModel {

    public static final int AUTH_PROVIDER_MAX_LENGTH = 20;
    public static final int EMAIL_MAX_LENGTH = 255;
    public static final int NAME_MAX_LENGTH = 50;
    public static final int ENCODED_PASSWORD_MAX_LENGTH = 255;
    public static final int RAW_PASSWORD_MAX_LENGTH = 50;
    public static final int PROFILE_IMAGE_URL_MAX_LENGTH = 1024;

    private final AuthProvider authProvider;

    private final String email;

    private final String name;

    private String password;

    private String profileImageUrl;

    private Role role;

    private boolean locked;

    private UserModel(
        AuthProvider authProvider,
        String email,
        String name,
        String password
    ) {
        super();
        this.authProvider = authProvider;
        this.email = email;
        this.name = name;
        this.password = password;
        this.role = Role.USER;
        this.locked = false;
    }

    public UserModel(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt,
        AuthProvider authProvider,
        String email,
        String name,
        String password,
        String profileImageUrl,
        Role role,
        boolean locked
    ) {
        super(id, createdAt, deletedAt, updatedAt);
        this.authProvider = authProvider;
        this.email = email;
        this.name = name;
        this.password = password;
        this.profileImageUrl = profileImageUrl;
        this.role = role;
        this.locked = locked;
    }

    public static UserModel create(
        AuthProvider authProvider,
        String email,
        String name,
        String password
    ) {
        if (authProvider == null) {
            throw new IllegalArgumentException("회원가입 경로는 null일 수 없습니다.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일은 비어있을 수 없습니다.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 비어있을 수 없습니다.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 비어있을 수 없습니다.");
        }

        validateEmail(email);
        validateName(name);
        validatePassword(password);

        return new UserModel(
            authProvider,
            email,
            name,
            password
        );
    }

    public UserModel updatePassword(String newEncodedPassword) {
        if (newEncodedPassword != null && !newEncodedPassword.isBlank()) {
            validatePassword(newEncodedPassword);
            this.password = newEncodedPassword;
        }
        return this;
    }

    public UserModel updateProfileImageUrl(String newProfileImageUrl) {
        if (newProfileImageUrl != null) {
            validateProfileImageUrl(newProfileImageUrl);
            this.profileImageUrl = newProfileImageUrl;
        }
        return this;
    }

    public UserModel updateRole(Role newRole) {
        if (newRole != null) {
            this.role = newRole;
        }
        return this;
    }

    public UserModel lock() {
        this.locked = true;
        return this;
    }

    public UserModel unlock() {
        this.locked = false;
        return this;
    }

    private static void validateEmail(String email) {
        if (email.length() > EMAIL_MAX_LENGTH) {
            throw new IllegalArgumentException(
                "email must not exceed " + EMAIL_MAX_LENGTH);
        }
    }

    private static void validateName(String username) {
        if (username.length() > NAME_MAX_LENGTH) {
            throw new IllegalArgumentException(
                "username must not exceed " + NAME_MAX_LENGTH);
        }
    }

    private static void validatePassword(String password) {
        if (password.length() > ENCODED_PASSWORD_MAX_LENGTH) {
            throw new IllegalArgumentException(
                "encoded password must not exceed " + ENCODED_PASSWORD_MAX_LENGTH);
        }
    }

    private static void validateProfileImageUrl(String profileImageUrl) {
        if (profileImageUrl.length() > PROFILE_IMAGE_URL_MAX_LENGTH) {
            throw new IllegalArgumentException(
                "profile image url must not exceed " + PROFILE_IMAGE_URL_MAX_LENGTH);
        }
    }
}
