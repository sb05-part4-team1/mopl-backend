package com.mopl.redis.repository.watchingsession.query;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.domain.repository.watchingsession.WatchingSessionSortField;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WatchingSessionSortFieldSupport 단위 테스트")
class WatchingSessionSortFieldSupportTest {

    @Nested
    @DisplayName("from()")
    class FromTest {

        @Test
        @DisplayName("createdAt 도메인 필드를 CREATED_AT으로 변환")
        void withCreatedAt_returnsCreatedAtSupport() {
            // when
            WatchingSessionSortFieldSupport result = WatchingSessionSortFieldSupport.from(
                WatchingSessionSortField.CREATED_AT
            );

            // then
            assertThat(result).isEqualTo(WatchingSessionSortFieldSupport.CREATED_AT);
        }
    }

    @Nested
    @DisplayName("extractValue()")
    class ExtractValueTest {

        @Test
        @DisplayName("모델에서 createdAt 값 추출")
        void withModel_extractsCreatedAt() {
            // given
            Instant createdAt = Instant.parse("2024-01-01T12:00:00Z");
            WatchingSessionModel model = WatchingSessionModel.builder()
                .watcherId(UUID.randomUUID())
                .contentId(UUID.randomUUID())
                .createdAt(createdAt)
                .build();

            // when
            Instant result = WatchingSessionSortFieldSupport.CREATED_AT.extractValue(model);

            // then
            assertThat(result).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("createdAt이 null인 모델에서 null 반환")
        void withNullCreatedAt_returnsNull() {
            // given
            WatchingSessionModel model = WatchingSessionModel.builder()
                .watcherId(UUID.randomUUID())
                .contentId(UUID.randomUUID())
                .createdAt(null)
                .build();

            // when
            Instant result = WatchingSessionSortFieldSupport.CREATED_AT.extractValue(model);

            // then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("serializeCursor()")
    class SerializeCursorTest {

        @Test
        @DisplayName("Instant를 문자열로 직렬화")
        void withInstant_returnsString() {
            // given
            Instant instant = Instant.parse("2024-01-01T12:00:00Z");

            // when
            String result = WatchingSessionSortFieldSupport.CREATED_AT.serializeCursor(instant);

            // then
            assertThat(result).isEqualTo("2024-01-01T12:00:00Z");
        }

        @Test
        @DisplayName("null 값은 빈 문자열로 직렬화")
        void withNull_returnsEmptyString() {
            // when
            String result = WatchingSessionSortFieldSupport.CREATED_AT.serializeCursor(null);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("deserializeCursor()")
    class DeserializeCursorTest {

        @Test
        @DisplayName("유효한 문자열을 Instant로 역직렬화")
        void withValidString_returnsInstant() {
            // given
            String cursor = "2024-01-01T12:00:00Z";

            // when
            Instant result = WatchingSessionSortFieldSupport.CREATED_AT.deserializeCursor(cursor);

            // then
            assertThat(result).isEqualTo(Instant.parse("2024-01-01T12:00:00Z"));
        }

        @Test
        @DisplayName("null 커서는 null 반환")
        void withNull_returnsNull() {
            // when
            Instant result = WatchingSessionSortFieldSupport.CREATED_AT.deserializeCursor(null);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("빈 문자열은 null 반환")
        void withEmptyString_returnsNull() {
            // when
            Instant result = WatchingSessionSortFieldSupport.CREATED_AT.deserializeCursor("");

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("공백 문자열은 null 반환")
        void withBlankString_returnsNull() {
            // when
            Instant result = WatchingSessionSortFieldSupport.CREATED_AT.deserializeCursor("   ");

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("잘못된 형식의 문자열은 null 반환")
        void withInvalidFormat_returnsNull() {
            // when
            Instant result = WatchingSessionSortFieldSupport.CREATED_AT.deserializeCursor("invalid-date");

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("앞뒤 공백이 있는 문자열도 파싱")
        void withWhitespace_trimsAndParses() {
            // given
            String cursor = "  2024-01-01T12:00:00Z  ";

            // when
            Instant result = WatchingSessionSortFieldSupport.CREATED_AT.deserializeCursor(cursor);

            // then
            assertThat(result).isEqualTo(Instant.parse("2024-01-01T12:00:00Z"));
        }
    }

    @Nested
    @DisplayName("getFieldName()")
    class GetFieldNameTest {

        @Test
        @DisplayName("도메인 필드 이름 반환")
        void returnsFieldName() {
            // when
            String result = WatchingSessionSortFieldSupport.CREATED_AT.getFieldName();

            // then
            assertThat(result).isEqualTo("createdAt");
        }
    }

    @Nested
    @DisplayName("getDomainField()")
    class GetDomainFieldTest {

        @Test
        @DisplayName("도메인 필드 반환")
        void returnsDomainField() {
            // when
            WatchingSessionSortField result = WatchingSessionSortFieldSupport.CREATED_AT.getDomainField();

            // then
            assertThat(result).isEqualTo(WatchingSessionSortField.CREATED_AT);
        }
    }
}
