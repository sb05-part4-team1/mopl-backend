package com.mopl.api.application.review;

import com.mopl.api.interfaces.api.review.ReviewCreateRequest;
import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.service.review.ReviewService;
import com.mopl.domain.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewFacade 단위 테스트")
class ReviewFacadeTest {

    @Mock
    private ReviewService reviewService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ReviewFacade reviewFacade;

    @Nested
    @DisplayName("createReview()")
    class CreateReviewTest {

        @Test
        @DisplayName("요청자 ID로 author(UserModel id만 세팅)를 만들어 ReviewService.create를 호출한다")
        void withValidRequest_callsReviewServiceCreate() {
            // given
            UUID requesterId = UUID.randomUUID();

            UUID contentId = UUID.randomUUID();
            String text = "리뷰 내용";
            BigDecimal rating = BigDecimal.valueOf(4);

            ReviewCreateRequest request = new ReviewCreateRequest(contentId, text, rating);

            ReviewModel saved = ReviewModel.builder()
                .id(UUID.randomUUID())
                .contentId(contentId)
                .author(UserModel.builder().id(requesterId).build())
                .text(text)
                .rating(rating)
                .build();

            given(reviewService.create(any(UUID.class), any(UserModel.class), any(String.class),
                any(BigDecimal.class)))
                .willReturn(saved);

            // when
            ReviewModel result = reviewFacade.createReview(requesterId, request);

            // then
            assertThat(result).isEqualTo(saved);

            ArgumentCaptor<UserModel> authorCaptor = ArgumentCaptor.forClass(UserModel.class);

            then(reviewService).should().create(
                org.mockito.ArgumentMatchers.eq(contentId),
                authorCaptor.capture(),
                org.mockito.ArgumentMatchers.eq(text),
                org.mockito.ArgumentMatchers.eq(rating)
            );

            UserModel capturedAuthor = authorCaptor.getValue();
            assertThat(capturedAuthor.getId()).isEqualTo(requesterId);
        }

        @Test
        @DisplayName("요청자 ID가 null이면 author.id도 null인 채로 ReviewService에 전달된다")
        void withNullRequesterId_passesNullAuthorId() {
            // given
            UUID contentId = UUID.randomUUID();
            String text = "리뷰";
            BigDecimal rating = BigDecimal.valueOf(4);

            ReviewCreateRequest request = new ReviewCreateRequest(contentId, text, rating);

            ReviewModel returned = ReviewModel.builder()
                .id(UUID.randomUUID())
                .build();

            given(reviewService.create(any(UUID.class), any(UserModel.class), any(String.class),
                any(BigDecimal.class)))
                .willReturn(returned);

            // when
            ReviewModel result = reviewFacade.createReview(null, request);

            // then
            assertThat(result).isEqualTo(returned);

            ArgumentCaptor<UserModel> authorCaptor = ArgumentCaptor.forClass(UserModel.class);

            then(reviewService).should().create(
                org.mockito.ArgumentMatchers.eq(contentId),
                authorCaptor.capture(),
                org.mockito.ArgumentMatchers.eq(text),
                org.mockito.ArgumentMatchers.eq(rating)
            );

            assertThat(authorCaptor.getValue().getId()).isNull();
        }
    }
}
