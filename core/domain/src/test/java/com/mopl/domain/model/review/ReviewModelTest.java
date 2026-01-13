package com.mopl.domain.model.review;

import com.mopl.domain.exception.review.InvalidReviewDataException;
import com.mopl.domain.fixture.ReviewModelFixture;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.user.UserModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@DisplayName("ReviewModel 단위 테스트")
class ReviewModelTest {

    @Nested
    @DisplayName("create()")
    class CreateTest {

        @Test
        @DisplayName("유효한 객체와 값이 주어지면 리뷰 모델을 생성한다")
        void withValidData_createsReview() {
            // given
            UUID contentId = UUID.randomUUID();
            UUID authorId = UUID.randomUUID();

            // 협력 객체 Mocking
            ContentModel content = mock(ContentModel.class);
            given(content.getId()).willReturn(contentId);

            UserModel author = mock(UserModel.class);
            given(author.getId()).willReturn(authorId);

            String text = "리뷰 내용";
            BigDecimal rating = new BigDecimal("4");

            // when
            ReviewModel review = ReviewModel.create(content, author, text, rating);

            // then
            assertThat(review.getContent()).isEqualTo(content);
            assertThat(review.getAuthor()).isEqualTo(author);
            assertThat(review.getText()).isEqualTo(text);
            assertThat(review.getRating()).isEqualTo(rating);
        }

        @Test
        @DisplayName("ContentModel이 null이거나 ID가 없으면 예외 발생")
        void withInvalidContent_throwsException() {
            // given
            UserModel author = mock(UserModel.class);

            // when & then
            assertThatThrownBy(() -> ReviewModel.create(null, author, "text", BigDecimal.ONE))
                .isInstanceOf(InvalidReviewDataException.class)
                .satisfies(e -> {
                    InvalidReviewDataException ex = (InvalidReviewDataException) e;
                    // Object -> String 형변환
                    assertThat((String) ex.getDetails().get("detailMessage"))
                        .isEqualTo("콘텐츠 정보는 null일 수 없습니다.");
                });
        }

        @Test
        @DisplayName("평점이 정수가 아니면(예: 4.5) 예외 발생")
        void withDecimalRating_throwsException() {
            // given
            ContentModel content = mock(ContentModel.class);
            given(content.getId()).willReturn(UUID.randomUUID());
            UserModel author = mock(UserModel.class);
            given(author.getId()).willReturn(UUID.randomUUID());

            // when & then
            assertThatThrownBy(() -> ReviewModel.create(content, author, "text", new BigDecimal(
                "4.5")))
                .isInstanceOf(InvalidReviewDataException.class)
                .satisfies(e -> {
                    InvalidReviewDataException ex = (InvalidReviewDataException) e;
                    // Object -> String 형변환 후 contains 사용
                    assertThat((String) ex.getDetails().get("detailMessage"))
                        .contains("평점은 정수만 가능합니다");
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
            ReviewModel review = ReviewModelFixture.create();

            String newText = "수정된 내용";
            BigDecimal newRating = new BigDecimal("5"); // 정수형 평점

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
            ReviewModel review = ReviewModelFixture.create();

            // when & then
            assertThatThrownBy(() -> review.update(null, BigDecimal.valueOf(5)))
                .isInstanceOf(InvalidReviewDataException.class)
                .satisfies(e -> {
                    InvalidReviewDataException ex = (InvalidReviewDataException) e;
                    // Object -> String 형변환
                    assertThat((String) ex.getDetails().get("detailMessage"))
                        .isEqualTo("리뷰 내용은 null일 수 없습니다.");
                });
        }

        @Test
        @DisplayName("수정할 평점 범위를 벗어나면 예외 발생")
        void withInvalidRating_throwsException() {
            // given
            ReviewModel review = ReviewModelFixture.create();

            // when & then
            assertThatThrownBy(() -> review.update("Valid text", new BigDecimal("6")))
                .isInstanceOf(InvalidReviewDataException.class)
                .satisfies(e -> {
                    InvalidReviewDataException ex = (InvalidReviewDataException) e;
                    // Object -> String 형변환 후 contains 사용
                    assertThat((String) ex.getDetails().get("detailMessage"))
                        .contains("평점은 0.0 이상 5.0 이하만 가능합니다");
                });
        }
    }

    @Nested
    @DisplayName("deleteReview()")
    class DeleteTest {

        @Test
        @DisplayName("삭제 요청 시 deletedAt 필드가 설정된다")
        void deletesReview() {
            // given
            ReviewModel review = ReviewModelFixture.create();

            // when
            ReviewModel deletedReview = review.deleteReview();

            // then
            assertThat(deletedReview.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("이미 삭제된 리뷰를 다시 삭제해도 에러 없이 멱등성이 보장된다")
        void deleteAlreadyDeletedReview_isIdempotent() {
            // given
            ReviewModel review = ReviewModelFixture.create();
            review.deleteReview(); // 1차 삭제

            // when
            ReviewModel doubleDeletedReview = review.deleteReview(); // 2차 삭제

            // then
            assertThat(doubleDeletedReview).isNotNull();
            assertThat(doubleDeletedReview.getDeletedAt()).isNotNull();
        }
    }
}
