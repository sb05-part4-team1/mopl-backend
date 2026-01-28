package com.mopl.domain.support.popularity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DisplayName("PopularityScoreCalculator 단위 테스트")
class PopularityScoreCalculatorTest {

    @Nested
    @DisplayName("Bayesian Average 계산")
    class BayesianAverageTest {

        @Test
        @DisplayName("리뷰가 없으면 글로벌 평균 평점을 반환한다")
        void withNoReviews_returnsGlobalAverage() {
            // given
            int reviewCount = 0;
            double averageRating = 0.0;
            double globalAverageRating = 3.5;
            int minimumReviewCount = 10;

            // when
            double result = PopularityScoreCalculator.calculate(
                reviewCount, averageRating, globalAverageRating, minimumReviewCount
            );

            // then
            // (0/(0+10))*0 + (10/(0+10))*3.5 = 3.5
            assertThat(result).isCloseTo(3.5, within(0.001));
        }

        @Test
        @DisplayName("리뷰 수가 minimumReviewCount와 같으면 두 평균의 중간값을 반환한다")
        void withEqualReviewCount_returnsMiddleValue() {
            // given
            int reviewCount = 10;
            double averageRating = 5.0;
            double globalAverageRating = 3.0;
            int minimumReviewCount = 10;

            // when
            double result = PopularityScoreCalculator.calculate(
                reviewCount, averageRating, globalAverageRating, minimumReviewCount
            );

            // then
            // (10/(10+10))*5.0 + (10/(10+10))*3.0 = 0.5*5.0 + 0.5*3.0 = 4.0
            assertThat(result).isCloseTo(4.0, within(0.001));
        }

        @Test
        @DisplayName("리뷰 수가 많으면 평균 평점에 수렴한다")
        void withManyReviews_convergesToAverageRating() {
            // given
            int reviewCount = 1000;
            double averageRating = 4.8;
            double globalAverageRating = 3.0;
            int minimumReviewCount = 10;

            // when
            double result = PopularityScoreCalculator.calculate(
                reviewCount, averageRating, globalAverageRating, minimumReviewCount
            );

            // then
            // (1000/(1000+10))*4.8 + (10/(1000+10))*3.0 ≈ 4.782
            assertThat(result).isCloseTo(4.8, within(0.05));
        }

        @Test
        @DisplayName("리뷰 수가 적으면 글로벌 평균에 더 가깝다")
        void withFewReviews_closerToGlobalAverage() {
            // given
            int reviewCount = 2;
            double averageRating = 5.0;
            double globalAverageRating = 3.0;
            int minimumReviewCount = 10;

            // when
            double result = PopularityScoreCalculator.calculate(
                reviewCount, averageRating, globalAverageRating, minimumReviewCount
            );

            // then
            // (2/(2+10))*5.0 + (10/(2+10))*3.0 = (2/12)*5.0 + (10/12)*3.0
            // ≈ 0.167*5.0 + 0.833*3.0 ≈ 0.835 + 2.499 ≈ 3.33
            assertThat(result).isCloseTo(3.33, within(0.01));
            assertThat(result).isLessThan(averageRating);
            assertThat(result).isGreaterThan(globalAverageRating);
        }

        @ParameterizedTest
        @CsvSource({
            "100, 4.5, 3.5, 10, 4.409",  // 많은 리뷰
            "50, 4.0, 3.5, 10, 3.917",   // 중간 리뷰
            "10, 4.0, 3.5, 10, 3.75",    // 최소 리뷰
            "5, 4.0, 3.5, 10, 3.667"     // 적은 리뷰
        })
        @DisplayName("다양한 입력에 대해 올바른 점수를 계산한다")
        void withVariousInputs_calculatesCorrectScore(
            int reviewCount,
            double averageRating,
            double globalAverageRating,
            int minimumReviewCount,
            double expectedScore
        ) {
            // when
            double result = PopularityScoreCalculator.calculate(
                reviewCount, averageRating, globalAverageRating, minimumReviewCount
            );

            // then
            assertThat(result).isCloseTo(expectedScore, within(0.01));
        }
    }

