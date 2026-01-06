package com.mopl.api.application.review;

import com.mopl.api.interfaces.api.review.ReviewCreateRequest;
import com.mopl.api.interfaces.api.review.ReviewResponse;
import com.mopl.api.interfaces.api.review.ReviewResponseMapper;
import com.mopl.api.interfaces.api.review.ReviewUpdateRequest;
import com.mopl.domain.exception.review.InvalidReviewDataException;
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

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
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
            String text = "좋아요";
            BigDecimal rating = new BigDecimal("5.0");
            ReviewCreateRequest request = new ReviewCreateRequest(contentId, text, rating);

            UserModel author = UserModel.builder().id(requesterId).name("테스터").build();
            ReviewModel savedReview = ReviewModel.builder().id(UUID.randomUUID()).build();
            ReviewResponse expectedResponse = new ReviewResponse(
                savedReview.getId(), contentId, null, text, rating
            );

            // Mocking
            given(userService.getById(requesterId)).willReturn(author);
            given(contentService.exists(contentId)).willReturn(true);
            given(reviewService.create(eq(contentId), eq(author), eq(text), eq(rating)))
                .willReturn(savedReview);
            given(reviewResponseMapper.toResponse(savedReview, author))
                .willReturn(expectedResponse);

            // when
            ReviewResponse result = reviewFacade.createReview(requesterId, request);

            // then
            assertThat(result).isEqualTo(expectedResponse);

            // Verify
            then(contentService).should().exists(contentId);
            then(reviewService).should().create(any(), any(), any(), any());
        }

        @Test
        @DisplayName("존재하지 않는 콘텐츠 ID면 InvalidReviewDataException이 발생한다")
        void withNonExistingContent_throwsException() {
            // given
            UUID requesterId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();
            ReviewCreateRequest request = new ReviewCreateRequest(contentId, "내용", BigDecimal.ONE);

            UserModel author = UserModel.builder().id(requesterId).build();

            // Mocking
            given(userService.getById(requesterId)).willReturn(author);
            given(contentService.exists(contentId)).willReturn(false); // 존재하지 않음 설정

            // when & then
            assertThatThrownBy(() -> reviewFacade.createReview(requesterId, request))
                .isInstanceOf(InvalidReviewDataException.class);

            // Verify: 리뷰 생성 로직은 절대 실행되면 안 됨
            then(reviewService).should(never()).create(any(), any(), any(), any());
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
            String newText = "수정된 내용";
            BigDecimal newRating = new BigDecimal("4.0");
            ReviewUpdateRequest request = new ReviewUpdateRequest(newText, newRating);

            UserModel requester = UserModel.builder().id(requesterId).name("작성자").build();
            ReviewModel updatedReview = ReviewModel.builder().id(reviewId).text(newText).build();
            ReviewResponse expectedResponse = new ReviewResponse(
                reviewId, UUID.randomUUID(), null, newText, newRating
            );

            // Mocking
            given(userService.getById(requesterId)).willReturn(requester);
            given(reviewService.update(eq(reviewId), eq(requesterId), eq(newText), eq(newRating)))
                .willReturn(updatedReview);
            given(reviewResponseMapper.toResponse(updatedReview, requester))
                .willReturn(expectedResponse);

            // when
            ReviewResponse result = reviewFacade.updateReview(requesterId, reviewId, request);

            // then
            assertThat(result).isEqualTo(expectedResponse);

            // Verify
            then(reviewService).should().update(any(), any(), any(), any());
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
            UserModel requester = UserModel.builder().id(requesterId).build();

            given(userService.getById(requesterId)).willReturn(requester);

            // when
            reviewFacade.deleteReview(requesterId, reviewId);

            // then
            then(userService).should().getById(requesterId);
            then(reviewService).should().delete(reviewId, requesterId);
        }
    }
}
