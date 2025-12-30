package com.mopl.api.application.review;

import com.mopl.api.interfaces.api.review.ReviewCreateRequest;
import com.mopl.api.interfaces.api.review.ReviewResponse;
import com.mopl.api.interfaces.api.review.ReviewResponseMapper;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewFacade 단위 테스트")
class ReviewFacadeTest {

    @Mock
    private ReviewService reviewService;

    @Mock
    private UserService userService; // 현재는 사용되지 않지만 구조상 유지

    @Mock
    private ReviewResponseMapper reviewResponseMapper; // [추가] 매퍼 모킹

    @InjectMocks
    private ReviewFacade reviewFacade;

    @Nested
    @DisplayName("createReview()")
    class CreateReviewTest {

        @Test
        @DisplayName("요청자 ID로 author를 생성하여 저장하고, 결과를 Response로 변환하여 반환한다")
        void withValidRequest_callsServiceAndMapper() {
            // given
            UUID requesterId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();
            String text = "리뷰 내용";
            BigDecimal rating = BigDecimal.valueOf(4);

            ReviewCreateRequest request = new ReviewCreateRequest(contentId, text, rating);

            // Service가 반환할 모델 (ID만 가지고 있음)
            ReviewModel savedModel = ReviewModel.builder()
                .id(UUID.randomUUID())
                .contentId(contentId)
                .authorId(requesterId) // [변경] author 객체 대신 ID
                .text(text)
                .rating(rating)
                .build();

            // Mapper가 반환할 최종 응답
            ReviewResponse expectedResponse = new ReviewResponse(
                savedModel.getId(),
                contentId,
                null, // 테스트에선 중요하지 않으므로 null 혹은 더미 객체
                text,
                rating
            );

            // 1. Service Mocking
            given(reviewService.create(
                eq(contentId),
                any(UserModel.class), // Facade가 내부에서 생성한 UserModel
                eq(text),
                eq(rating)
            )).willReturn(savedModel);

            // 2. Mapper Mocking
            given(reviewResponseMapper.toResponse(
                eq(savedModel),
                any(UserModel.class)
            )).willReturn(expectedResponse);

            // when
            ReviewResponse result = reviewFacade.createReview(requesterId, request);

            // then
            assertThat(result).isEqualTo(expectedResponse);

            // Verify Service Call (authorCaptor로 Facade가 만든 UserModel 검증)
            ArgumentCaptor<UserModel> authorCaptor = ArgumentCaptor.forClass(UserModel.class);
            then(reviewService).should().create(
                eq(contentId),
                authorCaptor.capture(),
                eq(text),
                eq(rating)
            );

            // Facade가 ID를 기반으로 UserModel을 잘 만들어서 넘겼는지 확인
            assertThat(authorCaptor.getValue().getId()).isEqualTo(requesterId);

            // Verify Mapper Call (모델과 유저 정보를 잘 넘겼는지 확인)
            then(reviewResponseMapper).should().toResponse(
                eq(savedModel),
                eq(authorCaptor.getValue()) // 위에서 캡쳐한 바로 그 author 객체여야 함
            );
        }

        @Test
        @DisplayName("요청자 ID가 null이면 author.id도 null인 상태로 처리된다")
        void withNullRequesterId_passesNullAuthorId() {
            // given
            UUID contentId = UUID.randomUUID();
            ReviewCreateRequest request = new ReviewCreateRequest(contentId, "리뷰", BigDecimal.ONE);

            ReviewModel savedModel = ReviewModel.builder()
                .id(UUID.randomUUID())
                .authorId(null) // ID 없음
                .build();

            ReviewResponse expectedResponse = new ReviewResponse(savedModel.getId(), contentId,
                null, "리뷰", BigDecimal.ONE);

            given(reviewService.create(any(), any(), any(), any())).willReturn(savedModel);
            given(reviewResponseMapper.toResponse(any(), any())).willReturn(expectedResponse);

            // when
            ReviewResponse result = reviewFacade.createReview(null, request);

            // then
            assertThat(result).isEqualTo(expectedResponse);

            ArgumentCaptor<UserModel> authorCaptor = ArgumentCaptor.forClass(UserModel.class);
            then(reviewService).should().create(
                eq(contentId),
                authorCaptor.capture(),
                any(),
                any()
            );

            // ID가 null인 UserModel이 생성되어 서비스로 넘어갔는지 확인
            assertThat(authorCaptor.getValue().getId()).isNull();
        }
    }
}
