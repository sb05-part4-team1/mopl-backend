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

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import static com.mopl.domain.model.user.UserModel.EMAIL_MAX_LENGTH;
import static com.mopl.domain.model.user.UserModel.ENCODED_PASSWORD_MAX_LENGTH;
import static com.mopl.domain.model.user.UserModel.NAME_MAX_LENGTH;
import static com.mopl.domain.model.user.UserModel.PROFILE_IMAGE_URL_MAX_LENGTH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("UserModel 단위 테스트")
class UserModelTest {

    @Nested
    @DisplayName("SuperBuilder")
    class SuperBuilderTest {

        @Test
        @DisplayName("모든 필드가 주어진 값으로 초기화됨")
        void withBuilder_initializesAllFields() {
            // given
            UUID id = UUID.randomUUID();
            Instant createdAt = Instant.now();
            Instant updatedAt = Instant.now();
            Instant deletedAt = null;

            // when
            UserModel user = UserModel.builder()
                .id(id)
                .createdAt(createdAt)
                .deletedAt(deletedAt)
                .updatedAt(updatedAt)
                .authProvider(AuthProvider.GOOGLE)
                .email("test@example.com")
                .name("홍길동")
                .password("encodedP@ssw0rd!")
                .profileImageUrl("https://example.com/original.jpg")
                .role(Role.ADMIN)
                .locked(true)
                .build();

            // then
            assertThat(user.getId()).isEqualTo(id);
            assertThat(user.getCreatedAt()).isEqualTo(createdAt);
            assertThat(user.getUpdatedAt()).isEqualTo(updatedAt);
            assertThat(user.getDeletedAt()).isNull();
            assertThat(user.getAuthProvider()).isEqualTo(AuthProvider.GOOGLE);
            assertThat(user.getEmail()).isEqualTo("test@example.com");
            assertThat(user.getName()).isEqualTo("홍길동");
            assertThat(user.getPassword()).isEqualTo("encodedP@ssw0rd!");
            assertThat(user.getProfileImageUrl()).isEqualTo("https://example.com/original.jpg");
            assertThat(user.getRole()).isEqualTo(Role.ADMIN);
            assertThat(user.isLocked()).isTrue();
        }
    }

    @Nested
    @DisplayName("create()")
    class CreateTest {

        @Test
        @DisplayName("유효한 데이터로 UserModel 생성")
        void withValidData_createsUserModel() {
            // when
            UserModel user = UserModel.create(
                AuthProvider.EMAIL,
                "test@example.com",
                "홍길동",
                "encodedPassword123"
            );

            // then
            assertThat(user.getAuthProvider()).isEqualTo(AuthProvider.EMAIL);
            assertThat(user.getEmail()).isEqualTo("test@example.com");
            assertThat(user.getName()).isEqualTo("홍길동");
            assertThat(user.getPassword()).isEqualTo("encodedPassword123");
            assertThat(user.getRole()).isEqualTo(Role.USER);
            assertThat(user.isLocked()).isFalse();
            assertThat(user.getProfileImageUrl()).isNull();
        }

