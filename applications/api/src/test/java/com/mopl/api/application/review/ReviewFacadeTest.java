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

    @Mock
    private ReviewService reviewService;

    @Mock
    private UserService userService;

    @Mock
    private ContentService contentService;

    @Mock
    private ReviewResponseMapper reviewResponseMapper;

    @InjectMocks
    private ReviewFacade reviewFacade;

    @Nested
    @DisplayName("createReview()")
    class CreateReviewTest {

        @Test
        @DisplayName("콘텐츠가 존재하고 유저가 유효하면 리뷰를 생성한다")
        void withValidData_createsReview() {
            // given
            UUID requesterId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();
            String text = "굿";
            BigDecimal rating = new BigDecimal("5.0");

            ReviewCreateRequest request = new ReviewCreateRequest(contentId, text, rating);

            UserModel author = UserModel.builder().id(requesterId).name("테스터").build();
            ReviewModel savedReview = ReviewModel.builder().id(UUID.randomUUID()).build();
            ReviewResponse expectedResponse = new ReviewResponse(savedReview.getId(), contentId,
                null, text, rating);

            // Mocking
            given(userService.getById(requesterId)).willReturn(author);
            given(contentService.exists(contentId)).willReturn(true); // [체크] 콘텐츠 존재함

            // Service 호출 시 파라미터 매칭 (any() 사용 시 타입을 명시하면 더 안전함)
            given(reviewService.create(eq(contentId), eq(author), eq(text), eq(rating)))
                .willReturn(savedReview);

            given(reviewResponseMapper.toResponse(savedReview, author)).willReturn(
                expectedResponse);

            // when
            ReviewResponse result = reviewFacade.createReview(requesterId, request);

            // then
            assertThat(result).isEqualTo(expectedResponse);

            // 검증 로직 호출 확인
            then(contentService).should().exists(contentId);
            then(reviewService).should().create(eq(contentId), eq(author), eq(text), eq(rating));
        }

        @Test
        @DisplayName("존재하지 않는 콘텐츠 ID면 예외가 발생하고 서비스는 호출되지 않는다")
        void withNonExistingContent_throwsException() {
            // given
            UUID requesterId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();
            ReviewCreateRequest request = new ReviewCreateRequest(contentId, "굿", new BigDecimal(
                "5.0"));

            UserModel author = UserModel.builder().id(requesterId).build();

            // Mocking
            given(userService.getById(requesterId)).willReturn(author);
            given(contentService.exists(contentId)).willReturn(false); // [체크] 콘텐츠 없음!

            // when & then
            assertThatThrownBy(() -> reviewFacade.createReview(requesterId, request))
                // [핵심] 사용자의 Exception 클래스 타입 확인
                .isInstanceOf(InvalidReviewDataException.class);

            // [검증] ReviewService.create는 절대 실행되면 안 됨!
            then(reviewService).should(never()).create(any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("updateReview()")
    class UpdateReviewTest {

        @Test
        @DisplayName("요청자 정보와 수정 내용을 서비스에 전달하여 리뷰를 수정한다")
        void withValidRequest_updatesReview() {
            // given
            UUID requesterId = UUID.randomUUID();
            UUID reviewId = UUID.randomUUID();
            String newText = "수정된 내용";
            BigDecimal newRating = new BigDecimal("4.0");

            ReviewUpdateRequest request = new ReviewUpdateRequest(newText, newRating);

            UserModel requester = UserModel.builder().id(requesterId).name("작성자").build();

            // 업데이트된 모델 결과 (가정)
            ReviewModel updatedReview = ReviewModel.builder()
                .id(reviewId)
                .authorId(requesterId)
                .text(newText)
                .rating(newRating)
                .build();

            ReviewResponse expectedResponse = new ReviewResponse(reviewId, UUID.randomUUID(), null,
                newText, newRating);

            // Mocking
            given(userService.getById(requesterId)).willReturn(requester);

            given(reviewService.update(
                eq(reviewId),
                eq(requesterId),
                eq(newText),
                eq(newRating)
            )).willReturn(updatedReview);

            given(reviewResponseMapper.toResponse(updatedReview, requester)).willReturn(
                expectedResponse);

            // when
            ReviewResponse result = reviewFacade.updateReview(requesterId, reviewId, request);

            // then
            assertThat(result).isEqualTo(expectedResponse);

            // Verify
            then(reviewService).should().update(reviewId, requesterId, newText, newRating);
            then(reviewResponseMapper).should().toResponse(updatedReview, requester);
        }
    }
}
