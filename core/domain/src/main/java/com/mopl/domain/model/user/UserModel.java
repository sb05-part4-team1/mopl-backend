package com.mopl.domain.model.user;

import com.mopl.domain.exception.user.InvalidUserDataException;
import com.mopl.domain.model.base.BaseUpdatableModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder(toBuilder = true)
public class UserModel extends BaseUpdatableModel {

    public static final int AUTH_PROVIDER_MAX_LENGTH = 20;
    public static final int EMAIL_MAX_LENGTH = 255;
    public static final int NAME_MAX_LENGTH = 50;
    public static final int ENCODED_PASSWORD_MAX_LENGTH = 255;
    public static final int RAW_PASSWORD_MAX_LENGTH = 50;
    public static final int PROFILE_IMAGE_PATH_MAX_LENGTH = 1024;
    public static final int ROLE_MAX_LENGTH = 20;

    public enum AuthProvider {
        EMAIL, GOOGLE, KAKAO
    }

    public enum Role {
        USER, ADMIN
    }

    private AuthProvider authProvider;
    private String email;
    private String name;
    private String password;
    private String profileImagePath;
    private Role role;
    private boolean locked;

    public static UserModel create(
        String email,
        String name,
        String encodedPassword
    ) {
        if (email == null || email.isBlank()) {
            throw InvalidUserDataException.withDetailMessage("이메일은 비어있을 수 없습니다.");
        }
        if (name == null || name.isBlank()) {
            throw InvalidUserDataException.withDetailMessage("이름은 비어있을 수 없습니다.");
        }
        if (encodedPassword == null || encodedPassword.isBlank()) {
            throw InvalidUserDataException.withDetailMessage("비밀번호는 비어있을 수 없습니다.");
        }

        validateEmail(email);
        validateName(name);
        validatePassword(encodedPassword);

        return UserModel.builder()
            .authProvider(AuthProvider.EMAIL)
            .email(email)
            .name(name)
            .password(encodedPassword)
            .role(Role.USER)
            .locked(false)
            .build();
    }

    public static UserModel createOAuthUser(
        AuthProvider authProvider,
        String email,
        String name
    ) {
        if (authProvider == null || authProvider == AuthProvider.EMAIL) {
            throw InvalidUserDataException.withDetailMessage("OAuth 회원가입에는 유효한 OAuth 제공자가 필요합니다.");
        }
        if (email == null || email.isBlank()) {
            throw InvalidUserDataException.withDetailMessage("이메일은 비어있을 수 없습니다.");
        }
        if (name == null || name.isBlank()) {
            throw InvalidUserDataException.withDetailMessage("이름은 비어있을 수 없습니다.");
        }

        validateEmail(email);
        validateName(name);

        return UserModel.builder()
            .authProvider(authProvider)
            .email(email)
            .name(name)
            .password(null)
            .profileImagePath(null)
            .role(Role.USER)
            .locked(false)
            .build();
    }

    public UserModel updateName(String newName) {
        if (newName == null || newName.isBlank()) {
            return this;
        }
        validateName(newName);
        return this.toBuilder()
            .name(newName)
            .build();
    }

    public UserModel updatePassword(String newEncodedPassword) {
        if (newEncodedPassword == null || newEncodedPassword.isBlank()) {
            return this;
        }
        validatePassword(newEncodedPassword);
        return this.toBuilder()
            .password(newEncodedPassword)
            .build();
    }

    public UserModel updateProfileImagePath(String newProfileImagePath) {
        if (newProfileImagePath == null) {
            return this;
        }
        validateProfileImagePath(newProfileImagePath);
        return this.toBuilder()
            .profileImagePath(newProfileImagePath)
            .build();
    }

    public UserModel updateRole(Role newRole) {
        if (newRole == null) {
            return this;
        }
        return this.toBuilder()
            .role(newRole)
            .build();
    }

    public UserModel lock() {
        return this.toBuilder()
            .locked(true)
            .build();
    }

    public UserModel unlock() {
        return this.toBuilder()
            .locked(false)
            .build();
    }

    private static void validateEmail(String email) {
        if (email.length() > EMAIL_MAX_LENGTH) {
            throw InvalidUserDataException.withDetailMessage(
                "이메일은 " + EMAIL_MAX_LENGTH + "자를 초과할 수 없습니다.");
        }
    }

    private static void validateName(String username) {
        if (username.length() > NAME_MAX_LENGTH) {
            throw InvalidUserDataException.withDetailMessage(
                "이름은 " + NAME_MAX_LENGTH + "자를 초과할 수 없습니다.");
        }
    }

    private static void validatePassword(String password) {
        if (password.length() > ENCODED_PASSWORD_MAX_LENGTH) {
            throw InvalidUserDataException.withDetailMessage(
                "비밀번호는 " + ENCODED_PASSWORD_MAX_LENGTH + "자를 초과할 수 없습니다.");
        }
    }

    private static void validateProfileImagePath(String profileImagePath) {
        if (profileImagePath.length() > PROFILE_IMAGE_PATH_MAX_LENGTH) {
            throw InvalidUserDataException.withDetailMessage(
                "프로필 이미지 경로는 " + PROFILE_IMAGE_PATH_MAX_LENGTH + "자를 초과할 수 없습니다.");
        }
    }
}
