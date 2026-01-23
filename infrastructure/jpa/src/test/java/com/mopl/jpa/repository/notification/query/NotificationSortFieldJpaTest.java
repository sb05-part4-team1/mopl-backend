package com.mopl.jpa.repository.notification.query;

import com.mopl.domain.model.notification.NotificationModel;
import com.mopl.domain.repository.notification.NotificationSortField;
import com.mopl.jpa.entity.notification.NotificationEntity;
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

@DisplayName("NotificationSortFieldJpa 단위 테스트")
class NotificationSortFieldJpaTest {

    @Nested
    @DisplayName("from()")
    class FromTest {

        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("domainToJpaMapping")
        @DisplayName("도메인 필드를 JPA 필드로 변환한다")
        void convertsDomainFieldToJpaField(
            NotificationSortField domainField,
            NotificationSortFieldJpa expectedJpaField
        ) {
            // when
            NotificationSortFieldJpa result = NotificationSortFieldJpa.from(domainField);

            // then
            assertThat(result).isEqualTo(expectedJpaField);
        }

        static Stream<Arguments> domainToJpaMapping() {
            return Stream.of(
                Arguments.of(NotificationSortField.createdAt, NotificationSortFieldJpa.CREATED_AT)
            );
        }
    }

    @Nested
    @DisplayName("serializeCursor() / deserializeCursor()")
    class SerializationTest {

        @Test
        @DisplayName("CREATED_AT - Instant 직렬화/역직렬화")
        void createdAt_serializesAndDeserializesInstant() {
            // given
            Instant value = Instant.parse("2024-01-15T10:30:00Z");

            // when
            String serialized = NotificationSortFieldJpa.CREATED_AT.serializeCursor(value);
            Object deserialized = NotificationSortFieldJpa.CREATED_AT.deserializeCursor(serialized);

            // then
            assertThat(serialized).isEqualTo("2024-01-15T10:30:00Z");
            assertThat(deserialized).isEqualTo(value);
        }

        @Test
        @DisplayName("null 값은 빈 문자열로 직렬화된다")
        void nullValue_serializesToEmptyString() {
            assertThat(NotificationSortFieldJpa.CREATED_AT.serializeCursor(null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("extractValue()")
    class ExtractValueTest {

        private NotificationEntity createTestEntity() {
            return NotificationEntity.builder()
                .id(UUID.randomUUID())
                .createdAt(Instant.parse("2024-01-15T10:30:00Z"))
                .title("테스트 알림")
                .content("테스트 내용")
                .level(NotificationModel.NotificationLevel.INFO)
                .receiverId(UUID.randomUUID())
                .build();
        }

        @Test
        @DisplayName("CREATED_AT - 생성일시를 추출한다")
        void createdAt_extractsCreatedAt() {
            // given
            NotificationEntity entity = createTestEntity();

            // when
            Object value = NotificationSortFieldJpa.CREATED_AT.extractValue(entity);

            // then
            assertThat(value).isEqualTo(Instant.parse("2024-01-15T10:30:00Z"));
        }
    }

    @Nested
    @DisplayName("getExpression()")
    class GetExpressionTest {

        @Test
        @DisplayName("모든 필드가 non-null expression을 반환한다")
        void allFields_returnNonNullExpression() {
            for (NotificationSortFieldJpa field : NotificationSortFieldJpa.values()) {
                assertThat(field.getExpression())
                    .as("Expression for %s should not be null", field.name())
                    .isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("getFieldName()")
    class GetFieldNameTest {

        @Test
        @DisplayName("CREATED_AT - 필드명을 반환한다")
        void createdAt_returnsFieldName() {
            // when
            String fieldName = NotificationSortFieldJpa.CREATED_AT.getFieldName();

            // then
            assertThat(fieldName).isEqualTo("createdAt");
        }
    }
}
