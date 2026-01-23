package com.mopl.api.application.review;

import com.mopl.api.interfaces.api.review.dto.ReviewCreateRequest;
import com.mopl.api.interfaces.api.review.dto.ReviewResponse;
import com.mopl.api.interfaces.api.review.mapper.ReviewResponseMapper;
import com.mopl.api.interfaces.api.review.dto.ReviewUpdateRequest;
import com.mopl.domain.exception.content.ContentNotFoundException;
import com.mopl.domain.fixture.ReviewModelFixture;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.review.ReviewQueryRequest;
import com.mopl.domain.repository.review.ReviewSortField;
import com.mopl.domain.service.content.ContentService;
import com.mopl.domain.service.review.ReviewService;
import com.mopl.domain.service.user.UserService;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewFacade 단위 테스트")
class ReviewFacadeTest {

    @InjectMocks
    private ReviewFacade reviewFacade;

    @Mock
    private ReviewService reviewService;

    @Mock
    private UserService userService;

    @Mock
    private ContentService contentService;

    @Mock
    private ReviewResponseMapper reviewResponseMapper;

    @Nested
    @DisplayName("createReview()")
    class CreateReviewTest {

        @Test
        @DisplayName("유효한 데이터가 주어지면 리뷰를 생성하고 응답을 반환한다")
        void withValidData_createsReview() {
            // given
            UUID requesterId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();
            ReviewCreateRequest request = new ReviewCreateRequest(contentId, "좋아요", 5.0);

            UserModel author = mock(UserModel.class);
            ContentModel content = mock(ContentModel.class);
            ReviewModel savedReview = ReviewModelFixture.create();
            ReviewResponse expectedResponse = mock(ReviewResponse.class);

            given(userService.getById(requesterId)).willReturn(author);
            given(contentService.getById(contentId)).willReturn(content);

            given(reviewService.create(content, author, request.text(), request.rating()))
                .willReturn(savedReview);

            given(reviewResponseMapper.toResponse(savedReview))
                .willReturn(expectedResponse);

            // when
            ReviewResponse result = reviewFacade.createReview(requesterId, request);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            then(reviewService).should().create(content, author, request.text(), request.rating());
        }

        @Test
        @DisplayName("존재하지 않는 콘텐츠 ID면 예외가 발생한다")
        void withNonExistingContent_throwsException() {
            // given
            UUID requesterId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();
            ReviewCreateRequest request = new ReviewCreateRequest(contentId, "내용", 1.0);

            given(userService.getById(requesterId)).willReturn(mock(UserModel.class));

            given(contentService.getById(contentId))
                .willThrow(ContentNotFoundException.withId(contentId));

            // when & then
            assertThatThrownBy(() -> reviewFacade.createReview(requesterId, request))
                .isInstanceOf(ContentNotFoundException.class);

            then(reviewService).should(never()).create(any(), any(), any(), anyDouble());
        }
    }

    @Nested
    @DisplayName("getReviews()")
    class GetReviewsTest {

        @Test
        @DisplayName("요청이 주어지면 리뷰 목록을 조회하고 응답을 반환한다")
        void withValidRequest_returnsReviews() {
            // given
            UUID contentId = UUID.randomUUID();
            ReviewQueryRequest request = new ReviewQueryRequest(
                contentId, null, null, 10, SortDirection.DESCENDING, ReviewSortField.createdAt
            );

            ReviewModel review1 = ReviewModelFixture.create();
            ReviewModel review2 = ReviewModelFixture.create();

            ReviewResponse response1 = mock(ReviewResponse.class);
            ReviewResponse response2 = mock(ReviewResponse.class);

            CursorResponse<ReviewModel> serviceResponse = CursorResponse.of(
                List.of(review1, review2),
                null,
                null,
                false,
                2,
                "createdAt",
                SortDirection.DESCENDING
            );

            given(reviewService.getAll(request)).willReturn(serviceResponse);
            given(reviewResponseMapper.toResponse(review1)).willReturn(response1);
            given(reviewResponseMapper.toResponse(review2)).willReturn(response2);

            // when
            CursorResponse<ReviewResponse> result = reviewFacade.getReviews(request);

            // then
            assertThat(result.data()).hasSize(2);
            assertThat(result.data().getFirst()).isEqualTo(response1);
            assertThat(result.data().get(1)).isEqualTo(response2);
            assertThat(result.hasNext()).isFalse();
            assertThat(result.totalCount()).isEqualTo(2);

            then(reviewService).should().getAll(request);
        }

