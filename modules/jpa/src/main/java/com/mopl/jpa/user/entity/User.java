package com.mopl.jpa.user.entity;

import com.mopl.jpa.global.auditing.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseUpdatableEntity {

    public static final int AUTH_PROVIDER_MAX_LENGTH = 20;
    public static final int EMAIL_MAX_LENGTH = 255;
    public static final int NAME_MAX_LENGTH = 50;
    public static final int ENCODED_PASSWORD_MAX_LENGTH = 255;
    public static final int RAW_PASSWORD_MAX_LENGTH = 50;
    public static final int PROFILE_IMAGE_URL_MAX_LENGTH = 1024;

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
    @Column(nullable = false)
    private Role role;

    @Column
    private boolean locked;

    public User(
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

        this.authProvider = authProvider;
        this.email = email;
        this.name = name;
        this.password = password;
    }

    public User updatePassword(String newEncodedPassword) {
        if (newEncodedPassword != null && !newEncodedPassword.isBlank()) {
            validatePassword(newEncodedPassword);
            this.password = newEncodedPassword;
        }
        return this;
    }

    public User updateProfileImageUrl(String newProfileImageUrl) {
        if (newProfileImageUrl != null) {
            validateProfileImageUrl(newProfileImageUrl);
            this.profileImageUrl = newProfileImageUrl;
        }
        return this;
    }

    public User updateRole(Role newRole) {
        if (newRole != null) {
            this.role = newRole;
        }
        return this;
    }

    private void validateEmail(String email) {
        if (email.length() > EMAIL_MAX_LENGTH) {
            throw new IllegalArgumentException(
                "email must not exceed " + EMAIL_MAX_LENGTH);
        }
    }

    private void validateName(String username) {
        if (username.length() > NAME_MAX_LENGTH) {
            throw new IllegalArgumentException(
                "username must not exceed " + NAME_MAX_LENGTH);
        }
    }

    private void validatePassword(String password) {
        if (password.length() > ENCODED_PASSWORD_MAX_LENGTH) {
            throw new IllegalArgumentException(
                "encoded password must not exceed " + ENCODED_PASSWORD_MAX_LENGTH);
        }
    }

    private void validateProfileImageUrl(String profileImageUrl) {
        if (profileImageUrl.length() > PROFILE_IMAGE_URL_MAX_LENGTH) {
            throw new IllegalArgumentException(
                "profile image url must not exceed " + PROFILE_IMAGE_URL_MAX_LENGTH);
        }
    }
}
