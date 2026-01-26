package com.mopl.domain.service.review;

import com.mopl.domain.exception.review.ReviewAlreadyExistsException;
import com.mopl.domain.exception.review.ReviewForbiddenException;
import com.mopl.domain.exception.review.ReviewNotFoundException;
import com.mopl.domain.fixture.ReviewModelFixture;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.domain.repository.review.ReviewQueryRepository;
import com.mopl.domain.repository.review.ReviewQueryRequest;
import com.mopl.domain.repository.review.ReviewRepository;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService 단위 테스트")
class ReviewServiceTest {

    @Mock
    private ReviewQueryRepository reviewQueryRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ContentRepository contentRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Nested
    @DisplayName("getAll()")
    class GetAllTest {

        @Test
        @DisplayName("Repository에 위임하여 결과 반환")
        void delegatesToRepository() {
            // given
            ReviewQueryRequest request = new ReviewQueryRequest(
                null, null, null, null, null, null
            );
            CursorResponse<ReviewModel> expectedResponse = CursorResponse.empty(
                "CREATED_AT", SortDirection.DESCENDING
            );

            given(reviewQueryRepository.findAll(request)).willReturn(expectedResponse);

            // when
            CursorResponse<ReviewModel> result = reviewService.getAll(request);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            then(reviewQueryRepository).should().findAll(request);
        }

        @Test
        @DisplayName("리뷰 목록이 있으면 결과 반환")
        void withReviews_returnsReviewList() {
            // given
            ReviewModel review1 = ReviewModelFixture.create();
            ReviewModel review2 = ReviewModelFixture.create();
            ReviewQueryRequest request = new ReviewQueryRequest(
                null, null, null, 20, null, null
            );
            CursorResponse<ReviewModel> expectedResponse = CursorResponse.of(
                List.of(review1, review2),
                null,
                null,
                false,
                2L,
                "CREATED_AT",
                SortDirection.DESCENDING
            );

            given(reviewQueryRepository.findAll(request)).willReturn(expectedResponse);

            // when
            CursorResponse<ReviewModel> result = reviewService.getAll(request);

            // then
            assertThat(result.data()).hasSize(2);
            assertThat(result.totalCount()).isEqualTo(2L);
            then(reviewQueryRepository).should().findAll(request);
        }
    }

    @Nested
    @DisplayName("create()")
    class CreateTest {