    @Nested
    @DisplayName("minimumReviewCount 경계값 처리")
    class MinimumReviewCountTest {

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -10})
        @DisplayName("minimumReviewCount가 0 이하면 1로 처리된다")
        void withZeroOrNegativeMinReviewCount_treatedAsOne(int minReviewCount) {
            // given
            int reviewCount = 1;
            double averageRating = 5.0;
            double globalAverageRating = 3.0;

            // when
            double result = PopularityScoreCalculator.calculate(
                reviewCount, averageRating, globalAverageRating, minReviewCount
            );

            // then
            // minReviewCount=1로 처리: (1/(1+1))*5.0 + (1/(1+1))*3.0 = 0.5*5.0 + 0.5*3.0 = 4.0
            assertThat(result).isCloseTo(4.0, within(0.001));
        }

        @Test
        @DisplayName("minimumReviewCount가 1이면 그대로 사용된다")
        void withMinReviewCountOne_usedAsIs() {
            // given
            int reviewCount = 1;
            double averageRating = 5.0;
            double globalAverageRating = 3.0;
            int minimumReviewCount = 1;

            // when
            double result = PopularityScoreCalculator.calculate(
                reviewCount, averageRating, globalAverageRating, minimumReviewCount
            );

            // then
            // (1/(1+1))*5.0 + (1/(1+1))*3.0 = 4.0
            assertThat(result).isCloseTo(4.0, within(0.001));
        }

        @Test
        @DisplayName("minimumReviewCount가 크면 글로벌 평균에 더 가중치가 부여된다")
        void withLargeMinReviewCount_moreWeightToGlobalAverage() {
            // given
            int reviewCount = 10;
            double averageRating = 5.0;
            double globalAverageRating = 3.0;
            int smallMinReviewCount = 5;
            int largeMinReviewCount = 50;

            // when
            double resultSmall = PopularityScoreCalculator.calculate(
                reviewCount, averageRating, globalAverageRating, smallMinReviewCount
            );
            double resultLarge = PopularityScoreCalculator.calculate(
                reviewCount, averageRating, globalAverageRating, largeMinReviewCount
            );

            // then
            // 큰 minimumReviewCount일수록 글로벌 평균에 가까워짐
            assertThat(resultSmall).isGreaterThan(resultLarge);
            assertThat(resultLarge).isCloseTo(globalAverageRating, within(0.5));
        }
    }

    @Nested
    @DisplayName("극단적인 값 처리")
    class EdgeCaseTest {

        @Test
        @DisplayName("평균 평점이 0이면 글로벌 평균만 반영된다")
        void withZeroAverageRating_reflectsOnlyGlobalAverage() {
            // given
            int reviewCount = 10;
            double averageRating = 0.0;
            double globalAverageRating = 3.5;
            int minimumReviewCount = 10;

            // when
            double result = PopularityScoreCalculator.calculate(
                reviewCount, averageRating, globalAverageRating, minimumReviewCount
            );

            // then
            // (10/(10+10))*0.0 + (10/(10+10))*3.5 = 0.5*0.0 + 0.5*3.5 = 1.75
            assertThat(result).isCloseTo(1.75, within(0.001));
        }

        @Test
        @DisplayName("글로벌 평균이 0이면 평균 평점만 반영된다")
        void withZeroGlobalAverage_reflectsOnlyAverageRating() {
            // given
            int reviewCount = 10;
            double averageRating = 4.0;
            double globalAverageRating = 0.0;
            int minimumReviewCount = 10;

            // when
            double result = PopularityScoreCalculator.calculate(
                reviewCount, averageRating, globalAverageRating, minimumReviewCount
            );

            // then
            // (10/(10+10))*4.0 + (10/(10+10))*0.0 = 0.5*4.0 + 0.5*0.0 = 2.0
            assertThat(result).isCloseTo(2.0, within(0.001));
        }

        @Test
        @DisplayName("매우 큰 리뷰 수에서도 정상 동작한다")
        void withVeryLargeReviewCount_worksCorrectly() {
            // given
            int reviewCount = Integer.MAX_VALUE / 2;
            double averageRating = 4.5;
            double globalAverageRating = 3.0;
            int minimumReviewCount = 10;

            // when
            double result = PopularityScoreCalculator.calculate(
                reviewCount, averageRating, globalAverageRating, minimumReviewCount
            );

            // then
            // 리뷰 수가 매우 많으면 averageRating에 거의 수렴
            assertThat(result).isCloseTo(averageRating, within(0.001));
        }
    }
}
