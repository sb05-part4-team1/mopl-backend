package com.mopl.domain.support.popularity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DisplayName("ContentPopularityPolicyPort 단위 테스트")
class ContentPopularityPolicyPortTest {

    @Nested
    @DisplayName("calculatePopularityScore() default 메서드")
    class CalculatePopularityScoreTest {

        @Test
        @DisplayName("설정된 정책 값으로 인기도 점수를 계산한다")
        void withConfiguredPolicy_calculatesScore() {
            // given
            ContentPopularityPolicyPort policy = new TestPolicy(3.5, 10);
            int reviewCount = 10;
            double averageRating = 5.0;

            // when
            double result = policy.calculatePopularityScore(reviewCount, averageRating);

            // then
            // (10/(10+10))*5.0 + (10/(10+10))*3.5 = 0.5*5.0 + 0.5*3.5 = 4.25
            assertThat(result).isCloseTo(4.25, within(0.001));
        }

        @Test
        @DisplayName("글로벌 평균이 높으면 인기도 점수도 높아진다")
        void withHighGlobalAverage_increasesScore() {
            // given
            ContentPopularityPolicyPort lowPolicy = new TestPolicy(2.0, 10);
            ContentPopularityPolicyPort highPolicy = new TestPolicy(4.0, 10);
            int reviewCount = 5;
            double averageRating = 3.0;

            // when
            double lowResult = lowPolicy.calculatePopularityScore(reviewCount, averageRating);
            double highResult = highPolicy.calculatePopularityScore(reviewCount, averageRating);

            // then
            assertThat(highResult).isGreaterThan(lowResult);
        }

        @Test
        @DisplayName("minimumReviewCount가 크면 글로벌 평균에 더 가중치가 부여된다")
        void withHighMinReviewCount_moreWeightToGlobalAverage() {
            // given
            ContentPopularityPolicyPort lowMinPolicy = new TestPolicy(3.0, 5);
            ContentPopularityPolicyPort highMinPolicy = new TestPolicy(3.0, 50);
            int reviewCount = 10;
            double averageRating = 5.0;

            // when
            double lowMinResult = lowMinPolicy.calculatePopularityScore(reviewCount, averageRating);
            double highMinResult = highMinPolicy.calculatePopularityScore(reviewCount, averageRating);

            // then
            // lowMinPolicy: (10/(10+5))*5.0 + (5/(10+5))*3.0 ≈ 4.33
            // highMinPolicy: (10/(10+50))*5.0 + (50/(10+50))*3.0 ≈ 3.33
            assertThat(lowMinResult).isGreaterThan(highMinResult);
            assertThat(lowMinResult).isCloseTo(4.33, within(0.01));
            assertThat(highMinResult).isCloseTo(3.33, within(0.01));
        }

        @Test
        @DisplayName("리뷰가 없으면 글로벌 평균을 반환한다")
        void withNoReviews_returnsGlobalAverage() {
            // given
            ContentPopularityPolicyPort policy = new TestPolicy(3.5, 10);
            int reviewCount = 0;
            double averageRating = 0.0;

            // when
            double result = policy.calculatePopularityScore(reviewCount, averageRating);

            // then
            assertThat(result).isCloseTo(3.5, within(0.001));
        }

        @Test
        @DisplayName("리뷰 수가 매우 많으면 평균 평점에 수렴한다")
        void withManyReviews_convergesToAverageRating() {
            // given
            ContentPopularityPolicyPort policy = new TestPolicy(3.0, 10);
            int reviewCount = 10000;
            double averageRating = 4.8;

            // when
            double result = policy.calculatePopularityScore(reviewCount, averageRating);

            // then
            assertThat(result).isCloseTo(4.8, within(0.01));
        }
    }

    @Nested
    @DisplayName("정책 인터페이스 구현")
    class PolicyImplementationTest {

        @Test
        @DisplayName("globalAverageRating()이 올바르게 반환된다")
        void globalAverageRating_returnsCorrectValue() {
            // given
            ContentPopularityPolicyPort policy = new TestPolicy(4.2, 15);

            // when & then
            assertThat(policy.globalAverageRating()).isEqualTo(4.2);
        }

        @Test
        @DisplayName("minimumReviewCount()가 올바르게 반환된다")
        void minimumReviewCount_returnsCorrectValue() {
            // given
            ContentPopularityPolicyPort policy = new TestPolicy(3.5, 20);

            // when & then
            assertThat(policy.minimumReviewCount()).isEqualTo(20);
        }
    }

    private record TestPolicy(
        double globalAverageRating,
        int minimumReviewCount
    ) implements ContentPopularityPolicyPort {
    }
}