        @Test
        @DisplayName("유효한 정보가 주어지면 리뷰를 생성하고 저장한다")
        void withValidData_createsAndSavesReview() {
            // given
            UUID contentId = UUID.randomUUID();
            UUID authorId = UUID.randomUUID();

            ContentModel content = mock(ContentModel.class);
            given(content.getId()).willReturn(contentId);

            UserModel author = mock(UserModel.class);
            given(author.getId()).willReturn(authorId);

            String text = "리뷰 내용입니다.";
            double rating = 5.0;

            given(reviewRepository.existsByContentIdAndAuthorId(contentId, authorId))
                .willReturn(false);
            given(reviewRepository.save(any(ReviewModel.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

            // when
            ReviewModel result = reviewService.create(content, author, text, rating);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEqualTo(content);
            assertThat(result.getText()).isEqualTo(text);
            assertThat(result.getRating()).isEqualTo(rating);

            then(reviewRepository).should().existsByContentIdAndAuthorId(contentId, authorId);
            then(reviewRepository).should().save(any(ReviewModel.class));
        }

        @Test
        @DisplayName("이미 동일한 콘텐츠에 리뷰가 있으면 ReviewAlreadyExistsException 발생")
        void withExistingReview_throwsReviewAlreadyExistsException() {
            // given
            UUID contentId = UUID.randomUUID();
            UUID authorId = UUID.randomUUID();

            ContentModel content = mock(ContentModel.class);
            given(content.getId()).willReturn(contentId);

            UserModel author = mock(UserModel.class);
            given(author.getId()).willReturn(authorId);

            given(reviewRepository.existsByContentIdAndAuthorId(contentId, authorId))
                .willReturn(true);

            // when & then
            assertThatThrownBy(() -> reviewService.create(content, author, "리뷰 내용", 5.0))
                .isInstanceOf(ReviewAlreadyExistsException.class);

            then(reviewRepository).should().existsByContentIdAndAuthorId(contentId, authorId);
            then(reviewRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTest {

        @Test
        @DisplayName("작성자가 본인의 리뷰를 수정하면 정상적으로 업데이트되고 저장된다")
        void withOwner_updatesAndSavesReview() {
            // given
            ReviewModel existingReview = ReviewModelFixture.create();
            UUID reviewId = existingReview.getId();
            UUID authorId = existingReview.getAuthor().getId();

            String newText = "수정된 내용";
            double originalRating = existingReview.getRating();
            Double newRating = originalRating == 5.0 ? 4.0 : 5.0;

            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(existingReview));
            given(reviewRepository.save(any(ReviewModel.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

            // when
            ReviewModel updatedReview = reviewService.update(reviewId, authorId, newText,
                newRating);

            // then
            assertThat(updatedReview.getText()).isEqualTo(newText);
            assertThat(updatedReview.getRating()).isEqualTo(newRating);

            then(reviewRepository).should().save(any(ReviewModel.class));
            then(contentRepository).should().save(any(ContentModel.class));
        }

        @Test
        @DisplayName("텍스트만 수정하면 텍스트만 업데이트되고 콘텐츠 평점은 업데이트되지 않는다")
        void withOnlyText_updatesTextOnly() {
            // given
            ReviewModel existingReview = ReviewModelFixture.create();
            UUID reviewId = existingReview.getId();
            UUID authorId = existingReview.getAuthor().getId();

            double originalRating = existingReview.getRating();
            String newText = "수정된 내용";

            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(existingReview));
            given(reviewRepository.save(any(ReviewModel.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

            // when
            ReviewModel updatedReview = reviewService.update(reviewId, authorId, newText, null);

            // then
            assertThat(updatedReview.getText()).isEqualTo(newText);
            assertThat(updatedReview.getRating()).isEqualTo(originalRating);

            then(reviewRepository).should().save(any(ReviewModel.class));
            then(contentRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("평점만 수정하면 평점만 업데이트되고 콘텐츠 평점도 업데이트된다")
        void withOnlyRating_updatesRatingOnly() {
            // given
            ReviewModel existingReview = ReviewModelFixture.create();
            UUID reviewId = existingReview.getId();
            UUID authorId = existingReview.getAuthor().getId();

            String originalText = existingReview.getText();
            double originalRating = existingReview.getRating();
            Double newRating = originalRating == 5.0 ? 4.0 : 5.0;

            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(existingReview));
            given(reviewRepository.save(any(ReviewModel.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

            // when
            ReviewModel updatedReview = reviewService.update(reviewId, authorId, null, newRating);

            // then
            assertThat(updatedReview.getText()).isEqualTo(originalText);
            assertThat(updatedReview.getRating()).isEqualTo(newRating);

            then(reviewRepository).should().save(any(ReviewModel.class));
            then(contentRepository).should().save(any(ContentModel.class));
        }

        @Test
        @DisplayName("같은 평점으로 수정하면 콘텐츠 평점은 업데이트되지 않는다")
        void withSameRating_doesNotUpdateContentRating() {
            // given
            ReviewModel existingReview = ReviewModelFixture.create();
            UUID reviewId = existingReview.getId();
            UUID authorId = existingReview.getAuthor().getId();

            double originalRating = existingReview.getRating();

            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(existingReview));
            given(reviewRepository.save(any(ReviewModel.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

            // when
            reviewService.update(reviewId, authorId, null, originalRating);

            // then
            then(reviewRepository).should().save(any(ReviewModel.class));
            then(contentRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 리뷰 ID면 ReviewNotFoundException 발생")
        void withNonExistingId_throwsNotFoundException() {
            // given
            UUID reviewId = UUID.randomUUID();
            UUID requesterId = UUID.randomUUID();

            given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewService.update(reviewId, requesterId, "text", 1.0))
                .isInstanceOf(ReviewNotFoundException.class);

            then(reviewRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("작성자가 아닌 사람이 수정하려 하면 ReviewForbiddenException 발생")
        void withDifferentUser_throwsForbiddenException() {
            // given
            ReviewModel existingReview = ReviewModelFixture.create();
            UUID reviewId = existingReview.getId();

            // Fixture가 만든 작성자 ID와 '확실히 다른' ID 생성
            UUID requesterId = UUID.randomUUID();

            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(existingReview));

            // when & then
            assertThatThrownBy(() -> reviewService.update(reviewId, requesterId, "text", 1.0))
                .isInstanceOf(ReviewForbiddenException.class);

            then(reviewRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTest {

        @Test
        @DisplayName("작성자가 본인의 리뷰를 삭제 요청하면 정상적으로 삭제된다")
        void withOwner_deletesReview() {
            // given
            ReviewModel existingReview = ReviewModelFixture.create();
            UUID reviewId = existingReview.getId();
            UUID authorId = existingReview.getAuthor().getId();

            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(existingReview));

            // when
            reviewService.deleteAndGetContentId(reviewId, authorId);

            // then
            then(reviewRepository).should().delete(reviewId);
            then(contentRepository).should().save(any(ContentModel.class));
        }

        @Test
        @DisplayName("작성자가 아닌 사람이 삭제하려 하면 ReviewForbiddenException 발생")
        void withDifferentUser_throwsForbiddenException() {
            // given
            ReviewModel existingReview = ReviewModelFixture.create();
            UUID requesterId = UUID.randomUUID(); // 다른 사용자

            given(reviewRepository.findById(existingReview.getId())).willReturn(Optional.of(
                existingReview));

            // when & then
            assertThatThrownBy(() -> reviewService.deleteAndGetContentId(existingReview.getId(), requesterId))
                .isInstanceOf(ReviewForbiddenException.class);

            then(reviewRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 리뷰 삭제 시 ReviewNotFoundException 발생")
        void withNonExistingId_throwsNotFoundException() {
            // given
            UUID reviewId = UUID.randomUUID();
            UUID requesterId = UUID.randomUUID();

            given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewService.deleteAndGetContentId(reviewId, requesterId))
                .isInstanceOf(ReviewNotFoundException.class);

            then(reviewRepository).should(never()).delete(any());
        }

        @Test
        @DisplayName("작성자가 soft delete된 경우 ReviewForbiddenException 발생")
        void withNullAuthor_throwsForbiddenException() {
            // given
            UUID reviewId = UUID.randomUUID();
            UUID requesterId = UUID.randomUUID();

            ReviewModel reviewWithNullAuthor = ReviewModelFixture.builder()
                .set("id", reviewId)
                .setNull("author")
                .sample();

            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(reviewWithNullAuthor));

            // when & then
            assertThatThrownBy(() -> reviewService.deleteAndGetContentId(reviewId, requesterId))
                .isInstanceOf(ReviewForbiddenException.class);

            then(reviewRepository).should(never()).delete(any());
        }
    }
}
