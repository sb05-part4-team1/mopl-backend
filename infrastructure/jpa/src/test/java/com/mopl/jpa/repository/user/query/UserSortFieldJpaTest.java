package com.mopl.jpa.repository.user.query;

import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.user.UserSortField;
import com.mopl.jpa.entity.user.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserSortFieldJpa 단위 테스트")
class UserSortFieldJpaTest {

    @Nested
    @DisplayName("from()")
    class FromTest {

        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("domainToJpaMapping")
        @DisplayName("도메인 필드를 JPA 필드로 변환한다")
        void convertsDomainFieldToJpaField(
            UserSortField domainField,
            UserSortFieldJpa expectedJpaField
        ) {
            // when
            UserSortFieldJpa result = UserSortFieldJpa.from(domainField);

            // then
            assertThat(result).isEqualTo(expectedJpaField);
        }

        static Stream<Arguments> domainToJpaMapping() {
            return Stream.of(
                Arguments.of(UserSortField.NAME, UserSortFieldJpa.NAME),
                Arguments.of(UserSortField.EMAIL, UserSortFieldJpa.EMAIL),
                Arguments.of(UserSortField.CREATED_AT, UserSortFieldJpa.CREATED_AT),
                Arguments.of(UserSortField.IS_LOCKED, UserSortFieldJpa.IS_LOCKED),
                Arguments.of(UserSortField.ROLE, UserSortFieldJpa.ROLE)
            );
        }
    }

    @Nested
    @DisplayName("serializeCursor() / deserializeCursor()")
    class SerializationTest {

        @Test
        @DisplayName("NAME - 문자열 직렬화/역직렬화")
        void name_serializesAndDeserializesString() {
            // given
            String value = "홍길동";

            // when
            String serialized = UserSortFieldJpa.NAME.serializeCursor(value);
            Object deserialized = UserSortFieldJpa.NAME.deserializeCursor(serialized);

            // then
            assertThat(serialized).isEqualTo("홍길동");
            assertThat(deserialized).isEqualTo(value);
        }

        @Test
        @DisplayName("EMAIL - 문자열 직렬화/역직렬화")
        void email_serializesAndDeserializesString() {
            // given
            String value = "test@example.com";

            // when
            String serialized = UserSortFieldJpa.EMAIL.serializeCursor(value);
            Object deserialized = UserSortFieldJpa.EMAIL.deserializeCursor(serialized);

            // then
            assertThat(serialized).isEqualTo("test@example.com");
            assertThat(deserialized).isEqualTo(value);
        }

        @Test
        @DisplayName("CREATED_AT - Instant 직렬화/역직렬화")
        void createdAt_serializesAndDeserializesInstant() {
            // given
            Instant value = Instant.parse("2024-01-15T10:30:00Z");

            // when
            String serialized = UserSortFieldJpa.CREATED_AT.serializeCursor(value);
            Object deserialized = UserSortFieldJpa.CREATED_AT.deserializeCursor(serialized);

            // then
            assertThat(serialized).isEqualTo("2024-01-15T10:30:00Z");
            assertThat(deserialized).isEqualTo(value);
        }

        @Test
        @DisplayName("IS_LOCKED - Boolean 직렬화/역직렬화")
        void isLocked_serializesAndDeserializesBoolean() {
            // given & when & then
            assertThat(UserSortFieldJpa.IS_LOCKED.serializeCursor(true)).isEqualTo("true");
            assertThat(UserSortFieldJpa.IS_LOCKED.serializeCursor(false)).isEqualTo("false");
            assertThat(UserSortFieldJpa.IS_LOCKED.deserializeCursor("true")).isEqualTo(true);
            assertThat(UserSortFieldJpa.IS_LOCKED.deserializeCursor("false")).isEqualTo(false);
        }

        @Test
        @DisplayName("ROLE - 문자열 직렬화/역직렬화")
        void role_serializesAndDeserializesString() {
            // given
            String value = "ADMIN";

            // when
            String serialized = UserSortFieldJpa.ROLE.serializeCursor(value);
            Object deserialized = UserSortFieldJpa.ROLE.deserializeCursor(serialized);

            // then
            assertThat(serialized).isEqualTo("ADMIN");
            assertThat(deserialized).isEqualTo(value);
        }

        @Test
        @DisplayName("null 값은 빈 문자열로 직렬화된다")
        void nullValue_serializesToEmptyString() {
            assertThat(UserSortFieldJpa.NAME.serializeCursor(null)).isEmpty();
            assertThat(UserSortFieldJpa.EMAIL.serializeCursor(null)).isEmpty();
            assertThat(UserSortFieldJpa.ROLE.serializeCursor(null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("extractValue()")
    class ExtractValueTest {

        private UserEntity createTestEntity() {
            return UserEntity.builder()
                .id(UUID.randomUUID())
                .createdAt(Instant.parse("2024-01-15T10:30:00Z"))
                .updatedAt(Instant.now())
                .authProvider(UserModel.AuthProvider.EMAIL)
                .email("test@example.com")
                .name("홍길동")
                .password("encodedPassword")
                .role(UserModel.Role.ADMIN)
                .locked(true)
                .build();
        }

        @Test
        @DisplayName("NAME - 이름을 추출한다")
        void name_extractsName() {
            // given
            UserEntity entity = createTestEntity();

            // when
            Object value = UserSortFieldJpa.NAME.extractValue(entity);

            // then
            assertThat(value).isEqualTo("홍길동");
        }

        @Test
        @DisplayName("EMAIL - 이메일을 추출한다")
        void email_extractsEmail() {
            // given
            UserEntity entity = createTestEntity();

            // when
            Object value = UserSortFieldJpa.EMAIL.extractValue(entity);

            // then
            assertThat(value).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("CREATED_AT - 생성일시를 추출한다")
        void createdAt_extractsCreatedAt() {
            // given
            UserEntity entity = createTestEntity();

            // when
            Object value = UserSortFieldJpa.CREATED_AT.extractValue(entity);

            // then
            assertThat(value).isEqualTo(Instant.parse("2024-01-15T10:30:00Z"));
        }

        @Test
        @DisplayName("IS_LOCKED - 잠금 상태를 추출한다")
        void isLocked_extractsLockedStatus() {
            // given
            UserEntity entity = createTestEntity();

            // when
            Object value = UserSortFieldJpa.IS_LOCKED.extractValue(entity);

            // then
            assertThat(value).isEqualTo(true);
        }

        @Test
        @DisplayName("ROLE - 역할명을 문자열로 추출한다")
        void role_extractsRoleName() {
            // given
            UserEntity entity = createTestEntity();

            // when
            Object value = UserSortFieldJpa.ROLE.extractValue(entity);

            // then
            assertThat(value).isEqualTo("ADMIN");
        }
    }

    @Nested
    @DisplayName("getExpression()")
    class GetExpressionTest {

        @Test
        @DisplayName("모든 필드가 non-null expression을 반환한다")
        void allFields_returnNonNullExpression() {
            for (UserSortFieldJpa field : UserSortFieldJpa.values()) {
                assertThat(field.getExpression())
                    .as("Expression for %s should not be null", field.name())
                    .isNotNull();
            }
        }
    }
}