        @SuppressWarnings("ConstantConditions")
        @Test
        @DisplayName("authProvider가 null이면 예외 발생")
        void withNullAuthProvider_throwsException() {
            assertThatThrownBy(() -> UserModel.create(
                null,
                "test@example.com",
                "홍길동",
                "password"
            ))
                .isInstanceOf(InvalidUserDataException.class)
                .satisfies(e -> {
                    InvalidUserDataException ex = (InvalidUserDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("회원가입 경로는 null일 수 없습니다.");
                });
        }

        static Stream<Arguments> invalidEmailProvider() {
            return Stream.of(
                Arguments.of("null", null),
                Arguments.of("빈 문자열", ""),
                Arguments.of("공백만", "   ")
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidEmailProvider")
        @DisplayName("이메일이 비어있으면 예외 발생")
        void withEmptyEmail_throwsException(String description, String email) {
            assertThatThrownBy(() -> UserModel.create(
                AuthProvider.EMAIL,
                email,
                "홍길동",
                "password"
            ))
                .isInstanceOf(InvalidUserDataException.class)
                .satisfies(e -> {
                    InvalidUserDataException ex = (InvalidUserDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("이메일은 비어있을 수 없습니다.");
                });
        }

        @Test
        @DisplayName("이메일이 255자 초과하면 예외 발생")
        void withEmailExceedingMaxLength_throwsException() {
            String longEmail = "a".repeat(EMAIL_MAX_LENGTH + 1);

            assertThatThrownBy(() -> UserModel.create(
                AuthProvider.EMAIL,
                longEmail,
                "홍길동",
                "password"
            ))
                .isInstanceOf(InvalidUserDataException.class)
                .satisfies(e -> {
                    InvalidUserDataException ex = (InvalidUserDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("이메일은 " + EMAIL_MAX_LENGTH + "자를 초과할 수 없습니다.");
                });
        }

        static Stream<Arguments> invalidNameProvider() {
            return Stream.of(
                Arguments.of("null", null),
                Arguments.of("빈 문자열", ""),
                Arguments.of("공백만", "   ")
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidNameProvider")
        @DisplayName("이름이 비어있으면 예외 발생")
        void withEmptyName_throwsException(String description, String name) {
            assertThatThrownBy(() -> UserModel.create(
                AuthProvider.EMAIL,
                "test@example.com",
                name,
                "password"
            ))
                .isInstanceOf(InvalidUserDataException.class)
                .satisfies(e -> {
                    InvalidUserDataException ex = (InvalidUserDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("이름은 비어있을 수 없습니다.");
                });
        }

        @Test
        @DisplayName("이름이 50자 초과하면 예외 발생")
        void withNameExceedingMaxLength_throwsException() {
            String longName = "가".repeat(NAME_MAX_LENGTH + 1);

            assertThatThrownBy(() -> UserModel.create(
                AuthProvider.EMAIL,
                "test@example.com",
                longName,
                "password"
            ))
                .isInstanceOf(InvalidUserDataException.class)
                .satisfies(e -> {
                    InvalidUserDataException ex = (InvalidUserDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("이름은 " + NAME_MAX_LENGTH + "자를 초과할 수 없습니다.");
                });
        }

        static Stream<Arguments> invalidPasswordProvider() {
            return Stream.of(
                Arguments.of("null", null),
                Arguments.of("빈 문자열", ""),
                Arguments.of("공백만", "   ")
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidPasswordProvider")
        @DisplayName("비밀번호가 비어있으면 예외 발생")
        void withEmptyPassword_throwsException(String description, String password) {
            assertThatThrownBy(() -> UserModel.create(
                AuthProvider.EMAIL,
                "test@example.com",
                "홍길동",
                password
            ))
                .isInstanceOf(InvalidUserDataException.class)
                .satisfies(e -> {
                    InvalidUserDataException ex = (InvalidUserDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("비밀번호는 비어있을 수 없습니다.");
                });
        }

        @Test
        @DisplayName("비밀번호가 255자 초과하면 예외 발생")
        void withPasswordExceedingMaxLength_throwsException() {
            String longPassword = "a".repeat(ENCODED_PASSWORD_MAX_LENGTH + 1);

            assertThatThrownBy(() -> UserModel.create(
                AuthProvider.EMAIL,
                "test@example.com",
                "홍길동",
                longPassword
            ))
                .isInstanceOf(InvalidUserDataException.class)
                .satisfies(e -> {
                    InvalidUserDataException ex = (InvalidUserDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("비밀번호는 " + ENCODED_PASSWORD_MAX_LENGTH + "자를 초과할 수 없습니다.");
                });
        }
    }

    @Nested
    @DisplayName("updatePassword()")
    class UpdatePasswordTest {

        @Test
        @DisplayName("유효한 비밀번호로 변경")
        void withValidPassword_updatesPassword() {
            // given
            UserModel user = UserModel.create(
                AuthProvider.EMAIL,
                "test@example.com",
                "홍길동",
                "oldPassword"
            );

            // when
            user.updatePassword("newEncodedPassword");

            // then
            assertThat(user.getPassword()).isEqualTo("newEncodedPassword");
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "   "})
        @DisplayName("null이거나 빈 문자열이면 변경하지 않음")
        void withEmptyPassword_doesNotUpdate(String newPassword) {
            // given
            UserModel user = UserModel.create(
                AuthProvider.EMAIL,
                "test@example.com",
                "홍길동",
                "originalPassword"
            );

            // when
            user.updatePassword(newPassword);

            // then
            assertThat(user.getPassword()).isEqualTo("originalPassword");
        }

        @Test
        @DisplayName("255자 초과하면 예외 발생")
        void withPasswordExceedingMaxLength_throwsException() {
            // given
            UserModel user = UserModel.create(
                AuthProvider.EMAIL,
                "test@example.com",
                "홍길동",
                "password"
            );
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

        @Test
        @DisplayName("자기 자신을 반환")
        void withValidPassword_returnsThis() {
            // given
            UserModel user = UserModel.create(
                AuthProvider.EMAIL,
                "test@example.com",
                "홍길동",
                "password"
            );

            // when
            UserModel result = user.updatePassword("newPassword");

            // then
            assertThat(result).isSameAs(user);
        }
    }

    @Nested
    @DisplayName("updateProfileImageUrl()")
    class UpdateProfileImageUrlTest {

        @Test
        @DisplayName("유효한 URL로 변경")
        void withValidUrl_updatesProfileImageUrl() {
            // given
            UserModel user = UserModel.create(
                AuthProvider.EMAIL,
                "test@example.com",
                "홍길동",
                "password"
            );

            // when
            user.updateProfileImageUrl("https://example.com/new-profile.jpg");

            // then
            assertThat(user.getProfileImageUrl()).isEqualTo("https://example.com/new-profile.jpg");
        }

        @Test
        @DisplayName("null이면 변경하지 않음")
        void withNullUrl_doesNotUpdate() {
            // given
            UserModel user = UserModel.builder()
                .id(UUID.randomUUID())
                .createdAt(Instant.now())
                .deletedAt(null)
                .updatedAt(Instant.now())
                .authProvider(AuthProvider.EMAIL)
                .email("test@example.com")
                .name("홍길동")
                .password("P@ssw0rd!")
                .profileImageUrl("https://example.com/original.jpg")
                .role(Role.USER)
                .locked(false)
                .build();

            // when
            user.updateProfileImageUrl(null);

            // then
            assertThat(user.getProfileImageUrl()).isEqualTo("https://example.com/original.jpg");
        }

        @Test
        @DisplayName("1024자 초과하면 예외 발생")
        void withUrlExceedingMaxLength_throwsException() {
            // given
            UserModel user = UserModel.create(
                AuthProvider.EMAIL,
                "test@example.com",
                "홍길동",
                "password"
            );
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

        @Test
        @DisplayName("자기 자신을 반환")
        void withValidUrl_returnsThis() {
            // given
            UserModel user = UserModel.create(
                AuthProvider.EMAIL,
                "test@example.com",
                "홍길동",
                "password"
            );

            // when
            UserModel result = user.updateProfileImageUrl("https://example.com/profile.jpg");

            // then
            assertThat(result).isSameAs(user);
        }
    }

    @Nested
    @DisplayName("updateRole()")
    class UpdateRoleTest {

        @Test
        @DisplayName("역할 변경")
        void withValidRole_updatesRole() {
            // given
            UserModel user = UserModel.create(
                AuthProvider.EMAIL,
                "test@example.com",
                "홍길동",
                "password"
            );
            assertThat(user.getRole()).isEqualTo(Role.USER);

            // when
            user.updateRole(Role.ADMIN);

            // then
            assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        }

        @Test
        @DisplayName("null이면 변경하지 않음")
        void withNullRole_doesNotUpdate() {
            // given
            UserModel user = UserModel.create(
                AuthProvider.EMAIL,
                "test@example.com",
                "홍길동",
                "password"
            );

            // when
            user.updateRole(null);

            // then
            assertThat(user.getRole()).isEqualTo(Role.USER);
        }

        @Test
        @DisplayName("자기 자신을 반환")
        void withValidRole_returnsThis() {
            // given
            UserModel user = UserModel.create(
                AuthProvider.EMAIL,
                "test@example.com",
                "홍길동",
                "password"
            );

            // when
            UserModel result = user.updateRole(Role.ADMIN);

            // then
            assertThat(result).isSameAs(user);
        }
    }

    @Nested
    @DisplayName("lock()")
    class LockTest {

        @Test
        @DisplayName("계정 잠금")
        void withUnlockedAccount_locksAccount() {
            // given
            UserModel user = UserModel.create(
                AuthProvider.EMAIL,
                "test@example.com",
                "홍길동",
                "password"
            );
            assertThat(user.isLocked()).isFalse();

            // when
            user.lock();

            // then
            assertThat(user.isLocked()).isTrue();
        }

        @Test
        @DisplayName("자기 자신을 반환")
        void withUnlockedAccount_returnsThis() {
            // given
            UserModel user = UserModel.create(
                AuthProvider.EMAIL,
                "test@example.com",
                "홍길동",
                "password"
            );

            // when
            UserModel result = user.lock();

            // then
            assertThat(result).isSameAs(user);
        }
    }

    @Nested
    @DisplayName("unlock()")
    class UnlockTest {

        @Test
        @DisplayName("계정 잠금 해제")
        void withLockedAccount_unlocksAccount() {
            // given
            UserModel user = UserModel.builder()
                .id(UUID.randomUUID())
                .createdAt(Instant.now())
                .deletedAt(null)
                .updatedAt(Instant.now())
                .authProvider(AuthProvider.EMAIL)
                .email("test@example.com")
                .name("홍길동")
                .password("P@ssw0rd!")
                .profileImageUrl(null)
                .role(Role.USER)
                .locked(true)
                .build();

            assertThat(user.isLocked()).isTrue();

            // when
            user.unlock();

            // then
            assertThat(user.isLocked()).isFalse();
        }

        @Test
        @DisplayName("자기 자신을 반환")
        void withLockedAccount_returnsThis() {
            // given
            UserModel user = UserModel.create(
                AuthProvider.EMAIL,
                "test@example.com",
                "홍길동",
                "password"
            );

            // when
            UserModel result = user.unlock();

            // then
            assertThat(result).isSameAs(user);
        }
    }
}
