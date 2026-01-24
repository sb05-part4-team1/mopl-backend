package com.mopl.domain.model.user;

import com.mopl.domain.exception.user.InvalidUserDataException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static com.mopl.domain.model.user.UserModel.EMAIL_MAX_LENGTH;
import static com.mopl.domain.model.user.UserModel.ENCODED_PASSWORD_MAX_LENGTH;
import static com.mopl.domain.model.user.UserModel.NAME_MAX_LENGTH;
import static com.mopl.domain.model.user.UserModel.PROFILE_IMAGE_PATH_MAX_LENGTH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("UserModel 단위 테스트")
class UserModelTest {

    private static final String DEFAULT_EMAIL = "test@example.com";
    private static final String DEFAULT_NAME = "홍길동";
    private static final String DEFAULT_PASSWORD = "password";

    static Stream<Arguments> blankStringProvider() {
        return Stream.of(
            Arguments.of("null", null),
            Arguments.of("빈 문자열", ""),
            Arguments.of("공백만", "   ")
        );
    }

    private static UserModel createDefaultUser() {
        return UserModel.create(DEFAULT_EMAIL, DEFAULT_NAME, DEFAULT_PASSWORD);
    }

    @Nested
    @DisplayName("create()")
    class CreateTest {

        @Test
        @DisplayName("유효한 데이터로 UserModel 생성")
        void withValidData_createsUserModel() {
            // when
            UserModel user = UserModel.create(
                "test@example.com",
                "홍길동",
                "encodedPassword123"
            );

            // then
            assertThat(user.getAuthProvider()).isEqualTo(UserModel.AuthProvider.EMAIL);
            assertThat(user.getEmail()).isEqualTo("test@example.com");
            assertThat(user.getName()).isEqualTo("홍길동");
            assertThat(user.getPassword()).isEqualTo("encodedPassword123");
            assertThat(user.getRole()).isEqualTo(UserModel.Role.USER);
            assertThat(user.isLocked()).isFalse();
            assertThat(user.getProfileImagePath()).isNull();
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("com.mopl.domain.model.user.UserModelTest#blankStringProvider")
        @DisplayName("이메일이 비어있으면 예외 발생")
        void withBlankEmail_throwsException(String description, String email) {
            assertThatThrownBy(() -> UserModel.create(email, DEFAULT_NAME, DEFAULT_PASSWORD))
                .isInstanceOf(InvalidUserDataException.class);
        }

        @Test
        @DisplayName("이메일이 정확히 최대 길이면 생성 성공")
        void withEmailAtMaxLength_createsUserModel() {
            String maxEmail = "a".repeat(EMAIL_MAX_LENGTH);

            UserModel user = UserModel.create(maxEmail, DEFAULT_NAME, DEFAULT_PASSWORD);

            assertThat(user.getEmail()).isEqualTo(maxEmail);
        }

        @Test
        @DisplayName("이메일이 최대 길이 초과하면 예외 발생")
        void withEmailExceedingMaxLength_throwsException() {
            String longEmail = "a".repeat(EMAIL_MAX_LENGTH + 1);

            assertThatThrownBy(() -> UserModel.create(longEmail, DEFAULT_NAME, DEFAULT_PASSWORD))
                .isInstanceOf(InvalidUserDataException.class);
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("com.mopl.domain.model.user.UserModelTest#blankStringProvider")
        @DisplayName("이름이 비어있으면 예외 발생")
        void withBlankName_throwsException(String description, String name) {
            assertThatThrownBy(() -> UserModel.create(DEFAULT_EMAIL, name, DEFAULT_PASSWORD))
                .isInstanceOf(InvalidUserDataException.class);
        }

        @Test
        @DisplayName("이름이 정확히 최대 길이면 생성 성공")
        void withNameAtMaxLength_createsUserModel() {
            String maxName = "가".repeat(NAME_MAX_LENGTH);

            UserModel user = UserModel.create(DEFAULT_EMAIL, maxName, DEFAULT_PASSWORD);

            assertThat(user.getName()).isEqualTo(maxName);
        }

        @Test
        @DisplayName("이름이 최대 길이 초과하면 예외 발생")
        void withNameExceedingMaxLength_throwsException() {
            String longName = "가".repeat(NAME_MAX_LENGTH + 1);

            assertThatThrownBy(() -> UserModel.create(DEFAULT_EMAIL, longName, DEFAULT_PASSWORD))
                .isInstanceOf(InvalidUserDataException.class);
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("com.mopl.domain.model.user.UserModelTest#blankStringProvider")
        @DisplayName("비밀번호가 비어있으면 예외 발생")
        void withBlankPassword_throwsException(String description, String password) {
            assertThatThrownBy(() -> UserModel.create(DEFAULT_EMAIL, DEFAULT_NAME, password))
                .isInstanceOf(InvalidUserDataException.class);
        }

        @Test
        @DisplayName("비밀번호가 정확히 최대 길이면 생성 성공")
        void withPasswordAtMaxLength_createsUserModel() {
            String maxPassword = "a".repeat(ENCODED_PASSWORD_MAX_LENGTH);

            UserModel user = UserModel.create(DEFAULT_EMAIL, DEFAULT_NAME, maxPassword);

            assertThat(user.getPassword()).isEqualTo(maxPassword);
        }

        @Test
        @DisplayName("비밀번호가 최대 길이 초과하면 예외 발생")
        void withPasswordExceedingMaxLength_throwsException() {
            String longPassword = "a".repeat(ENCODED_PASSWORD_MAX_LENGTH + 1);

            assertThatThrownBy(() -> UserModel.create(DEFAULT_EMAIL, DEFAULT_NAME, longPassword))
                .isInstanceOf(InvalidUserDataException.class);
        }

    }

    @Nested
    @DisplayName("createOAuthUser()")
    class CreateOAuthUserTest {

        @Test
        @DisplayName("유효한 데이터로 OAuth UserModel 생성")
        void withValidData_createsOAuthUserModel() {
            // when
            UserModel user = UserModel.createOAuthUser(
                UserModel.AuthProvider.GOOGLE,
                "test@example.com",
                "홍길동"
            );

            // then
            assertThat(user.getAuthProvider()).isEqualTo(UserModel.AuthProvider.GOOGLE);
            assertThat(user.getEmail()).isEqualTo("test@example.com");
            assertThat(user.getName()).isEqualTo("홍길동");
            assertThat(user.getPassword()).isNull();
            assertThat(user.getRole()).isEqualTo(UserModel.Role.USER);
            assertThat(user.isLocked()).isFalse();
        }

        @Test
        @DisplayName("EMAIL provider로 생성 시 예외 발생")
        void withEmailProvider_throwsException() {
            assertThatThrownBy(() -> UserModel.createOAuthUser(
                UserModel.AuthProvider.EMAIL,
                "test@example.com",
                "홍길동"
            ))
                .isInstanceOf(InvalidUserDataException.class);
        }

        @Test
        @DisplayName("null provider로 생성 시 예외 발생")
        @SuppressWarnings("DataFlowIssue")
        void withNullProvider_throwsException() {
            assertThatThrownBy(() -> UserModel.createOAuthUser(
                null,
                "test@example.com",
                "홍길동"
            ))
                .isInstanceOf(InvalidUserDataException.class);
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("com.mopl.domain.model.user.UserModelTest#blankStringProvider")
        @DisplayName("이메일이 비어있으면 예외 발생")
        void withBlankEmail_throwsException(String description, String email) {
            assertThatThrownBy(() -> UserModel.createOAuthUser(
                UserModel.AuthProvider.GOOGLE, email, DEFAULT_NAME))
                .isInstanceOf(InvalidUserDataException.class);
        }

        @Test
        @DisplayName("이메일이 정확히 최대 길이면 생성 성공")
        void withEmailAtMaxLength_createsOAuthUserModel() {
            String maxEmail = "a".repeat(EMAIL_MAX_LENGTH);

            UserModel user = UserModel.createOAuthUser(
                UserModel.AuthProvider.GOOGLE, maxEmail, DEFAULT_NAME);

            assertThat(user.getEmail()).isEqualTo(maxEmail);
        }

        @Test
        @DisplayName("이메일이 최대 길이 초과하면 예외 발생")
        void withEmailExceedingMaxLength_throwsException() {
            String longEmail = "a".repeat(EMAIL_MAX_LENGTH + 1);

            assertThatThrownBy(() -> UserModel.createOAuthUser(
                UserModel.AuthProvider.GOOGLE, longEmail, DEFAULT_NAME))
                .isInstanceOf(InvalidUserDataException.class);
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("com.mopl.domain.model.user.UserModelTest#blankStringProvider")
        @DisplayName("이름이 비어있으면 예외 발생")
        void withBlankName_throwsException(String description, String name) {
            assertThatThrownBy(() -> UserModel.createOAuthUser(
                UserModel.AuthProvider.GOOGLE, DEFAULT_EMAIL, name))
                .isInstanceOf(InvalidUserDataException.class);
        }

        @Test
        @DisplayName("이름이 정확히 최대 길이면 생성 성공")
        void withNameAtMaxLength_createsOAuthUserModel() {
            String maxName = "가".repeat(NAME_MAX_LENGTH);

            UserModel user = UserModel.createOAuthUser(
                UserModel.AuthProvider.GOOGLE, DEFAULT_EMAIL, maxName);

            assertThat(user.getName()).isEqualTo(maxName);
        }

        @Test
        @DisplayName("이름이 최대 길이 초과하면 예외 발생")
        void withNameExceedingMaxLength_throwsException() {
            String longName = "가".repeat(NAME_MAX_LENGTH + 1);

            assertThatThrownBy(() -> UserModel.createOAuthUser(
                UserModel.AuthProvider.GOOGLE, DEFAULT_EMAIL, longName))
                .isInstanceOf(InvalidUserDataException.class);
        }

    }

    @Nested
    @DisplayName("updateName()")
    class UpdateNameTest {

        @Test
        @DisplayName("유효한 이름으로 변경하면 새 객체 반환")
        void withValidName_returnsNewInstance() {
            // given
            UserModel user = createDefaultUser();

            // when
            UserModel result = user.updateName("김철수");

            // then
            assertThat(result.getName()).isEqualTo("김철수");
            assertThat(result).isNotSameAs(user);
        }

        @ParameterizedTest
        @DisplayName("null이거나 빈 문자열이면 변경하지 않음")
        @NullSource
        @ValueSource(strings = {"", "   "})
        void withBlankName_returnsThis(String newName) {
            // given
            UserModel user = createDefaultUser();

            // when
            UserModel result = user.updateName(newName);

            // then
            assertThat(result.getName()).isEqualTo(DEFAULT_NAME);
            assertThat(result).isSameAs(user);
        }

        @Test
        @DisplayName("최대 길이 초과하면 예외 발생")
        void withNameExceedingMaxLength_throwsException() {
            // given
            UserModel user = createDefaultUser();
            String longName = "가".repeat(NAME_MAX_LENGTH + 1);

            // when & then
            assertThatThrownBy(() -> user.updateName(longName))
                .isInstanceOf(InvalidUserDataException.class);
        }
    }

    @Nested
    @DisplayName("updatePassword()")
    class UpdatePasswordTest {

        @Test
        @DisplayName("유효한 비밀번호로 변경하면 새 객체 반환")
        void withValidPassword_returnsNewInstance() {
            // given
            UserModel user = createDefaultUser();

            // when
            UserModel result = user.updatePassword("newEncodedPassword");

            // then
            assertThat(result.getPassword()).isEqualTo("newEncodedPassword");
            assertThat(result).isNotSameAs(user);
        }

        @ParameterizedTest
        @DisplayName("null이거나 빈 문자열이면 변경하지 않음")
        @NullSource
        @ValueSource(strings = {"", "   "})
        void withBlankPassword_returnsThis(String newPassword) {
            // given
            UserModel user = createDefaultUser();

            // when
            UserModel result = user.updatePassword(newPassword);

            // then
            assertThat(result.getPassword()).isEqualTo(DEFAULT_PASSWORD);
            assertThat(result).isSameAs(user);
        }

        @Test
        @DisplayName("최대 길이 초과하면 예외 발생")
        void withPasswordExceedingMaxLength_throwsException() {
            // given
            UserModel user = createDefaultUser();
            String longPassword = "a".repeat(ENCODED_PASSWORD_MAX_LENGTH + 1);

            // when & then
            assertThatThrownBy(() -> user.updatePassword(longPassword))
                .isInstanceOf(InvalidUserDataException.class);
        }
    }

    @Nested
    @DisplayName("updateProfileImagePath()")
    class UpdateProfileImagePathTest {

        @Test
        @DisplayName("유효한 경로로 변경하면 새 객체 반환")
        void withValidPath_returnsNewInstance() {
            // given
            UserModel user = createDefaultUser();

            // when
            UserModel result = user.updateProfileImagePath("users/123/profile.jpg");

            // then
            assertThat(result.getProfileImagePath()).isEqualTo("users/123/profile.jpg");
            assertThat(result).isNotSameAs(user);
        }

        @Test
        @DisplayName("빈 문자열로 변경하면 새 객체 반환")
        void withEmptyPath_returnsNewInstance() {
            // given
            UserModel user = createUserWithProfileImage();

            // when
            UserModel result = user.updateProfileImagePath("");

            // then
            assertThat(result.getProfileImagePath()).isEmpty();
            assertThat(result).isNotSameAs(user);
        }

        @Test
        @DisplayName("null이면 변경하지 않음")
        void withNullPath_returnsThis() {
            // given
            UserModel user = createUserWithProfileImage();

            // when
            UserModel result = user.updateProfileImagePath(null);

            // then
            assertThat(result.getProfileImagePath()).isEqualTo("users/123/original.jpg");
            assertThat(result).isSameAs(user);
        }

        @Test
        @DisplayName("최대 길이 초과하면 예외 발생")
        void withPathExceedingMaxLength_throwsException() {
            // given
            UserModel user = createDefaultUser();
            String longPath = "a".repeat(PROFILE_IMAGE_PATH_MAX_LENGTH + 1);

            // when & then
            assertThatThrownBy(() -> user.updateProfileImagePath(longPath))
                .isInstanceOf(InvalidUserDataException.class);
        }

        private UserModel createUserWithProfileImage() {
            return createDefaultUser()
                .toBuilder()
                .profileImagePath("users/123/original.jpg")
                .build();
        }
    }

    @Nested
    @DisplayName("updateRole()")
    class UpdateRoleTest {

        @ParameterizedTest(name = "{0}")
        @DisplayName("모든 역할로 변경 가능")
        @EnumSource(UserModel.Role.class)
        void withAllRoles_returnsNewInstance(UserModel.Role role) {
            // given
            UserModel user = createDefaultUser();

            // when
            UserModel result = user.updateRole(role);

            // then
            assertThat(result.getRole()).isEqualTo(role);
            assertThat(result).isNotSameAs(user);
        }

        @Test
        @DisplayName("null이면 변경하지 않음")
        void withNullRole_returnsThis() {
            // given
            UserModel user = createDefaultUser();

            // when
            UserModel result = user.updateRole(null);

            // then
            assertThat(result.getRole()).isEqualTo(UserModel.Role.USER);
            assertThat(result).isSameAs(user);
        }
    }

    @Nested
    @DisplayName("lock()")
    class LockTest {

        @Test
        @DisplayName("계정을 잠그면 새 객체 반환")
        void withUnlockedAccount_returnsNewInstance() {
            // given
            UserModel user = createDefaultUser();
            assertThat(user.isLocked()).isFalse();

            // when
            UserModel result = user.lock();

            // then
            assertThat(result.isLocked()).isTrue();
            assertThat(result).isNotSameAs(user);
        }

        @Test
        @DisplayName("이미 잠긴 계정을 잠가도 새 객체 반환")
        void withLockedAccount_returnsNewInstance() {
            // given
            UserModel user = createDefaultUser().lock();
            assertThat(user.isLocked()).isTrue();

            // when
            UserModel result = user.lock();

            // then
            assertThat(result.isLocked()).isTrue();
            assertThat(result).isNotSameAs(user);
        }
    }

    @Nested
    @DisplayName("unlock()")
    class UnlockTest {

        @Test
        @DisplayName("계정 잠금을 해제하면 새 객체 반환")
        void withLockedAccount_returnsNewInstance() {
            // given
            UserModel user = createDefaultUser().lock();
            assertThat(user.isLocked()).isTrue();

            // when
            UserModel result = user.unlock();

            // then
            assertThat(result.isLocked()).isFalse();
            assertThat(result).isNotSameAs(user);
        }

        @Test
        @DisplayName("이미 해제된 계정을 해제해도 새 객체 반환")
        void withUnlockedAccount_returnsNewInstance() {
            // given
            UserModel user = createDefaultUser();
            assertThat(user.isLocked()).isFalse();

            // when
            UserModel result = user.unlock();

            // then
            assertThat(result.isLocked()).isFalse();
            assertThat(result).isNotSameAs(user);
        }
    }
}
