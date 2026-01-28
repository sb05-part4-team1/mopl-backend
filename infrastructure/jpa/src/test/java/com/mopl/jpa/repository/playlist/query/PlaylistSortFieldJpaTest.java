package com.mopl.jpa.repository.playlist.query;

import com.mopl.domain.repository.playlist.PlaylistSortField;
import com.mopl.jpa.entity.playlist.PlaylistEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PlaylistSortFieldJpa 테스트")
class PlaylistSortFieldJpaTest {

    @Nested
    @DisplayName("from()")
    class FromTest {

        @Test
        @DisplayName("UPDATED_AT 필드를 변환한다")
        void convertsUpdatedAt() {
            // when
            PlaylistSortFieldJpa result = PlaylistSortFieldJpa.from(PlaylistSortField.UPDATED_AT);

            // then
            assertThat(result).isEqualTo(PlaylistSortFieldJpa.UPDATED_AT);
            assertThat(result.getFieldName()).isEqualTo("UPDATED_AT");
        }

        @Test
        @DisplayName("SUBSCRIBER_COUNT 필드를 변환한다")
        void convertsSubscriberCount() {
            // when
            PlaylistSortFieldJpa result = PlaylistSortFieldJpa.from(PlaylistSortField.SUBSCRIBER_COUNT);

            // then
            assertThat(result).isEqualTo(PlaylistSortFieldJpa.SUBSCRIBER_COUNT);
            assertThat(result.getFieldName()).isEqualTo("SUBSCRIBER_COUNT");
        }
    }

    @Nested
    @DisplayName("serializeCursor()")
    class SerializeCursorTest {

        @Test
        @DisplayName("UPDATED_AT 값을 직렬화한다")
        void serializesUpdatedAt() {
            // given
            Instant value = Instant.parse("2024-01-15T10:30:00Z");

            // when
            String serialized = PlaylistSortFieldJpa.UPDATED_AT.serializeCursor(value);

            // then
            assertThat(serialized).isEqualTo("2024-01-15T10:30:00Z");
        }

        @Test
        @DisplayName("SUBSCRIBER_COUNT 값을 직렬화한다")
        void serializesSubscriberCount() {
            // given
            Integer value = 42;

            // when
            String serialized = PlaylistSortFieldJpa.SUBSCRIBER_COUNT.serializeCursor(value);

            // then
            assertThat(serialized).isEqualTo("42");
        }

        @Test
        @DisplayName("null 값을 빈 문자열로 직렬화한다")
        void serializesNullToEmpty() {
            // when
            String serialized = PlaylistSortFieldJpa.UPDATED_AT.serializeCursor(null);

            // then
            assertThat(serialized).isEmpty();
        }
    }

    @Nested
    @DisplayName("deserializeCursor()")
    class DeserializeCursorTest {

        @Test
        @DisplayName("UPDATED_AT 커서를 역직렬화한다")
        void deserializesUpdatedAt() {
            // given
            String cursor = "2024-01-15T10:30:00Z";

            // when
            Comparable<?> result = PlaylistSortFieldJpa.UPDATED_AT.deserializeCursor(cursor);

            // then
            assertThat(result).isEqualTo(Instant.parse("2024-01-15T10:30:00Z"));
        }

        @Test
        @DisplayName("SUBSCRIBER_COUNT 커서를 역직렬화한다")
        void deserializesSubscriberCount() {
            // given
            String cursor = "42";

            // when
            Comparable<?> result = PlaylistSortFieldJpa.SUBSCRIBER_COUNT.deserializeCursor(cursor);

            // then
            assertThat(result).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("extractValue()")
    class ExtractValueTest {

        @Test
        @DisplayName("PlaylistEntity에서 updatedAt 값을 추출한다")
        void extractsUpdatedAt() {
            // given
            Instant updatedAt = Instant.parse("2024-01-15T10:30:00Z");
            PlaylistEntity entity = PlaylistEntity.builder()
                .updatedAt(updatedAt)
                .build();

            // when
            Object result = PlaylistSortFieldJpa.UPDATED_AT.extractValue(entity);

            // then
            assertThat(result).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("PlaylistEntity에서 subscriberCount 값을 추출한다")
        void extractsSubscriberCount() {
            // given
            PlaylistEntity entity = PlaylistEntity.builder()
                .subscriberCount(15)
                .build();

            // when
            Object result = PlaylistSortFieldJpa.SUBSCRIBER_COUNT.extractValue(entity);

            // then
            assertThat(result).isEqualTo(15);
        }
    }
}
