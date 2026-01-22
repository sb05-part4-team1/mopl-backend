package com.mopl.domain.model.user;

import com.mopl.domain.exception.user.InvalidUserDataException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static com.mopl.domain.model.user.UserModel.EMAIL_MAX_LENGTH;
import static com.mopl.domain.model.user.UserModel.ENCODED_PASSWORD_MAX_LENGTH;
import static com.mopl.domain.model.user.UserModel.NAME_MAX_LENGTH;
import static com.mopl.domain.model.user.UserModel.PROFILE_IMAGE_URL_MAX_LENGTH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("UserModel 단위 테스트")
class UserModelTest {

    @DisplayName("create()")
    @Nested
    class CreateTest {

        @DisplayName("유효한 데이터로 UserModel 생성")
        @Test
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
            assertThat(user.getProfileImageUrl()).isNull();
        }

        @DisplayName("이메일이 비어있으면 예외 발생")
        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidEmailProvider")
        void withEmptyEmail_throwsException(String description, String email) {
            assertThatThrownBy(() -> UserModel.create(email, "홍길동", "password"))
                .isInstanceOf(InvalidUserDataException.class)
                .satisfies(e -> {
                    InvalidUserDataException ex = (InvalidUserDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("이메일은 비어있을 수 없습니다.");
                });
        }

        @DisplayName("이메일이 정확히 최대 길이면 생성 성공")
        @Test
        void withEmailAtMaxLength_createsUserModel() {
            String maxEmail = "a".repeat(EMAIL_MAX_LENGTH);

            UserModel user = UserModel.create(maxEmail, "홍길동", "password");

            assertThat(user.getEmail()).isEqualTo(maxEmail);
        }

        @DisplayName("이메일이 최대 길이 초과하면 예외 발생")
        @Test
        void withEmailExceedingMaxLength_throwsException() {
            String longEmail = "a".repeat(EMAIL_MAX_LENGTH + 1);

            assertThatThrownBy(() -> UserModel.create(longEmail, "홍길동", "password"))
                .isInstanceOf(InvalidUserDataException.class)
                .satisfies(e -> {
                    InvalidUserDataException ex = (InvalidUserDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("이메일은 " + EMAIL_MAX_LENGTH + "자를 초과할 수 없습니다.");
                });
        }

        @DisplayName("이름이 비어있으면 예외 발생")
        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidNameProvider")
        void withEmptyName_throwsException(String description, String name) {
            assertThatThrownBy(() -> UserModel.create("test@example.com", name, "password"))
                .isInstanceOf(InvalidUserDataException.class)
                .satisfies(e -> {
                    InvalidUserDataException ex = (InvalidUserDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("이름은 비어있을 수 없습니다.");
                });
        }

        @DisplayName("이름이 정확히 최대 길이면 생성 성공")
        @Test
        void withNameAtMaxLength_createsUserModel() {
            String maxName = "가".repeat(NAME_MAX_LENGTH);

            UserModel user = UserModel.create("test@example.com", maxName, "password");

            assertThat(user.getName()).isEqualTo(maxName);
        }

        @DisplayName("이름이 최대 길이 초과하면 예외 발생")
        @Test
        void withNameExceedingMaxLength_throwsException() {
            String longName = "가".repeat(NAME_MAX_LENGTH + 1);

            assertThatThrownBy(() -> UserModel.create("test@example.com", longName, "password"))
                .isInstanceOf(InvalidUserDataException.class)
                .satisfies(e -> {
                    InvalidUserDataException ex = (InvalidUserDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("이름은 " + NAME_MAX_LENGTH + "자를 초과할 수 없습니다.");
                });
        }

        @DisplayName("비밀번호가 비어있으면 예외 발생")
        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidPasswordProvider")
        void withEmptyPassword_throwsException(String description, String password) {
            assertThatThrownBy(() -> UserModel.create("test@example.com", "홍길동", password))
                .isInstanceOf(InvalidUserDataException.class)
                .satisfies(e -> {
                    InvalidUserDataException ex = (InvalidUserDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("비밀번호는 비어있을 수 없습니다.");
                });
        }

        @DisplayName("비밀번호가 정확히 최대 길이면 생성 성공")
        @Test
        void withPasswordAtMaxLength_createsUserModel() {
            String maxPassword = "a".repeat(ENCODED_PASSWORD_MAX_LENGTH);

            UserModel user = UserModel.create("test@example.com", "홍길동", maxPassword);

            assertThat(user.getPassword()).isEqualTo(maxPassword);
        }

        @DisplayName("비밀번호가 최대 길이 초과하면 예외 발생")
        @Test
        void withPasswordExceedingMaxLength_throwsException() {
            String longPassword = "a".repeat(ENCODED_PASSWORD_MAX_LENGTH + 1);

            assertThatThrownBy(() -> UserModel.create("test@example.com", "홍길동", longPassword))
                .isInstanceOf(InvalidUserDataException.class)
                .satisfies(e -> {
                    InvalidUserDataException ex = (InvalidUserDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("비밀번호는 " + ENCODED_PASSWORD_MAX_LENGTH + "자를 초과할 수 없습니다.");
                });
        }

        static Stream<Arguments> invalidEmailProvider() {
            return Stream.of(
                Arguments.of("null", null),
                Arguments.of("빈 문자열", ""),
                Arguments.of("공백만", "   ")
            );
        }

        static Stream<Arguments> invalidNameProvider() {
            return Stream.of(
                Arguments.of("null", null),
                Arguments.of("빈 문자열", ""),
                Arguments.of("공백만", "   ")
            );
        }

        static Stream<Arguments> invalidPasswordProvider() {
            return Stream.of(
                Arguments.of("null", null),
                Arguments.of("빈 문자열", ""),
                Arguments.of("공백만", "   ")
            );
        }
    }

    @DisplayName("createOAuthUser()")
    @Nested
    class CreateOAuthUserTest {

        @DisplayName("유효한 데이터로 OAuth UserModel 생성")
        @Test
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

        @DisplayName("EMAIL provider로 생성 시 예외 발생")
        @Test
        void withEmailProvider_throwsException() {
            assertThatThrownBy(() -> UserModel.createOAuthUser(
                UserModel.AuthProvider.EMAIL,
                "test@example.com",
                "홍길동"
            ))
                .isInstanceOf(InvalidUserDataException.class)
                .satisfies(e -> {
                    InvalidUserDataException ex = (InvalidUserDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("OAuth 회원가입에는 유효한 OAuth 제공자가 필요합니다.");
                });
        }

        @DisplayName("null provider로 생성 시 예외 발생")
        @Test
        void withNullProvider_throwsException() {
            assertThatThrownBy(() -> UserModel.createOAuthUser(
                null,
                "test@example.com",
                "홍길동"
            ))
                .isInstanceOf(InvalidUserDataException.class)
                .satisfies(e -> {
                    InvalidUserDataException ex = (InvalidUserDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("OAuth 회원가입에는 유효한 OAuth 제공자가 필요합니다.");
                });
        }

        @DisplayName("이메일이 비어있으면 예외 발생")
        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidEmailProvider")
        void withEmptyEmail_throwsException(String description, String email) {
            assertThatThrownBy(() -> UserModel.createOAuthUser(
                UserModel.AuthProvider.GOOGLE, email, "홍길동"))
                .isInstanceOf(InvalidUserDataException.class)
                .satisfies(e -> {
                    InvalidUserDataException ex = (InvalidUserDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("이메일은 비어있을 수 없습니다.");
                });
        }

        @DisplayName("이메일이 최대 길이 초과하면 예외 발생")
        @Test
        void withEmailExceedingMaxLength_throwsException() {
            String longEmail = "a".repeat(EMAIL_MAX_LENGTH + 1);

            assertThatThrownBy(() -> UserModel.createOAuthUser(
                UserModel.AuthProvider.GOOGLE, longEmail, "홍길동"))
                .isInstanceOf(InvalidUserDataException.class)
                .satisfies(e -> {
                    InvalidUserDataException ex = (InvalidUserDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("이메일은 " + EMAIL_MAX_LENGTH + "자를 초과할 수 없습니다.");
                });
        }

        @DisplayName("이름이 비어있으면 예외 발생")
        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidNameProvider")
        void withEmptyName_throwsException(String description, String name) {
            assertThatThrownBy(() -> UserModel.createOAuthUser(
                UserModel.AuthProvider.GOOGLE, "test@example.com", name))
                .isInstanceOf(InvalidUserDataException.class)
                .satisfies(e -> {
                    InvalidUserDataException ex = (InvalidUserDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("이름은 비어있을 수 없습니다.");
                });
        }

        @DisplayName("이름이 최대 길이 초과하면 예외 발생")
        @Test
        void withNameExceedingMaxLength_throwsException() {
            String longName = "가".repeat(NAME_MAX_LENGTH + 1);

            assertThatThrownBy(() -> UserModel.createOAuthUser(
                UserModel.AuthProvider.GOOGLE, "test@example.com", longName))
                .isInstanceOf(InvalidUserDataException.class)
                .satisfies(e -> {
                    InvalidUserDataException ex = (InvalidUserDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("이름은 " + NAME_MAX_LENGTH + "자를 초과할 수 없습니다.");
                });
        }

        static Stream<Arguments> invalidEmailProvider() {
            return Stream.of(
                Arguments.of("null", null),
                Arguments.of("빈 문자열", ""),
                Arguments.of("공백만", "   ")
            );
        }

        static Stream<Arguments> invalidNameProvider() {
            return Stream.of(
                Arguments.of("null", null),
                Arguments.of("빈 문자열", ""),
                Arguments.of("공백만", "   ")
            );
        }
    }

    @DisplayName("updateName()")
    @Nested
    class UpdateNameTest {

        @DisplayName("유효한 이름으로 변경하면 새 객체 반환")
        @Test
        void withValidName_returnsNewInstance() {
            // given
            UserModel user = UserModel.create("test@example.com", "홍길동", "password");

            // when
            UserModel result = user.updateName("김철수");

            // then
            assertThat(result.getName()).isEqualTo("김철수");
            assertThat(result).isNotSameAs(user);
            assertThat(result.getEmail()).isEqualTo(user.getEmail());
            assertThat(result.getPassword()).isEqualTo(user.getPassword());
        }

        @DisplayName("null이거나 빈 문자열이면 변경하지 않음")
        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "   "})
        void withEmptyName_returnsThis(String newName) {
            // given
            UserModel user = UserModel.create("test@example.com", "홍길동", "password");

            // when
            UserModel result = user.updateName(newName);

            // then
            assertThat(result.getName()).isEqualTo("홍길동");
            assertThat(result).isSameAs(user);
        }

        @DisplayName("최대 길이 초과하면 예외 발생")
        @Test
        void withNameExceedingMaxLength_throwsException() {
            // given
            UserModel user = UserModel.create("test@example.com", "홍길동", "password");
            String longName = "가".repeat(NAME_MAX_LENGTH + 1);

            // when & then
            assertThatThrownBy(() -> user.updateName(longName))
                .isInstanceOf(InvalidUserDataException.class)
                .satisfies(e -> {
                    InvalidUserDataException ex = (InvalidUserDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("이름은 " + NAME_MAX_LENGTH + "자를 초과할 수 없습니다.");
                });
        }
    }

    @DisplayName("updatePassword()")
    @Nested
    class UpdatePasswordTest {

        @DisplayName("유효한 비밀번호로 변경하면 새 객체 반환")
        @Test
        void withValidPassword_returnsNewInstance() {
            // given
            UserModel user = UserModel.create("test@example.com", "홍길동", "oldPassword");

            // when
            UserModel result = user.updatePassword("newEncodedPassword");

            // then
            assertThat(result.getPassword()).isEqualTo("newEncodedPassword");
            assertThat(result).isNotSameAs(user);
            assertThat(result.getEmail()).isEqualTo(user.getEmail());
            assertThat(result.getName()).isEqualTo(user.getName());
        }

        @DisplayName("null이거나 빈 문자열이면 변경하지 않음")
        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "   "})
        void withEmptyPassword_returnsThis(String newPassword) {
            // given
            UserModel user = UserModel.create("test@example.com", "홍길동", "originalPassword");

            // when
            UserModel result = user.updatePassword(newPassword);

            // then
            assertThat(result.getPassword()).isEqualTo("originalPassword");
            assertThat(result).isSameAs(user);
        }

        @DisplayName("최대 길이 초과하면 예외 발생")
        @Test
        void withPasswordExceedingMaxLength_throwsException() {
            // given
            UserModel user = UserModel.create("test@example.com", "홍길동", "password");
            String longPassword = "a".repeat(ENCODED_PASSWORD_MAX_LENGTH + 1);

            // when & then
            assertThatThrownBy(() -> user.updatePassword(longPassword))
                .isInstanceOf(InvalidUserDataException.class)
                .satisfies(e -> {
                    InvalidUserDataException ex = (InvalidUserDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("비밀번호는 " + ENCODED_PASSWORD_MAX_LENGTH + "자를 초과할 수 없습니다.");
                });
        }
    }

    @DisplayName("updateProfileImageUrl()")
    @Nested
    class UpdateProfileImageUrlTest {

        @DisplayName("유효한 URL로 변경하면 새 객체 반환")
        @Test
        void withValidUrl_returnsNewInstance() {
            // given
            UserModel user = UserModel.create("test@example.com", "홍길동", "password");

            // when
            UserModel result = user.updateProfileImageUrl("https://example.com/profile.jpg");

            // then
            assertThat(result.getProfileImageUrl()).isEqualTo("https://example.com/profile.jpg");
            assertThat(result).isNotSameAs(user);
            assertThat(result.getEmail()).isEqualTo(user.getEmail());
            assertThat(result.getName()).isEqualTo(user.getName());
        }

        @DisplayName("빈 문자열로 변경하면 새 객체 반환")
        @Test
        void withEmptyUrl_returnsNewInstance() {
            // given
            UserModel user = createUserWithProfileImage("https://example.com/original.jpg");

            // when
            UserModel result = user.updateProfileImageUrl("");

            // then
            assertThat(result.getProfileImageUrl()).isEmpty();
            assertThat(result).isNotSameAs(user);
        }

        @DisplayName("null이면 변경하지 않음")
        @Test
        void withNullUrl_returnsThis() {
            // given
            UserModel user = createUserWithProfileImage("https://example.com/original.jpg");

            // when
            UserModel result = user.updateProfileImageUrl(null);

            // then
            assertThat(result.getProfileImageUrl()).isEqualTo("https://example.com/original.jpg");
            assertThat(result).isSameAs(user);
        }

        @DisplayName("최대 길이 초과하면 예외 발생")
        @Test
        void withUrlExceedingMaxLength_throwsException() {
            // given
            UserModel user = UserModel.create("test@example.com", "홍길동", "password");
            String longUrl = "a".repeat(PROFILE_IMAGE_URL_MAX_LENGTH + 1);

            // when & then
            assertThatThrownBy(() -> user.updateProfileImageUrl(longUrl))
                .isInstanceOf(InvalidUserDataException.class)
                .satisfies(e -> {
                    InvalidUserDataException ex = (InvalidUserDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("프로필 이미지 URL은 " + PROFILE_IMAGE_URL_MAX_LENGTH
                            + "자를 초과할 수 없습니다.");
                });
        }

        private UserModel createUserWithProfileImage(String profileImageUrl) {
            return UserModel.create("test@example.com", "홍길동", "password")
                .toBuilder()
                .profileImageUrl(profileImageUrl)
                .build();
        }
    }

    @DisplayName("updateRole()")
    @Nested
    class UpdateRoleTest {

        @DisplayName("유효한 역할로 변경하면 새 객체 반환")
        @Test
        void withValidRole_returnsNewInstance() {
            // given
            UserModel user = UserModel.create("test@example.com", "홍길동", "password");
            assertThat(user.getRole()).isEqualTo(UserModel.Role.USER);

            // when
            UserModel result = user.updateRole(UserModel.Role.ADMIN);

            // then
            assertThat(result.getRole()).isEqualTo(UserModel.Role.ADMIN);
            assertThat(result).isNotSameAs(user);
            assertThat(result.getEmail()).isEqualTo(user.getEmail());
            assertThat(result.getName()).isEqualTo(user.getName());
        }

        @DisplayName("같은 역할로 변경해도 새 객체 반환")
        @Test
        void withSameRole_returnsNewInstance() {
            // given
            UserModel user = UserModel.create("test@example.com", "홍길동", "password");

            // when
            UserModel result = user.updateRole(UserModel.Role.USER);

            // then
            assertThat(result.getRole()).isEqualTo(UserModel.Role.USER);
            assertThat(result).isNotSameAs(user);
        }

        @DisplayName("null이면 변경하지 않음")
        @Test
        void withNullRole_returnsThis() {
            // given
            UserModel user = UserModel.create("test@example.com", "홍길동", "password");

            // when
            UserModel result = user.updateRole(null);

            // then
            assertThat(result.getRole()).isEqualTo(UserModel.Role.USER);
            assertThat(result).isSameAs(user);
        }
    }

    @DisplayName("lock()")
    @Nested
    class LockTest {

        @DisplayName("계정을 잠그면 새 객체 반환")
        @Test
        void withUnlockedAccount_returnsNewInstance() {
            // given
            UserModel user = UserModel.create("test@example.com", "홍길동", "password");
            assertThat(user.isLocked()).isFalse();

            // when
            UserModel result = user.lock();

            // then
            assertThat(result.isLocked()).isTrue();
            assertThat(result).isNotSameAs(user);
            assertThat(result.getEmail()).isEqualTo(user.getEmail());
            assertThat(result.getName()).isEqualTo(user.getName());
        }

        @DisplayName("이미 잠긴 계정을 잠가도 새 객체 반환")
        @Test
        void withLockedAccount_returnsNewInstance() {
            // given
            UserModel user = UserModel.create("test@example.com", "홍길동", "password").lock();
            assertThat(user.isLocked()).isTrue();

            // when
            UserModel result = user.lock();

            // then
            assertThat(result.isLocked()).isTrue();
            assertThat(result).isNotSameAs(user);
        }
    }

    @DisplayName("unlock()")
    @Nested
    class UnlockTest {

        @DisplayName("계정 잠금을 해제하면 새 객체 반환")
        @Test
        void withLockedAccount_returnsNewInstance() {
            // given
            UserModel user = UserModel.create("test@example.com", "홍길동", "password").lock();
            assertThat(user.isLocked()).isTrue();

            // when
            UserModel result = user.unlock();

            // then
            assertThat(result.isLocked()).isFalse();
            assertThat(result).isNotSameAs(user);
            assertThat(result.getEmail()).isEqualTo(user.getEmail());
            assertThat(result.getName()).isEqualTo(user.getName());
        }

        @DisplayName("이미 해제된 계정을 해제해도 새 객체 반환")
        @Test
        void withUnlockedAccount_returnsNewInstance() {
            // given
            UserModel user = UserModel.create("test@example.com", "홍길동", "password");
            assertThat(user.isLocked()).isFalse();

            // when
            UserModel result = user.unlock();

            // then
            assertThat(result.isLocked()).isFalse();
            assertThat(result).isNotSameAs(user);
        }
    }
}
