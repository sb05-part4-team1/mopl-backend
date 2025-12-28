package com.mopl.domain.model.review;

import com.mopl.domain.exception.review.InvalidReviewDataException;
import com.mopl.domain.model.user.UserModel;
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

            UserModel author = UserModel.builder()
                .id(authorId)
                .build();

            String text = "리뷰 내용";
            BigDecimal rating = BigDecimal.valueOf(4);

            // when
            ReviewModel review = ReviewModel.create(contentId, author, text, rating);

            // then
            assertThat(review.getContentId()).isEqualTo(contentId);
            assertThat(review.getAuthor()).isNotNull();
            assertThat(review.getAuthor().getId()).isEqualTo(authorId);
            assertThat(review.getText()).isEqualTo(text);
            assertThat(review.getRating()).isEqualTo(rating);
        }

        @Test
        @DisplayName("contentId가 null이면 예외 발생")
        void withNullContentId_throwsException() {
            // given
            UserModel author = UserModel.builder().id(UUID.randomUUID()).build();

            // when & then
            assertThatThrownBy(() -> ReviewModel.create(null, author, "text", BigDecimal.ONE))
                .isInstanceOf(InvalidReviewDataException.class)
                .satisfies(e -> {
                    InvalidReviewDataException ex = (InvalidReviewDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("콘텐츠 ID는 null일 수 없습니다.");
                });
        }

        @Test
        @DisplayName("author가 null이면 예외 발생")
        void withNullAuthor_throwsException() {
            // when & then
            assertThatThrownBy(() -> ReviewModel.create(UUID.randomUUID(), null, "text",
                BigDecimal.ONE))
                .isInstanceOf(InvalidReviewDataException.class)
                .satisfies(e -> {
                    InvalidReviewDataException ex = (InvalidReviewDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("작성자는 null일 수 없습니다.");
                });
        }

        @Test
        @DisplayName("rating이 null이면 예외 발생")
        void withNullRating_throwsException() {
            // given
            UserModel author = UserModel.builder().id(UUID.randomUUID()).build();

            // when & then
            assertThatThrownBy(() -> ReviewModel.create(UUID.randomUUID(), author, "text", null))
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
            UserModel author = UserModel.builder().id(UUID.randomUUID()).build();
            String longText = "a".repeat(ReviewModel.TEXT_MAX_LENGTH + 1);

            // when & then
            assertThatThrownBy(() -> ReviewModel.create(contentId, author, longText,
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
            UserModel author = UserModel.builder().id(UUID.randomUUID()).build();

            // when & then
            assertThatThrownBy(() -> ReviewModel.create(UUID.randomUUID(), author, "text",
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
            UserModel author = UserModel.builder().id(UUID.randomUUID()).build();

            // when & then
            assertThatThrownBy(() -> ReviewModel.create(UUID.randomUUID(), author, "text",
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
            UserModel author = UserModel.builder().id(UUID.randomUUID()).build();

            // when & then
            assertThatThrownBy(() -> ReviewModel.create(UUID.randomUUID(), author, "text",
                BigDecimal.valueOf(4.5)))
                .isInstanceOf(InvalidReviewDataException.class)
                .satisfies(e -> {
                    InvalidReviewDataException ex = (InvalidReviewDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("평점은 정수만 가능합니다. (0.5 단위는 허용되지 않습니다.)");
                });
        }
    }

    //    @Nested
    //    @DisplayName("update()")
    //    class UpdateTest {
    //
    //        @Test
    //        @DisplayName("newText/newRating이 null이 아니면 업데이트된다")
    //        void update_appliesWhenNonNull() {
    //            // given
    //            UUID contentId = UUID.randomUUID();
    //            UserModel author = UserModel.builder().id(UUID.randomUUID()).build();
    //
    //            ReviewModel review = ReviewModel.create(
    //                    contentId,
    //                    author,
    //                    "old",
    //                    BigDecimal.valueOf(3)
    //            );
    //
    //            // when
    //            review.update("new", BigDecimal.valueOf(5));
    //
    //            // then
    //            assertThat(review.getText()).isEqualTo("new");
    //            assertThat(review.getRating()).isEqualTo(BigDecimal.valueOf(5));
    //        }
    //
    //    }
}
