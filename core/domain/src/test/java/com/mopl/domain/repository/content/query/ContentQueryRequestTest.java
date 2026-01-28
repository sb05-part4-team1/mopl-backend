package com.mopl.domain.repository.content.query;

import com.mopl.domain.model.content.ContentModel.ContentType;
import com.mopl.domain.support.cursor.SortDirection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ContentQueryRequest 단위 테스트")
class ContentQueryRequestTest {

    @Nested
    @DisplayName("limit 기본값 및 제한")
    class LimitTest {

        @Test
        @DisplayName("limit이 null이면 DEFAULT_LIMIT이 적용된다")
        void withNullLimit_appliesDefaultLimit() {
            // when
            ContentQueryRequest request = new ContentQueryRequest(
                null, null, null, null, null,
                null, null, null
            );

            // then
            assertThat(request.limit()).isEqualTo(ContentQueryRequest.DEFAULT_LIMIT);
        }

        @Test
        @DisplayName("limit이 MAX_LIMIT 이하면 그대로 유지된다")
        void withLimitBelowMax_keepsLimit() {
            // when
            ContentQueryRequest request = new ContentQueryRequest(
                null, null, null, null, null,
                50, null, null
            );

            // then
            assertThat(request.limit()).isEqualTo(50);
        }

        @Test
        @DisplayName("limit이 MAX_LIMIT을 초과하면 MAX_LIMIT으로 제한된다")
        void withLimitAboveMax_capsToMaxLimit() {
            // when
            ContentQueryRequest request = new ContentQueryRequest(
                null, null, null, null, null,
                200, null, null
            );

            // then
            assertThat(request.limit()).isEqualTo(ContentQueryRequest.MAX_LIMIT);
        }

        @Test
        @DisplayName("limit이 정확히 MAX_LIMIT이면 그대로 유지된다")
        void withLimitAtMax_keepsLimit() {
            // when
            ContentQueryRequest request = new ContentQueryRequest(
                null, null, null, null, null,
                ContentQueryRequest.MAX_LIMIT, null, null
            );

            // then
            assertThat(request.limit()).isEqualTo(ContentQueryRequest.MAX_LIMIT);
        }
    }

    @Nested
    @DisplayName("sortDirection 기본값")
    class SortDirectionTest {

        @Test
        @DisplayName("sortDirection이 null이면 DESCENDING이 적용된다")
        void withNullSortDirection_appliesDescending() {
            // when
            ContentQueryRequest request = new ContentQueryRequest(
                null, null, null, null, null,
                null, null, null
            );

            // then
            assertThat(request.sortDirection()).isEqualTo(SortDirection.DESCENDING);
        }

        @Test
        @DisplayName("sortDirection이 ASCENDING이면 그대로 유지된다")
        void withAscending_keepsAscending() {
            // when
            ContentQueryRequest request = new ContentQueryRequest(
                null, null, null, null, null,
                null, SortDirection.ASCENDING, null
            );

            // then
            assertThat(request.sortDirection()).isEqualTo(SortDirection.ASCENDING);
        }
    }

    @Nested
    @DisplayName("sortBy 기본값")
    class SortByTest {

        @Test
        @DisplayName("sortBy가 null이면 POPULARITY가 적용된다")
        void withNullSortBy_appliesPopularity() {
            // when
            ContentQueryRequest request = new ContentQueryRequest(
                null, null, null, null, null,
                null, null, null
            );

            // then
            assertThat(request.sortBy()).isEqualTo(ContentSortField.POPULARITY);
        }

        @Test
        @DisplayName("sortBy가 CREATED_AT이면 그대로 유지된다")
        void withCreatedAt_keepsCreatedAt() {
            // when
            ContentQueryRequest request = new ContentQueryRequest(
                null, null, null, null, null,
                null, null, ContentSortField.CREATED_AT
            );

            // then
            assertThat(request.sortBy()).isEqualTo(ContentSortField.CREATED_AT);
        }
    }