        @Test
        @DisplayName("결과가 없으면 빈 응답을 반환한다")
        void withNoResults_returnsEmptyResponse() {
            // given
            ReviewQueryRequest request = new ReviewQueryRequest(
                null, null, null, 10, SortDirection.DESCENDING, ReviewSortField.createdAt
            );

            CursorResponse<ReviewModel> emptyResponse = CursorResponse.empty(
                "createdAt",
                SortDirection.DESCENDING
            );

            given(reviewService.getAll(request)).willReturn(emptyResponse);

            // when
            CursorResponse<ReviewResponse> result = reviewFacade.getReviews(request);

            // then
            assertThat(result.data()).isEmpty();
            assertThat(result.hasNext()).isFalse();
            assertThat(result.totalCount()).isZero();

            then(reviewService).should().getAll(request);
        }
    }

    @Nested
    @DisplayName("updateReview()")
    class UpdateReviewTest {

        @Test
        @DisplayName("유효한 요청이면 리뷰를 수정하고 응답을 반환한다")
        void withValidRequest_updatesReview() {
            // given
            UUID requesterId = UUID.randomUUID();
            UUID reviewId = UUID.randomUUID();
            ReviewUpdateRequest request = new ReviewUpdateRequest("수정된 내용", 4.5);

            ReviewModel updatedReview = ReviewModelFixture.create();
            ReviewResponse expectedResponse = mock(ReviewResponse.class);

            given(userService.getById(requesterId)).willReturn(mock(UserModel.class));

            given(reviewService.update(reviewId, requesterId, request.text(), request.rating()))
                .willReturn(updatedReview);

            given(reviewResponseMapper.toResponse(updatedReview))
                .willReturn(expectedResponse);

            // when
            ReviewResponse result = reviewFacade.updateReview(requesterId, reviewId, request);

            // then
            assertThat(result).isEqualTo(expectedResponse);

            then(reviewService).should().update(reviewId, requesterId, request.text(), request
                .rating());
        }

        @Test
        @DisplayName("텍스트만 수정 요청하면 텍스트만 전달한다")
        void withOnlyText_passesTextOnly() {
            // given
            UUID requesterId = UUID.randomUUID();
            UUID reviewId = UUID.randomUUID();
            ReviewUpdateRequest request = new ReviewUpdateRequest("수정된 내용", null);

            ReviewModel updatedReview = ReviewModelFixture.create();
            ReviewResponse expectedResponse = mock(ReviewResponse.class);

            given(userService.getById(requesterId)).willReturn(mock(UserModel.class));
            given(reviewService.update(reviewId, requesterId, "수정된 내용", null))
                .willReturn(updatedReview);
            given(reviewResponseMapper.toResponse(updatedReview))
                .willReturn(expectedResponse);

            // when
            ReviewResponse result = reviewFacade.updateReview(requesterId, reviewId, request);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            then(reviewService).should().update(reviewId, requesterId, "수정된 내용", null);
        }

        @Test
        @DisplayName("평점만 수정 요청하면 평점만 전달한다")
        void withOnlyRating_passesRatingOnly() {
            // given
            UUID requesterId = UUID.randomUUID();
            UUID reviewId = UUID.randomUUID();
            ReviewUpdateRequest request = new ReviewUpdateRequest(null, 4.5);

            ReviewModel updatedReview = ReviewModelFixture.create();
            ReviewResponse expectedResponse = mock(ReviewResponse.class);

            given(userService.getById(requesterId)).willReturn(mock(UserModel.class));
            given(reviewService.update(reviewId, requesterId, null, 4.5))
                .willReturn(updatedReview);
            given(reviewResponseMapper.toResponse(updatedReview))
                .willReturn(expectedResponse);

            // when
            ReviewResponse result = reviewFacade.updateReview(requesterId, reviewId, request);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            then(reviewService).should().update(reviewId, requesterId, null, 4.5);
        }
    }

    @Nested
    @DisplayName("deleteReview()")
    class DeleteReviewTest {

        @Test
        @DisplayName("요청 시 사용자 확인 후 리뷰 삭제 서비스를 호출한다")
        void validatesUserAndDeletesReview() {
            // given
            UUID requesterId = UUID.randomUUID();
            UUID reviewId = UUID.randomUUID();

            given(userService.getById(requesterId)).willReturn(mock(UserModel.class));

            // when
            reviewFacade.deleteReview(requesterId, reviewId);

            // then
            then(userService).should().getById(requesterId);
            then(reviewService).should().delete(reviewId, requesterId);
        }
    }
}
