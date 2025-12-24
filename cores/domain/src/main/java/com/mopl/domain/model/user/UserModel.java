package com.mopl.domain.model.user;

import com.mopl.domain.exception.user.InvalidUserDataException;
import com.mopl.domain.model.base.BaseUpdatableModel;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
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

    public static UserModel create(
        AuthProvider authProvider,
        String email,
        String name,
        String password
    ) {
        if (authProvider == null) {
            throw new InvalidUserDataException("회원가입 경로는 null일 수 없습니다.");
        }
        if (email == null || email.isBlank()) {
            throw new InvalidUserDataException("이메일은 비어있을 수 없습니다.");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidUserDataException("이름은 비어있을 수 없습니다.");
        }
        if (password == null || password.isBlank()) {
            throw new InvalidUserDataException("비밀번호는 비어있을 수 없습니다.");
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
            throw new InvalidUserDataException(
                "이메일은 " + EMAIL_MAX_LENGTH + "자를 초과할 수 없습니다.");
        }
    }

    private static void validateName(String username) {
        if (username.length() > NAME_MAX_LENGTH) {
            throw new InvalidUserDataException(
                "이름은 " + NAME_MAX_LENGTH + "자를 초과할 수 없습니다.");
        }
    }

    private static void validatePassword(String password) {
        if (password.length() > ENCODED_PASSWORD_MAX_LENGTH) {
            throw new InvalidUserDataException(
                "비밀번호는 " + ENCODED_PASSWORD_MAX_LENGTH + "자를 초과할 수 없습니다.");
        }
    }

    private static void validateProfileImageUrl(String profileImageUrl) {
        if (profileImageUrl.length() > PROFILE_IMAGE_URL_MAX_LENGTH) {
            throw new InvalidUserDataException(
                "프로필 이미지 URL은 " + PROFILE_IMAGE_URL_MAX_LENGTH + "자를 초과할 수 없습니다.");
        }
    }
}
