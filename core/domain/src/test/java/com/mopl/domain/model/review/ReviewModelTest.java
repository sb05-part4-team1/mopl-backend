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
            BigDecimal rating = new BigDecimal("4"); // 정수형 평점

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
                new BigDecimal("4.5")))
                .isInstanceOf(InvalidReviewDataException.class)
                .satisfies(e -> {
                    InvalidReviewDataException ex = (InvalidReviewDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("평점은 정수만 가능합니다. (0.5 단위는 허용되지 않습니다.)");
                });
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTest {

        @Test
        @DisplayName("유효한 내용과 평점으로 업데이트하면 값이 변경된다")
        void withValidData_updatesReview() {
            // given
            ReviewModel review = ReviewModel.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "기존 내용",
                new BigDecimal("3")
            );

            String newText = "수정된 내용";
            BigDecimal newRating = new BigDecimal("5");

            // when
            ReviewModel updatedReview = review.update(newText, newRating);

            // then
            assertThat(updatedReview.getText()).isEqualTo(newText);
            assertThat(updatedReview.getRating()).isEqualTo(newRating);
        }

        @Test
        @DisplayName("수정할 내용이 null이면 예외 발생")
        void withNullText_throwsException() {
            // given
            ReviewModel review = ReviewModel.create(UUID.randomUUID(), UUID.randomUUID(), "text",
                BigDecimal.ONE);

            // when & then
            assertThatThrownBy(() -> review.update(null, BigDecimal.valueOf(5)))
                .isInstanceOf(InvalidReviewDataException.class)
                .satisfies(e -> {
                    InvalidReviewDataException ex = (InvalidReviewDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("리뷰 내용은 null일 수 없습니다.");
                });
        }

        @Test
        @DisplayName("수정할 평점이 null이면 예외 발생")
        void withNullRating_throwsException() {
            // given
            ReviewModel review = ReviewModel.create(UUID.randomUUID(), UUID.randomUUID(), "text",
                BigDecimal.ONE);

            // when & then
            assertThatThrownBy(() -> review.update("new text", null))
                .isInstanceOf(InvalidReviewDataException.class)
                .satisfies(e -> {
                    InvalidReviewDataException ex = (InvalidReviewDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("평점은 null일 수 없습니다.");
                });
        }

        @Test
        @DisplayName("수정할 내용이 최대 길이를 초과하면 예외 발생")
        void withTooLongText_throwsException() {
            // given
            ReviewModel review = ReviewModel.create(UUID.randomUUID(), UUID.randomUUID(), "text",
                BigDecimal.ONE);
            String longText = "a".repeat(ReviewModel.TEXT_MAX_LENGTH + 1);

            // when & then
            assertThatThrownBy(() -> review.update(longText, BigDecimal.valueOf(5)))
                .isInstanceOf(InvalidReviewDataException.class)
                .satisfies(e -> {
                    InvalidReviewDataException ex = (InvalidReviewDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("리뷰 내용은 " + ReviewModel.TEXT_MAX_LENGTH + "자를 초과할 수 없습니다.");
                });
        }

        @Test
        @DisplayName("수정할 평점이 정수가 아니면 예외 발생")
        void withDecimalRating_throwsException() {
            // given
            ReviewModel review = ReviewModel.create(UUID.randomUUID(), UUID.randomUUID(), "text",
                BigDecimal.ONE);

            // when & then
            assertThatThrownBy(() -> review.update("new text", new BigDecimal("4.5")))
                .isInstanceOf(InvalidReviewDataException.class)
                .satisfies(e -> {
                    InvalidReviewDataException ex = (InvalidReviewDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("평점은 정수만 가능합니다. (0.5 단위는 허용되지 않습니다.)");
                });
        }

        @Test
        @DisplayName("수정할 평점이 범위를 벗어나면 예외 발생")
        void withInvalidRatingRange_throwsException() {
            // given
            ReviewModel review = ReviewModel.create(UUID.randomUUID(), UUID.randomUUID(), "text",
                BigDecimal.ONE);

            // when & then
            assertThatThrownBy(() -> review.update("new text", new BigDecimal("6")))
                .isInstanceOf(InvalidReviewDataException.class)
                .satisfies(e -> {
                    InvalidReviewDataException ex = (InvalidReviewDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("평점은 0.0 이상 5.0 이하만 가능합니다.");
                });
        }
    }

    @Nested
    @DisplayName("deleteReview()")
    class DeleteTest {

        @Test
        @DisplayName("삭제되지 않은 리뷰를 요청하면 정상적으로 삭제 처리된다")
        void withNotDeletedReview_deletesReview() {
            // given
            ReviewModel review = ReviewModel.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "삭제할 리뷰",
                new BigDecimal("3")
            );

            // when
            ReviewModel deletedReview = review.deleteReview();

            // then
            // BaseUpdatableModel의 delete()가 deletedAt을 찍는다고 가정
            assertThat(deletedReview).isNotNull();
            assertThat(deletedReview.getDeletedAt()).isNotNull();
        }

    }
}
