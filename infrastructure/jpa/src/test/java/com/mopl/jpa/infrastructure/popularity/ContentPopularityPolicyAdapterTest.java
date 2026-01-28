package com.mopl.jpa.infrastructure.popularity;

import com.mopl.domain.repository.setting.SystemConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContentPopularityPolicyAdapter 단위 테스트")
class ContentPopularityPolicyAdapterTest {

    private static final double DEFAULT_GLOBAL_AVG_RATING = 3.7;
    private static final int DEFAULT_MIN_REVIEW_COUNT = 10;

    @Mock
    private SystemConfigRepository systemConfigRepository;

    private ContentPopularityPolicyAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ContentPopularityPolicyAdapter(systemConfigRepository);
    }

    @Nested
    @DisplayName("globalAverageRating()")
    class GlobalAverageRatingTest {

        @Test
        @DisplayName("설정값이 있으면 파싱하여 반환")
        void withConfigValue_returnsParsedValue() {
            // given
            given(systemConfigRepository.findValue("CONTENT_GLOBAL_AVG_RATING"))
                .willReturn(Optional.of("4.2"));

            // when
            double result = adapter.globalAverageRating();

            // then
            assertThat(result).isEqualTo(4.2);
        }

        @Test
        @DisplayName("설정값이 없으면 기본값 반환")
        void withNoConfigValue_returnsDefault() {
            // given
            given(systemConfigRepository.findValue("CONTENT_GLOBAL_AVG_RATING"))
                .willReturn(Optional.empty());

            // when
            double result = adapter.globalAverageRating();

            // then
            assertThat(result).isEqualTo(DEFAULT_GLOBAL_AVG_RATING);
        }

        @Test
        @DisplayName("파싱 실패 시 기본값 반환")
        void withInvalidValue_returnsDefault() {
            // given
            given(systemConfigRepository.findValue("CONTENT_GLOBAL_AVG_RATING"))
                .willReturn(Optional.of("invalid"));

            // when
            double result = adapter.globalAverageRating();

            // then
            assertThat(result).isEqualTo(DEFAULT_GLOBAL_AVG_RATING);
        }

        @Test
        @DisplayName("빈 문자열이면 기본값 반환")
        void withEmptyString_returnsDefault() {
            // given
            given(systemConfigRepository.findValue("CONTENT_GLOBAL_AVG_RATING"))
                .willReturn(Optional.of(""));

            // when
            double result = adapter.globalAverageRating();

            // then
            assertThat(result).isEqualTo(DEFAULT_GLOBAL_AVG_RATING);
        }
    }

    @Nested
    @DisplayName("minimumReviewCount()")
    class MinimumReviewCountTest {

        @Test
        @DisplayName("설정값이 있으면 파싱하여 반환")
        void withConfigValue_returnsParsedValue() {
            // given
            given(systemConfigRepository.findValue("CONTENT_POPULARITY_M"))
                .willReturn(Optional.of("25"));

            // when
            int result = adapter.minimumReviewCount();

            // then
            assertThat(result).isEqualTo(25);
        }

        @Test
        @DisplayName("설정값이 없으면 기본값 반환")
        void withNoConfigValue_returnsDefault() {
            // given
            given(systemConfigRepository.findValue("CONTENT_POPULARITY_M"))
                .willReturn(Optional.empty());

            // when
            int result = adapter.minimumReviewCount();

            // then
            assertThat(result).isEqualTo(DEFAULT_MIN_REVIEW_COUNT);
        }

        @Test
        @DisplayName("파싱 실패 시 기본값 반환")
        void withInvalidValue_returnsDefault() {
            // given
            given(systemConfigRepository.findValue("CONTENT_POPULARITY_M"))
                .willReturn(Optional.of("not-a-number"));

            // when
            int result = adapter.minimumReviewCount();

            // then
            assertThat(result).isEqualTo(DEFAULT_MIN_REVIEW_COUNT);
        }

        @Test
        @DisplayName("소수점 값이면 기본값 반환")
        void withDecimalValue_returnsDefault() {
            // given
            given(systemConfigRepository.findValue("CONTENT_POPULARITY_M"))
                .willReturn(Optional.of("10.5"));

            // when
            int result = adapter.minimumReviewCount();

            // then
            assertThat(result).isEqualTo(DEFAULT_MIN_REVIEW_COUNT);
        }

        @Test
        @DisplayName("빈 문자열이면 기본값 반환")
        void withEmptyString_returnsDefault() {
            // given
            given(systemConfigRepository.findValue("CONTENT_POPULARITY_M"))
                .willReturn(Optional.of(""));

            // when
            int result = adapter.minimumReviewCount();

            // then
            assertThat(result).isEqualTo(DEFAULT_MIN_REVIEW_COUNT);
        }
    }
}
