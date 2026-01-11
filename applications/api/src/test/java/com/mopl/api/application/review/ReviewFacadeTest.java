package com.mopl.api.application.review;

import com.mopl.api.interfaces.api.review.ReviewCreateRequest;
import com.mopl.api.interfaces.api.review.ReviewResponse;
import com.mopl.api.interfaces.api.review.ReviewResponseMapper;
import com.mopl.api.interfaces.api.review.ReviewUpdateRequest;
import com.mopl.domain.exception.content.ContentNotFoundException;
import com.mopl.domain.fixture.ReviewModelFixture;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.service.content.ContentService;
import com.mopl.domain.service.review.ReviewService;
import com.mopl.domain.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    @DisplayName("updateReview()")
    class UpdateReviewTest {

        @Test
        @DisplayName("유효한 요청이면 리뷰를 수정하고 응답을 반환한다")
        void withValidRequest_updatesReview() {
            // given
            UUID requesterId = UUID.randomUUID();
            UUID reviewId = UUID.randomUUID();
            ReviewUpdateRequest request = new ReviewUpdateRequest("수정된 내용", 4.5);

            // Fixture 활용: 업데이트된 결과 모델 생성
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
