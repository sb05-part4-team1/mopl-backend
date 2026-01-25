package com.mopl.domain.model.review;

import com.mopl.domain.exception.review.InvalidReviewDataException;
import com.mopl.domain.fixture.ReviewModelFixture;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.user.UserModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
            double rating = 4.0;

            // when
            ReviewModel review = ReviewModel.create(content, author, text, rating);

            // then
            assertThat(review.getContent()).isEqualTo(content);
            assertThat(review.getAuthor()).isEqualTo(author);
            assertThat(review.getText()).isEqualTo(text);
            assertThat(review.getRating()).isEqualTo(rating);
        }

        @Test
        @DisplayName("ContentModel이 null이면 예외 발생")
        @SuppressWarnings("ConstantConditions")
        void withNullContent_throwsException() {
            // given
            UserModel author = mock(UserModel.class);

            // when & then
            assertThatThrownBy(() -> ReviewModel.create(null, author, "text", 1.0))
                .isInstanceOf(InvalidReviewDataException.class)
                .satisfies(e -> {
                    InvalidReviewDataException ex = (InvalidReviewDataException) e;
                    assertThat((String) ex.getDetails().get("detailMessage"))
                        .isEqualTo("콘텐츠 정보는 null일 수 없습니다.");
                });
        }

        @Test
        @DisplayName("ContentModel의 ID가 null이면 예외 발생")
        void withNullContentId_throwsException() {
            // given
            ContentModel content = mock(ContentModel.class);
            given(content.getId()).willReturn(null);
            UserModel author = mock(UserModel.class);

            // when & then
            assertThatThrownBy(() -> ReviewModel.create(content, author, "text", 1.0))
                .isInstanceOf(InvalidReviewDataException.class)
                .satisfies(e -> {
                    InvalidReviewDataException ex = (InvalidReviewDataException) e;
                    assertThat((String) ex.getDetails().get("detailMessage"))
                        .isEqualTo("콘텐츠 정보는 null일 수 없습니다.");
                });
        }

        @Test
        @DisplayName("UserModel이 null이면 예외 발생")
        void withNullAuthor_throwsException() {
            // given
            ContentModel content = mock(ContentModel.class);
            given(content.getId()).willReturn(UUID.randomUUID());

            // when & then
            assertThatThrownBy(() -> ReviewModel.create(content, null, "text", 1.0))
                .isInstanceOf(InvalidReviewDataException.class)
                .satisfies(e -> {
                    InvalidReviewDataException ex = (InvalidReviewDataException) e;
                    assertThat((String) ex.getDetails().get("detailMessage"))
                        .isEqualTo("작성자 정보는 null일 수 없습니다.");
                });
        }

        @Test
        @DisplayName("UserModel의 ID가 null이면 예외 발생")
        void withNullAuthorId_throwsException() {
            // given
            ContentModel content = mock(ContentModel.class);
            given(content.getId()).willReturn(UUID.randomUUID());
            UserModel author = mock(UserModel.class);
            given(author.getId()).willReturn(null);

            // when & then
            assertThatThrownBy(() -> ReviewModel.create(content, author, "text", 1.0))
                .isInstanceOf(InvalidReviewDataException.class)
                .satisfies(e -> {
                    InvalidReviewDataException ex = (InvalidReviewDataException) e;
                    assertThat((String) ex.getDetails().get("detailMessage"))
                        .isEqualTo("작성자 정보는 null일 수 없습니다.");
                });
        }

        @Test
        @DisplayName("텍스트 길이가 최대 길이를 초과하면 예외 발생")
        void withTextExceedingMaxLength_throwsException() {
            // given
            ContentModel content = mock(ContentModel.class);
            given(content.getId()).willReturn(UUID.randomUUID());
            UserModel author = mock(UserModel.class);
            given(author.getId()).willReturn(UUID.randomUUID());

            String longText = "a".repeat(ReviewModel.TEXT_MAX_LENGTH + 1);

            // when & then
            assertThatThrownBy(() -> ReviewModel.create(content, author, longText, 1.0))
                .isInstanceOf(InvalidReviewDataException.class)
                .satisfies(e -> {
                    InvalidReviewDataException ex = (InvalidReviewDataException) e;
                    assertThat((String) ex.getDetails().get("detailMessage"))
                        .contains("리뷰 내용은");
                });
        }

        @Test
        @DisplayName("평점이 0.0 미만이면 예외 발생")
        void withNegativeRating_throwsException() {
            // given
            ContentModel content = mock(ContentModel.class);
            given(content.getId()).willReturn(UUID.randomUUID());
            UserModel author = mock(UserModel.class);
            given(author.getId()).willReturn(UUID.randomUUID());

            // when & then
            assertThatThrownBy(() -> ReviewModel.create(content, author, "text", -0.1))
                .isInstanceOf(InvalidReviewDataException.class)
                .satisfies(e -> {
                    InvalidReviewDataException ex = (InvalidReviewDataException) e;
                    assertThat((String) ex.getDetails().get("detailMessage"))
                        .contains("평점은 0.0 이상 5.0 이하만 가능합니다");
                });
        }

        @Test
        @DisplayName("평점이 5.0 초과면 예외 발생")
        void withRatingExceedingMax_throwsException() {
            // given
            ContentModel content = mock(ContentModel.class);
            given(content.getId()).willReturn(UUID.randomUUID());
            UserModel author = mock(UserModel.class);
            given(author.getId()).willReturn(UUID.randomUUID());

            // when & then
            assertThatThrownBy(() -> ReviewModel.create(content, author, "text", 5.1))
                .isInstanceOf(InvalidReviewDataException.class)
                .satisfies(e -> {
                    InvalidReviewDataException ex = (InvalidReviewDataException) e;
                    assertThat((String) ex.getDetails().get("detailMessage"))
                        .contains("평점은 0.0 이상 5.0 이하만 가능합니다");
                });
        }

        @Test
        @DisplayName("평점이 0.0~5.0 범위 내면 소수도 허용된다")
        void withDecimalRating_isAllowed() {
            // given
            ContentModel content = mock(ContentModel.class);
            given(content.getId()).willReturn(UUID.randomUUID());

            UserModel author = mock(UserModel.class);
            given(author.getId()).willReturn(UUID.randomUUID());

            String text = "text";
            double rating = 4.5;

            // when
            ReviewModel review = ReviewModel.create(content, author, text, rating);

            // then
            assertThat(review.getRating()).isEqualTo(4.5);
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
            Double newRating = 4.5;

            // when
            ReviewModel updatedReview = review.update(newText, newRating);

            // then
            assertThat(updatedReview.getText()).isEqualTo(newText);
            assertThat(updatedReview.getRating()).isEqualTo(newRating);
        }

        @Test
        @DisplayName("텍스트만 수정하면 텍스트만 변경되고 평점은 유지된다")
        void withOnlyText_updatesTextOnly() {
            // given
            ReviewModel review = ReviewModelFixture.create();
            double originalRating = review.getRating();

            String newText = "수정된 내용";

            // when
            ReviewModel updatedReview = review.update(newText, null);

            // then
            assertThat(updatedReview.getText()).isEqualTo(newText);
            assertThat(updatedReview.getRating()).isEqualTo(originalRating);
        }

        @Test
        @DisplayName("평점만 수정하면 평점만 변경되고 텍스트는 유지된다")
        void withOnlyRating_updatesRatingOnly() {
            // given
            ReviewModel review = ReviewModelFixture.create();
            String originalText = review.getText();

            Double newRating = 4.5;

            // when
            ReviewModel updatedReview = review.update(null, newRating);

            // then
            assertThat(updatedReview.getText()).isEqualTo(originalText);
            assertThat(updatedReview.getRating()).isEqualTo(newRating);
        }

        @Test
        @DisplayName("둘 다 null이면 아무것도 변경되지 않는다")
        void withBothNull_noChanges() {
            // given
            ReviewModel review = ReviewModelFixture.create();
            String originalText = review.getText();
            double originalRating = review.getRating();

            // when
            ReviewModel updatedReview = review.update(null, null);

            // then
            assertThat(updatedReview.getText()).isEqualTo(originalText);
            assertThat(updatedReview.getRating()).isEqualTo(originalRating);
        }

        @Test
        @DisplayName("수정할 평점 범위를 벗어나면 예외 발생")
        void withInvalidRating_throwsException() {
            // given
            ReviewModel review = ReviewModelFixture.create();

            // when & then
            assertThatThrownBy(() -> review.update("Valid text", 6.0))
                .isInstanceOf(InvalidReviewDataException.class)
                .satisfies(e -> {
                    InvalidReviewDataException ex = (InvalidReviewDataException) e;
                    assertThat((String) ex.getDetails().get("detailMessage"))
                        .contains("평점은 0.0 이상 5.0 이하만 가능합니다");
                });
        }

        @Test
        @DisplayName("수정할 텍스트가 최대 길이를 초과하면 예외 발생")
        void withTextExceedingMaxLength_throwsException() {
            // given
            ReviewModel review = ReviewModelFixture.create();
            String longText = "a".repeat(ReviewModel.TEXT_MAX_LENGTH + 1);

            // when & then
            assertThatThrownBy(() -> review.update(longText, null))
                .isInstanceOf(InvalidReviewDataException.class)
                .satisfies(e -> {
                    InvalidReviewDataException ex = (InvalidReviewDataException) e;
                    assertThat((String) ex.getDetails().get("detailMessage"))
                        .contains("리뷰 내용은");
                });
        }

        @Test
        @DisplayName("수정할 평점이 0.0 미만이면 예외 발생")
        void withNegativeRating_throwsException() {
            // given
            ReviewModel review = ReviewModelFixture.create();

            // when & then
            assertThatThrownBy(() -> review.update(null, -0.1))
                .isInstanceOf(InvalidReviewDataException.class)
                .satisfies(e -> {
                    InvalidReviewDataException ex = (InvalidReviewDataException) e;
                    assertThat((String) ex.getDetails().get("detailMessage"))
                        .contains("평점은 0.0 이상 5.0 이하만 가능합니다");
                });
        }

        @Test
        @DisplayName("update()는 원본 객체를 변경하지 않고 새로운 객체를 반환한다 (immutable)")
        void update_returnsNewObjectWithoutModifyingOriginal() {
            // given
            ReviewModel original = ReviewModelFixture.create();
            String originalText = original.getText();
            double originalRating = original.getRating();

            String newText = "새로운 텍스트";
            Double newRating = 1.0;

            // when
            ReviewModel updated = original.update(newText, newRating);

            // then
            // 원본은 변경되지 않음
            assertThat(original.getText()).isEqualTo(originalText);
            assertThat(original.getRating()).isEqualTo(originalRating);

            // 새 객체에 변경 사항 적용됨
            assertThat(updated.getText()).isEqualTo(newText);
            assertThat(updated.getRating()).isEqualTo(newRating);

            // 서로 다른 인스턴스
            assertThat(updated).isNotSameAs(original);

            // ID는 동일
            assertThat(updated.getId()).isEqualTo(original.getId());
        }

        @Test
        @DisplayName("update()는 content와 author를 유지한다")
        void update_preservesContentAndAuthor() {
            // given
            ReviewModel original = ReviewModelFixture.create();

            // when
            ReviewModel updated = original.update("새로운 텍스트", 3.0);

            // then
            assertThat(updated.getContent()).isEqualTo(original.getContent());
            assertThat(updated.getAuthor()).isEqualTo(original.getAuthor());
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
            review.delete();

            // then
            assertThat(review.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("이미 삭제된 리뷰를 다시 삭제해도 에러 없이 멱등성이 보장된다")
        void deleteAlreadyDeletedReview_isIdempotent() {
            // given
            ReviewModel review = ReviewModelFixture.create();
            review.delete(); // 1차 삭제

            // when
            review.delete(); // 2차 삭제

            // then
            assertThat(review).isNotNull();
            assertThat(review.getDeletedAt()).isNotNull();
        }
    }
}
