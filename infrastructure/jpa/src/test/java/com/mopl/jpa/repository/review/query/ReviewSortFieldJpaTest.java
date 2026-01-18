package com.mopl.jpa.repository.review.query;

import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.review.ReviewSortField;
import com.mopl.jpa.entity.content.ContentEntity;
import com.mopl.jpa.entity.review.ReviewEntity;
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

@DisplayName("ReviewSortFieldJpa 단위 테스트")
class ReviewSortFieldJpaTest {

    @Nested
    @DisplayName("from()")
    class FromTest {

        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("domainToJpaMapping")
        @DisplayName("도메인 필드를 JPA 필드로 변환한다")
        void convertsDomainFieldToJpaField(
            ReviewSortField domainField,
            ReviewSortFieldJpa expectedJpaField
        ) {
            // when
            ReviewSortFieldJpa result = ReviewSortFieldJpa.from(domainField);

            // then
            assertThat(result).isEqualTo(expectedJpaField);
        }

        static Stream<Arguments> domainToJpaMapping() {
            return Stream.of(
                Arguments.of(ReviewSortField.createdAt, ReviewSortFieldJpa.CREATED_AT),
                Arguments.of(ReviewSortField.rating, ReviewSortFieldJpa.RATING)
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
            String serialized = ReviewSortFieldJpa.CREATED_AT.serializeCursor(value);
            Object deserialized = ReviewSortFieldJpa.CREATED_AT.deserializeCursor(serialized);

            // then
            assertThat(serialized).isEqualTo("2024-01-15T10:30:00Z");
            assertThat(deserialized).isEqualTo(value);
        }

        @Test
        @DisplayName("RATING - Double 직렬화/역직렬화")
        void rating_serializesAndDeserializesDouble() {
            // given
            Double value = 4.5;

            // when
            String serialized = ReviewSortFieldJpa.RATING.serializeCursor(value);
            Object deserialized = ReviewSortFieldJpa.RATING.deserializeCursor(serialized);

            // then
            assertThat(serialized).isEqualTo("4.5");
            assertThat(deserialized).isEqualTo(value);
        }

        @Test
        @DisplayName("null 값은 빈 문자열로 직렬화된다")
        void nullValue_serializesToEmptyString() {
            assertThat(ReviewSortFieldJpa.CREATED_AT.serializeCursor(null)).isEmpty();
            assertThat(ReviewSortFieldJpa.RATING.serializeCursor(null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("extractValue()")
    class ExtractValueTest {

        private ReviewEntity createTestEntity() {
            UserEntity author = UserEntity.builder()
                .id(UUID.randomUUID())
                .authProvider(UserModel.AuthProvider.EMAIL)
                .email("test@example.com")
                .name("테스트")
                .password("encodedPassword")
                .role(UserModel.Role.USER)
                .locked(false)
                .build();

            ContentEntity content = ContentEntity.builder()
                .id(UUID.randomUUID())
                .build();

            return ReviewEntity.builder()
                .id(UUID.randomUUID())
                .createdAt(Instant.parse("2024-01-15T10:30:00Z"))
                .text("테스트 리뷰")
                .rating(4.5)
                .author(author)
                .content(content)
                .build();
        }

        @Test
        @DisplayName("CREATED_AT - 생성일시를 추출한다")
        void createdAt_extractsCreatedAt() {
            // given
            ReviewEntity entity = createTestEntity();

            // when
            Object value = ReviewSortFieldJpa.CREATED_AT.extractValue(entity);

            // then
            assertThat(value).isEqualTo(Instant.parse("2024-01-15T10:30:00Z"));
        }

        @Test
        @DisplayName("RATING - 평점을 추출한다")
        void rating_extractsRating() {
            // given
            ReviewEntity entity = createTestEntity();

            // when
            Object value = ReviewSortFieldJpa.RATING.extractValue(entity);

            // then
            assertThat(value).isEqualTo(4.5);
        }
    }

    @Nested
    @DisplayName("getExpression()")
    class GetExpressionTest {

        @Test
        @DisplayName("모든 필드가 non-null expression을 반환한다")
        void allFields_returnNonNullExpression() {
            for (ReviewSortFieldJpa field : ReviewSortFieldJpa.values()) {
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
            String fieldName = ReviewSortFieldJpa.CREATED_AT.getFieldName();

            // then
            assertThat(fieldName).isEqualTo("createdAt");
        }

        @Test
        @DisplayName("RATING - 필드명을 반환한다")
        void rating_returnsFieldName() {
            // when
            String fieldName = ReviewSortFieldJpa.RATING.getFieldName();

            // then
            assertThat(fieldName).isEqualTo("rating");
        }
    }
}
