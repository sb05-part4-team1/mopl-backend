package com.mopl.domain.model.review;

import com.mopl.domain.exception.review.InvalidReviewDataException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ReviewModel 단위 테스트")
class ReviewModelTest {

    @Nested
    @DisplayName("create()")
    class CreateTest {

        @Test
        @DisplayName("유효한 값이면 리뷰 모델을 생성한다")
        void withValidData_createsReview() {
            // given
            UUID contentId = UUID.randomUUID();
            UUID authorId = UUID.randomUUID();

            String text = "리뷰 내용";
            BigDecimal rating = BigDecimal.valueOf(4);

            // when
            ReviewModel review = ReviewModel.create(contentId, authorId, text, rating);

            // then
            assertThat(review.getContentId()).isEqualTo(contentId);
            assertThat(review.getAuthorId()).isEqualTo(authorId);
            assertThat(review.getText()).isEqualTo(text);
            assertThat(review.getRating()).isEqualTo(rating);
        }

        @Test
        @DisplayName("contentId가 null이면 예외 발생")
        void withNullContentId_throwsException() {
            // given
            UUID authorId = UUID.randomUUID();

            // when & then
            assertThatThrownBy(() -> ReviewModel.create(null, authorId, "text", BigDecimal.ONE))
                .isInstanceOf(InvalidReviewDataException.class)
                .satisfies(e -> {
                    InvalidReviewDataException ex = (InvalidReviewDataException) e;
                    // [수정] 모델의 실제 메시지와 일치시킴
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("콘텐츠 ID는 null일 수 없습니다.");
                });
        }

        @Test
        @DisplayName("authorId가 null이면 예외 발생")
        void withNullAuthor_throwsException() {
            // when & then
            assertThatThrownBy(() -> ReviewModel.create(UUID.randomUUID(), null, "text",
                BigDecimal.ONE))
                .isInstanceOf(InvalidReviewDataException.class)
                .satisfies(e -> {
                    InvalidReviewDataException ex = (InvalidReviewDataException) e;
                    // [수정] 모델의 실제 메시지와 일치시킴
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("작성자 ID는 null일 수 없습니다.");
                });
        }

        @Test
        @DisplayName("rating이 null이면 예외 발생")
        void withNullRating_throwsException() {
            // given
            UUID authorId = UUID.randomUUID();

            // when & then
            assertThatThrownBy(() -> ReviewModel.create(UUID.randomUUID(), authorId, "text", null))
                .isInstanceOf(InvalidReviewDataException.class)
                .satisfies(e -> {
                    InvalidReviewDataException ex = (InvalidReviewDataException) e;
                    // [수정] 모델의 실제 메시지와 일치시킴
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("평점은 null일 수 없습니다.");
                });
        }

        @Test
        @DisplayName("text가 10,000자를 초과하면 예외 발생")
        void withTooLongText_throwsException() {
            // given
            UUID contentId = UUID.randomUUID();
            UUID authorId = UUID.randomUUID();
            String longText = "a".repeat(ReviewModel.TEXT_MAX_LENGTH + 1);

            // when & then
            assertThatThrownBy(() -> ReviewModel.create(contentId, authorId, longText,
                BigDecimal.ONE))
                .isInstanceOf(InvalidReviewDataException.class)
                .satisfies(e -> {
                    InvalidReviewDataException ex = (InvalidReviewDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("리뷰 내용은 " + ReviewModel.TEXT_MAX_LENGTH + "자를 초과할 수 없습니다.");
                });
        }

        @Test
        @DisplayName("평점이 0 미만이면 예외 발생")
        void withRatingLessThanZero_throwsException() {
            // given
            UUID authorId = UUID.randomUUID();

            // when & then
            assertThatThrownBy(() -> ReviewModel.create(UUID.randomUUID(), authorId, "text",
                BigDecimal.valueOf(-1)))
                .isInstanceOf(InvalidReviewDataException.class)
                .satisfies(e -> {
                    InvalidReviewDataException ex = (InvalidReviewDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("평점은 0.0 이상 5.0 이하만 가능합니다.");
                });
        }

        @Test
        @DisplayName("평점이 5 초과면 예외 발생")
        void withRatingGreaterThanFive_throwsException() {
            // given
            UUID authorId = UUID.randomUUID();

            // when & then
            assertThatThrownBy(() -> ReviewModel.create(UUID.randomUUID(), authorId, "text",
                BigDecimal.valueOf(6)))
                .isInstanceOf(InvalidReviewDataException.class)
                .satisfies(e -> {
                    InvalidReviewDataException ex = (InvalidReviewDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("평점은 0.0 이상 5.0 이하만 가능합니다.");
                });
        }

        @Test
        @DisplayName("평점이 소수(예: 4.5)이면 예외 발생")
        void withDecimalRating_throwsException() {
            // given
            UUID authorId = UUID.randomUUID();

            // when & then
            assertThatThrownBy(() -> ReviewModel.create(UUID.randomUUID(), authorId, "text",
                BigDecimal.valueOf(4.5)))
                .isInstanceOf(InvalidReviewDataException.class)
                .satisfies(e -> {
                    InvalidReviewDataException ex = (InvalidReviewDataException) e;
                    // [수정] 모델의 실제 메시지와 일치시킴 (괄호 포함)
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("평점은 정수만 가능합니다. (0.5 단위는 허용되지 않습니다.)");
                });
        }
    }
}
