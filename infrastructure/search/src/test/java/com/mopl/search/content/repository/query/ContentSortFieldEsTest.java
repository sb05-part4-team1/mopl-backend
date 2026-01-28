package com.mopl.search.content.repository.query;

import com.mopl.domain.repository.content.query.ContentSortField;
import com.mopl.search.document.ContentDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ContentSortFieldEs 단위 테스트")
class ContentSortFieldEsTest {

    @Nested
    @DisplayName("from()")
    class FromTest {

        @ParameterizedTest
        @EnumSource(ContentSortField.class)
        @DisplayName("모든 도메인 정렬 필드를 ES 정렬 필드로 변환")
        void withAllDomainFields_convertsCorrectly(ContentSortField domainField) {
            // when
            ContentSortFieldEs result = ContentSortFieldEs.from(domainField);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getDomainField()).isEqualTo(domainField);
        }

        @Test
        @DisplayName("CREATED_AT -> createdAt ES 필드 매핑")
        void withCreatedAt_returnsCorrectEsField() {
            // when
            ContentSortFieldEs result = ContentSortFieldEs.from(ContentSortField.CREATED_AT);

            // then
            assertThat(result).isEqualTo(ContentSortFieldEs.CREATED_AT);
            assertThat(result.getEsField()).isEqualTo("createdAt");
        }

        @Test
        @DisplayName("POPULARITY -> popularityScore ES 필드 매핑")
        void withPopularity_returnsCorrectEsField() {
            // when
            ContentSortFieldEs result = ContentSortFieldEs.from(ContentSortField.POPULARITY);

            // then
            assertThat(result).isEqualTo(ContentSortFieldEs.POPULARITY);
            assertThat(result.getEsField()).isEqualTo("popularityScore");
        }

        @Test
        @DisplayName("RATE -> averageRating ES 필드 매핑")
        void withRate_returnsCorrectEsField() {
            // when
            ContentSortFieldEs result = ContentSortFieldEs.from(ContentSortField.RATE);

            // then
            assertThat(result).isEqualTo(ContentSortFieldEs.RATE);
            assertThat(result.getEsField()).isEqualTo("averageRating");
        }
    }

    @Nested
    @DisplayName("serialize() / deserialize()")
    class SerializationTest {

        @Test
        @DisplayName("POPULARITY 직렬화/역직렬화")
        void withPopularity_serializesAndDeserializes() {
            // given
            ContentSortFieldEs field = ContentSortFieldEs.POPULARITY;
            Double value = 85.5;

            // when
            String serialized = field.serialize(value);
            Object deserialized = field.deserialize(serialized);

            // then
            assertThat(serialized).isEqualTo("85.5");
            assertThat(deserialized).isEqualTo(85.5);
        }

        @Test
        @DisplayName("RATE 직렬화/역직렬화")
        void withRate_serializesAndDeserializes() {
            // given
            ContentSortFieldEs field = ContentSortFieldEs.RATE;
            Double value = 4.7;

            // when
            String serialized = field.serialize(value);
            Object deserialized = field.deserialize(serialized);

            // then
            assertThat(serialized).isEqualTo("4.7");
            assertThat(deserialized).isEqualTo(4.7);
        }

        @Test
        @DisplayName("CREATED_AT 직렬화/역직렬화")
        void withCreatedAt_serializesAndDeserializes() {
            // given
            ContentSortFieldEs field = ContentSortFieldEs.CREATED_AT;
            Instant value = Instant.parse("2024-01-15T10:30:00Z");

            // when
            String serialized = field.serialize(value);
            Object deserialized = field.deserialize(serialized);

            // then
            assertThat(serialized).isEqualTo("2024-01-15T10:30:00Z");
            assertThat(deserialized).isEqualTo(value);
        }

        @Test
        @DisplayName("null 값 직렬화 시 빈 문자열 반환")
        void withNull_returnsEmptyString() {
            // given
            ContentSortFieldEs field = ContentSortFieldEs.POPULARITY;

            // when
            String serialized = field.serialize(null);

            // then
            assertThat(serialized).isEmpty();
        }
    }

    @Nested
    @DisplayName("extract()")
    class ExtractTest {

        @Test
        @DisplayName("POPULARITY 필드 값 추출")
        void withPopularity_extractsValue() {
            // given
            ContentSortFieldEs field = ContentSortFieldEs.POPULARITY;
            ContentDocument doc = ContentDocument.builder()
                .popularityScore(85.0)
                .build();

            // when
            Object result = field.extract(doc);

            // then
            assertThat(result).isEqualTo(85.0);
        }

        @Test
        @DisplayName("RATE 필드 값 추출")
        void withRate_extractsValue() {
            // given
            ContentSortFieldEs field = ContentSortFieldEs.RATE;
            ContentDocument doc = ContentDocument.builder()
                .averageRating(4.5)
                .build();

            // when
            Object result = field.extract(doc);

            // then
            assertThat(result).isEqualTo(4.5);
        }

        @Test
        @DisplayName("CREATED_AT 필드 값 추출")
        void withCreatedAt_extractsValue() {
            // given
            ContentSortFieldEs field = ContentSortFieldEs.CREATED_AT;
            Instant now = Instant.now();
            ContentDocument doc = ContentDocument.builder()
                .createdAt(now)
                .build();

            // when
            Object result = field.extract(doc);

            // then
            assertThat(result).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("fieldName()")
    class FieldNameTest {

        @ParameterizedTest
        @EnumSource(ContentSortFieldEs.class)
        @DisplayName("도메인 필드명 반환")
        void withAnyField_returnsDomainFieldName(ContentSortFieldEs esField) {
            // when
            String result = esField.fieldName();

            // then
            assertThat(result).isEqualTo(esField.getDomainField().name());
        }
    }
}
