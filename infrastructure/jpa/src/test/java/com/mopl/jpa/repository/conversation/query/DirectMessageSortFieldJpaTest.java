package com.mopl.jpa.repository.conversation.query;

import com.mopl.domain.repository.conversation.DirectMessageSortField;
import com.mopl.jpa.entity.conversation.DirectMessageEntity;
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

@DisplayName("DirectMessageSortFieldJpa 단위 테스트")
class DirectMessageSortFieldJpaTest {

    @Nested
    @DisplayName("from()")
    class FromTest {

        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("domainToJpaMapping")
        @DisplayName("도메인 필드를 JPA 필드로 변환한다")
        void convertsDomainFieldToJpaField(
            DirectMessageSortField domainField,
            DirectMessageSortFieldJpa expectedJpaField
        ) {
            // when
            DirectMessageSortFieldJpa result = DirectMessageSortFieldJpa.from(domainField);

            // then
            assertThat(result).isEqualTo(expectedJpaField);
        }

        static Stream<Arguments> domainToJpaMapping() {
            return Stream.of(
                Arguments.of(DirectMessageSortField.createdAt, DirectMessageSortFieldJpa.CREATED_AT)
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
            String serialized = DirectMessageSortFieldJpa.CREATED_AT.serializeCursor(value);
            Object deserialized = DirectMessageSortFieldJpa.CREATED_AT.deserializeCursor(serialized);

            // then
            assertThat(serialized).isEqualTo("2024-01-15T10:30:00Z");
            assertThat(deserialized).isEqualTo(value);
        }

        @Test
        @DisplayName("null 값은 빈 문자열로 직렬화된다")
        void nullValue_serializesToEmptyString() {
            assertThat(DirectMessageSortFieldJpa.CREATED_AT.serializeCursor(null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("extractValue()")
    class ExtractValueTest {

        private DirectMessageEntity createTestEntity() {
            return DirectMessageEntity.builder()
                .id(UUID.randomUUID())
                .createdAt(Instant.parse("2024-01-15T10:30:00Z"))
                .content("테스트 메시지")
                .build();
        }

        @Test
        @DisplayName("CREATED_AT - 생성일시를 추출한다")
        void createdAt_extractsCreatedAt() {
            // given
            DirectMessageEntity entity = createTestEntity();

            // when
            Object value = DirectMessageSortFieldJpa.CREATED_AT.extractValue(entity);

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
            for (DirectMessageSortFieldJpa field : DirectMessageSortFieldJpa.values()) {
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
        @DisplayName("도메인 필드 이름을 반환한다")
        void returnsFieldName() {
            assertThat(DirectMessageSortFieldJpa.CREATED_AT.getFieldName()).isEqualTo("createdAt");
        }
    }
}
