package com.mopl.jpa.repository.content.query;

import com.mopl.domain.repository.content.query.ContentSortField;
import com.mopl.jpa.entity.content.ContentEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ContentSortFieldJpa 테스트")
class ContentSortFieldJpaTest {

    @Nested
    @DisplayName("from()")
    class FromTest {

        @Test
        @DisplayName("POPULARITY 필드를 변환한다")
        void convertsPopularity() {
            // when
            ContentSortFieldJpa result = ContentSortFieldJpa.from(ContentSortField.POPULARITY);

            // then
            assertThat(result).isEqualTo(ContentSortFieldJpa.POPULARITY);
            assertThat(result.getFieldName()).isEqualTo("POPULARITY");
        }

        @Test
        @DisplayName("CREATED_AT 필드를 변환한다")
        void convertsCreatedAt() {
            // when
            ContentSortFieldJpa result = ContentSortFieldJpa.from(ContentSortField.CREATED_AT);

            // then
            assertThat(result).isEqualTo(ContentSortFieldJpa.CREATED_AT);
            assertThat(result.getFieldName()).isEqualTo("CREATED_AT");
        }

        @Test
        @DisplayName("RATE 필드를 변환한다")
        void convertsRate() {
            // when
            ContentSortFieldJpa result = ContentSortFieldJpa.from(ContentSortField.RATE);

            // then
            assertThat(result).isEqualTo(ContentSortFieldJpa.RATE);
            assertThat(result.getFieldName()).isEqualTo("RATE");
        }
    }

    @Nested
    @DisplayName("serializeCursor()")
    class SerializeCursorTest {

        @Test
        @DisplayName("POPULARITY 값을 직렬화한다")
        void serializesPopularity() {
            // given
            Double value = 123.45;

            // when
            String serialized = ContentSortFieldJpa.POPULARITY.serializeCursor(value);

            // then
            assertThat(serialized).isEqualTo("123.45");
        }

        @Test
        @DisplayName("CREATED_AT 값을 직렬화한다")
        void serializesCreatedAt() {
            // given
            Instant value = Instant.parse("2024-01-15T10:30:00Z");

            // when
            String serialized = ContentSortFieldJpa.CREATED_AT.serializeCursor(value);

            // then
            assertThat(serialized).isEqualTo("2024-01-15T10:30:00Z");
        }

        @Test
        @DisplayName("RATE 값을 직렬화한다")
        void serializesRate() {
            // given
            Double value = 4.5;

            // when
            String serialized = ContentSortFieldJpa.RATE.serializeCursor(value);

            // then
            assertThat(serialized).isEqualTo("4.5");
        }

        @Test
        @DisplayName("null 값을 빈 문자열로 직렬화한다")
        void serializesNullToEmpty() {
            // when
            String serialized = ContentSortFieldJpa.POPULARITY.serializeCursor(null);

            // then
            assertThat(serialized).isEmpty();
        }
    }

    @Nested
    @DisplayName("deserializeCursor()")
    class DeserializeCursorTest {

        @Test
        @DisplayName("POPULARITY 커서를 역직렬화한다")
        void deserializesPopularity() {
            // given
            String cursor = "123.45";

            // when
            Comparable<?> result = ContentSortFieldJpa.POPULARITY.deserializeCursor(cursor);

            // then
            assertThat(result).isEqualTo(123.45);
        }

        @Test
        @DisplayName("CREATED_AT 커서를 역직렬화한다")
        void deserializesCreatedAt() {
            // given
            String cursor = "2024-01-15T10:30:00Z";

            // when
            Comparable<?> result = ContentSortFieldJpa.CREATED_AT.deserializeCursor(cursor);

            // then
            assertThat(result).isEqualTo(Instant.parse("2024-01-15T10:30:00Z"));
        }

        @Test
        @DisplayName("RATE 커서를 역직렬화한다")
        void deserializesRate() {
            // given
            String cursor = "4.5";

            // when
            Comparable<?> result = ContentSortFieldJpa.RATE.deserializeCursor(cursor);

            // then
            assertThat(result).isEqualTo(4.5);
        }
    }

    @Nested
    @DisplayName("extractValue()")
    class ExtractValueTest {

        @Test
        @DisplayName("ContentEntity에서 popularityScore 값을 추출한다")
        void extractsPopularity() {
            // given
            ContentEntity entity = ContentEntity.builder()
                .popularityScore(123.45)
                .build();

            // when
            Object result = ContentSortFieldJpa.POPULARITY.extractValue(entity);

            // then
            assertThat(result).isEqualTo(123.45);
        }

        @Test
        @DisplayName("ContentEntity에서 createdAt 값을 추출한다")
        void extractsCreatedAt() {
            // given
            Instant createdAt = Instant.parse("2024-01-15T10:30:00Z");
            ContentEntity entity = ContentEntity.builder()
                .createdAt(createdAt)
                .build();

            // when
            Object result = ContentSortFieldJpa.CREATED_AT.extractValue(entity);

            // then
            assertThat(result).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("ContentEntity에서 averageRating 값을 추출한다")
        void extractsRate() {
            // given
            ContentEntity entity = ContentEntity.builder()
                .averageRating(4.5)
                .build();

            // when
            Object result = ContentSortFieldJpa.RATE.extractValue(entity);

            // then
            assertThat(result).isEqualTo(4.5);
        }
    }
}