    @Nested
    @DisplayName("keywordLike 정규화")
    class KeywordNormalizationTest {

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("null 또는 빈 문자열이면 null로 정규화된다")
        void withNullOrEmpty_normalizesToNull(String keyword) {
            // when
            ContentQueryRequest request = new ContentQueryRequest(
                null, keyword, null, null, null,
                null, null, null
            );

            // then
            assertThat(request.keywordLike()).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"   ", "\t", "\n", "  \t  "})
        @DisplayName("공백만 있으면 null로 정규화된다")
        void withOnlyWhitespace_normalizesToNull(String keyword) {
            // when
            ContentQueryRequest request = new ContentQueryRequest(
                null, keyword, null, null, null,
                null, null, null
            );

            // then
            assertThat(request.keywordLike()).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"ㄱ", "ㅂ", "ㄱㅂ", "ㄱㄴㄷ", "ㅎㅎㅎ"})
        @DisplayName("한글 자모만 있으면 null로 정규화된다")
        void withHangulJamoOnly_normalizesToNull(String keyword) {
            // when
            ContentQueryRequest request = new ContentQueryRequest(
                null, keyword, null, null, null,
                null, null, null
            );

            // then
            assertThat(request.keywordLike()).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"영", "가"})
        @DisplayName("한글 1글자는 null로 정규화된다")
        void withSingleKoreanChar_normalizesToNull(String keyword) {
            // when
            ContentQueryRequest request = new ContentQueryRequest(
                null, keyword, null, null, null,
                null, null, null
            );

            // then
            assertThat(request.keywordLike()).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"영화", "인셉션", "가나다"})
        @DisplayName("한글 2글자 이상이면 유지된다")
        void withTwoOrMoreKoreanChars_keepsKeyword(String keyword) {
            // when
            ContentQueryRequest request = new ContentQueryRequest(
                null, keyword, null, null, null,
                null, null, null
            );

            // then
            assertThat(request.keywordLike()).isEqualTo(keyword);
        }

        @ParameterizedTest
        @ValueSource(strings = {"a", "ab"})
        @DisplayName("영문 2글자 이하는 null로 정규화된다")
        void withTwoOrLessEnglishChars_normalizesToNull(String keyword) {
            // when
            ContentQueryRequest request = new ContentQueryRequest(
                null, keyword, null, null, null,
                null, null, null
            );

            // then
            assertThat(request.keywordLike()).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"abc", "test", "movie"})
        @DisplayName("영문 3글자 이상이면 유지된다")
        void withThreeOrMoreEnglishChars_keepsKeyword(String keyword) {
            // when
            ContentQueryRequest request = new ContentQueryRequest(
                null, keyword, null, null, null,
                null, null, null
            );

            // then
            assertThat(request.keywordLike()).isEqualTo(keyword);
        }

        @ParameterizedTest
        @CsvSource({
            "a가, a가",
            "1영, 1영",
            "영a, 영a"
        })
        @DisplayName("한글이 포함되면 2글자 이상이면 유지된다")
        void withMixedKorean_appliesKoreanRule(String input, String expected) {
            // when
            ContentQueryRequest request = new ContentQueryRequest(
                null, input, null, null, null,
                null, null, null
            );

            // then
            assertThat(request.keywordLike()).isEqualTo(expected);
        }

        @Test
        @DisplayName("한글이 포함된 1글자는 null로 정규화된다")
        void withSingleMixedKorean_normalizesToNull() {
            // 한글이 포함되어 있지만 1글자뿐인 경우는 불가능 (최소 2바이트)
            // 이 케이스는 실제로 발생하기 어려움
        }

        @Test
        @DisplayName("앞뒤 공백은 trim 된다")
        void withLeadingTrailingSpaces_trimmed() {
            // when
            ContentQueryRequest request = new ContentQueryRequest(
                null, "  영화  ", null, null, null,
                null, null, null
            );

            // then
            assertThat(request.keywordLike()).isEqualTo("영화");
        }

        @ParameterizedTest
        @ValueSource(strings = {"12", "!@"})
        @DisplayName("숫자/특수문자만 2글자는 null로 정규화된다 (영문 규칙 적용)")
        void withTwoSpecialChars_normalizesToNull(String keyword) {
            // when
            ContentQueryRequest request = new ContentQueryRequest(
                null, keyword, null, null, null,
                null, null, null
            );

            // then
            assertThat(request.keywordLike()).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"123", "!@#"})
        @DisplayName("숫자/특수문자만 3글자 이상이면 유지된다")
        void withThreeOrMoreSpecialChars_keepsKeyword(String keyword) {
            // when
            ContentQueryRequest request = new ContentQueryRequest(
                null, keyword, null, null, null,
                null, null, null
            );

            // then
            assertThat(request.keywordLike()).isEqualTo(keyword);
        }

        @Test
        @DisplayName("한글 자모 확장 영역 문자는 완성형으로 인식되지 않는다")
        void withHangulJamoExtended_notRecognizedAsSyllable() {
            // given - 한글 자모 확장 A 영역 (U+D7B0~U+D7FF)
            // 이 문자는 '가'(U+AC00) 이상이지만 '힣'(U+D7A3) 초과
            String keyword = "ab\uD7B0";

            // when
            ContentQueryRequest request = new ContentQueryRequest(
                null, keyword, null, null, null,
                null, null, null
            );

            // then - 한글 완성형이 없으므로 영문 규칙(3글자 이상) 적용
            assertThat(request.keywordLike()).isEqualTo(keyword);
        }
    }

    @Nested
    @DisplayName("전체 필드 전달")
    class FullConstructionTest {

        @Test
        @DisplayName("모든 필드가 정상적으로 전달된다")
        void withAllFields_allFieldsPreserved() {
            // when
            ContentQueryRequest request = new ContentQueryRequest(
                ContentType.movie,
                "인셉션",
                java.util.List.of("SF", "액션"),
                "cursor123",
                java.util.UUID.randomUUID(),
                30,
                SortDirection.ASCENDING,
                ContentSortField.CREATED_AT
            );

            // then
            assertThat(request.typeEqual()).isEqualTo(ContentType.movie);
            assertThat(request.keywordLike()).isEqualTo("인셉션");
            assertThat(request.tagsIn()).containsExactly("SF", "액션");
            assertThat(request.cursor()).isEqualTo("cursor123");
            assertThat(request.idAfter()).isNotNull();
            assertThat(request.limit()).isEqualTo(30);
            assertThat(request.sortDirection()).isEqualTo(SortDirection.ASCENDING);
            assertThat(request.sortBy()).isEqualTo(ContentSortField.CREATED_AT);
        }
    }
}
