package com.mopl.api.application.review;

import com.mopl.api.interfaces.api.review.ReviewCreateRequest;
import com.mopl.api.interfaces.api.review.ReviewResponse;
import com.mopl.api.interfaces.api.review.ReviewResponseMapper;
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
    private ContentService contentService; // [핵심] 이제 Facade가 ContentService를 씁니다

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
            ReviewCreateRequest request = new ReviewCreateRequest(contentId, "굿", BigDecimal.valueOf(5));

            UserModel author = UserModel.builder().id(requesterId).build();
            ReviewModel savedReview = ReviewModel.builder().id(UUID.randomUUID()).build();
            ReviewResponse response = new ReviewResponse(savedReview.getId(), contentId, null, "굿", BigDecimal.valueOf(5));

            // Mocking
            given(userService.getById(requesterId)).willReturn(author);
            given(contentService.exists(contentId)).willReturn(true); // [체크] 콘텐츠 존재함
            given(reviewService.create(eq(contentId), eq(author), any(), any())).willReturn(savedReview);
            given(reviewResponseMapper.toResponse(savedReview, author)).willReturn(response);

            // when
            ReviewResponse result = reviewFacade.createReview(requesterId, request);

            // then
            assertThat(result).isEqualTo(response);

            // 검증 로직 호출 확인
            then(contentService).should().exists(contentId);
            then(reviewService).should().create(eq(contentId), eq(author), any(), any());
        }

        @Test
        @DisplayName("존재하지 않는 콘텐츠 ID면 예외가 발생하고 서비스는 호출되지 않는다")
        void withNonExistingContent_throwsException() {
            // given
            UUID requesterId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();
            ReviewCreateRequest request = new ReviewCreateRequest(contentId, "굿", BigDecimal.valueOf(5));

            UserModel author = UserModel.builder().id(requesterId).build();

            // Mocking
            given(userService.getById(requesterId)).willReturn(author);
            given(contentService.exists(contentId)).willReturn(false); // [체크] 콘텐츠 없음!

            // when & then
            assertThatThrownBy(() -> reviewFacade.createReview(requesterId, request))
                    // 🚨 [수정됨] IllegalArgumentException -> InvalidReviewDataException
                    .isInstanceOf(InvalidReviewDataException.class)

                    .hasMessageContaining("리뷰 데이터가 유효하지 않습니다.");

            // [검증] ReviewService.create는 절대 실행되면 안 됨!
            then(reviewService).should(never()).create(any(), any(), any(), any());
        }
    }
}