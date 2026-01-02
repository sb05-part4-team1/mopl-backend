package com.mopl.domain.service.review;

import com.mopl.domain.exception.review.ReviewForbiddenException;
import com.mopl.domain.exception.review.ReviewNotFoundException;
import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.review.ReviewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService 단위 테스트")
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Nested
    @DisplayName("create()")
    class CreateTest {

        @Test
        @DisplayName("유효한 정보가 주어지면 리뷰를 생성하고 저장한다")
        void withValidData_createsAndSavesReview() {
            // given
            UUID contentId = UUID.randomUUID();
            UserModel author = UserModel.builder().id(UUID.randomUUID()).build();
            String text = "리뷰 내용입니다.";
            BigDecimal rating = new BigDecimal("5.0");

            given(reviewRepository.save(any(ReviewModel.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

            // when
            ReviewModel result = reviewService.create(contentId, author, text, rating);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContentId()).isEqualTo(contentId);
            assertThat(result.getText()).isEqualTo(text);
            assertThat(result.getRating()).isEqualByComparingTo(rating);

            then(reviewRepository).should().save(any(ReviewModel.class));
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTest {

        @Test
        @DisplayName("작성자가 본인의 리뷰를 수정하면 정상적으로 업데이트되고 저장된다")
        void withOwner_updatesAndSavesReview() {
            // given
            UUID reviewId = UUID.randomUUID();
            UUID authorId = UUID.randomUUID();

            // 기존 리뷰 데이터 (평점 3.0)
            ReviewModel existingReview = ReviewModel.builder()
                .id(reviewId)
                .authorId(authorId)
                .text("기존 내용")
                .rating(new BigDecimal("3.0"))
                .build();

            // 수정할 데이터 (평점 5.0)
            String newText = "수정된 내용";
            BigDecimal newRating = new BigDecimal("5.0");

            // Mocking: 조회 시 기존 리뷰 반환, 저장 시 변경된 객체 반환
            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(existingReview));
            given(reviewRepository.save(any(ReviewModel.class))).willAnswer(invocation -> invocation
                .getArgument(0));

            // when
            ReviewModel updatedReview = reviewService.update(reviewId, authorId, newText,
                newRating);

            // then
            assertThat(updatedReview.getText()).isEqualTo(newText);
            assertThat(updatedReview.getRating()).isEqualByComparingTo(newRating);

            // Verify: 조회 후 저장이 일어났는지 확인
            then(reviewRepository).should().findById(reviewId);
            then(reviewRepository).should().save(existingReview);
        }

        @Test
        @DisplayName("존재하지 않는 리뷰 ID면 ReviewNotFoundException 발생")
        void withNonExistingId_throwsNotFoundException() {
            // given
            UUID reviewId = UUID.randomUUID();
            UUID requesterId = UUID.randomUUID();

            // Mocking: 조회 실패 (Empty)
            given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewService.update(reviewId, requesterId, "text",
                BigDecimal.ONE))
                .isInstanceOf(ReviewNotFoundException.class);

            // Verify: 저장은 절대 일어나면 안 됨
            then(reviewRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("작성자가 아닌 사람이 수정하려 하면 ReviewForbiddenException 발생")
        void withDifferentUser_throwsForbiddenException() {
            // given
            UUID reviewId = UUID.randomUUID();
            UUID authorId = UUID.randomUUID();
            UUID requesterId = UUID.randomUUID(); // 작성자와 다른 ID

            ReviewModel existingReview = ReviewModel.builder()
                .id(reviewId)
                .authorId(authorId) // 실제 주인
                .build();

            // Mocking: 조회 성공
            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(existingReview));

            // when & then
            assertThatThrownBy(() -> reviewService.update(reviewId, requesterId, "text",
                BigDecimal.ONE))
                .isInstanceOf(ReviewForbiddenException.class);

            // Verify: 저장은 절대 일어나면 안 됨
            then(reviewRepository).should(never()).save(any());
        }
    }
}
