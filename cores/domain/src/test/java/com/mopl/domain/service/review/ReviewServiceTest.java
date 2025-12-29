package com.mopl.domain.service.review;

import com.mopl.domain.exception.review.InvalidReviewDataException;
import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.domain.repository.review.ReviewRepository;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService 단위 테스트")
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ContentRepository contentRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Nested
    @DisplayName("create()")
    class CreateTest {

        @Test
        @DisplayName("콘텐츠가 존재하면 리뷰를 생성하고 저장한다")
        void withExistingContent_createsAndSavesReview() {
            // given
            UUID contentId = UUID.randomUUID();
            UUID authorId = UUID.randomUUID();

            UserModel author = UserModel.builder()
                    .id(authorId)
                    .build();

            String text = "리뷰 내용";
            BigDecimal rating = BigDecimal.valueOf(4);

            given(contentRepository.existsById(contentId)).willReturn(true);

            // save() 호출 시 들어온 객체를 그대로 반환하도록 설정
            given(reviewRepository.save(any(ReviewModel.class)))
                    .willAnswer(invocation -> invocation.getArgument(0, ReviewModel.class));

            // when
            ReviewModel result = reviewService.create(contentId, author, text, rating);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContentId()).isEqualTo(contentId);
            // [수정] 객체 비교가 아니라 ID 비교로 변경
            assertThat(result.getAuthorId()).isEqualTo(authorId);
            assertThat(result.getText()).isEqualTo(text);
            assertThat(result.getRating()).isEqualTo(rating);

            then(contentRepository).should().existsById(contentId);
            then(reviewRepository).should().save(any(ReviewModel.class));
        }

        @Test
        @DisplayName("콘텐츠가 존재하지 않으면 예외 발생 및 저장하지 않는다")
        void withNotExistingContent_throwsException() {
            // given
            UUID contentId = UUID.randomUUID();
            UserModel author = UserModel.builder().id(UUID.randomUUID()).build();

            given(contentRepository.existsById(contentId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> reviewService.create(
                    contentId,
                    author,
                    "리뷰",
                    BigDecimal.valueOf(4)
            ))
                    .isInstanceOf(InvalidReviewDataException.class)
                    .satisfies(e -> {
                        InvalidReviewDataException ex = (InvalidReviewDataException) e;
                        // ReviewService에서 던지는 메시지는 기존과 동일
                        assertThat(ex.getDetails().get("detailMessage"))
                                .isEqualTo("존재하지 않는 콘텐츠입니다. contentId=" + contentId);
                    });

            then(contentRepository).should().existsById(contentId);
            then(reviewRepository).should(never()).save(any(ReviewModel.class));
        }

        @Test
        @DisplayName("authorId가 null이면 ReviewModel 유효성 예외가 발생하고 저장하지 않는다")
        void withNullAuthorId_throwsException() {
            // given
            UUID contentId = UUID.randomUUID();
            given(contentRepository.existsById(contentId)).willReturn(true);

            // [수정] Service가 author.getId()를 호출하므로, 객체는 있되 ID가 null인 상태로 테스트
            UserModel authorWithNullId = UserModel.builder().id(null).build();

            // when & then
            assertThatThrownBy(() -> reviewService.create(
                    contentId,
                    authorWithNullId,
                    "리뷰",
                    BigDecimal.valueOf(4)
            ))
                    .isInstanceOf(InvalidReviewDataException.class)
                    .satisfies(e -> {
                        InvalidReviewDataException ex = (InvalidReviewDataException) e;
                        // [수정] ReviewModel의 변경된 메시지 반영
                        assertThat(ex.getDetails().get("detailMessage"))
                                .isEqualTo("작성자 ID는 null일 수 없습니다.");
                    });

            then(contentRepository).should().existsById(contentId);
            then(reviewRepository).should(never()).save(any(ReviewModel.class));
        }

        @Test
        @DisplayName("rating이 null이면 ReviewModel 유효성 예외가 발생하고 저장하지 않는다")
        void withNullRating_throwsException() {
            // given
            UUID contentId = UUID.randomUUID();
            UserModel author = UserModel.builder().id(UUID.randomUUID()).build();

            given(contentRepository.existsById(contentId)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> reviewService.create(
                    contentId,
                    author,
                    "리뷰",
                    null
            ))
                    .isInstanceOf(InvalidReviewDataException.class)
                    .satisfies(e -> {
                        InvalidReviewDataException ex = (InvalidReviewDataException) e;
                        // [수정] ReviewModel의 변경된 메시지 반영
                        assertThat(ex.getDetails().get("detailMessage"))
                                .isEqualTo("평점은 null일 수 없습니다.");
                    });

            then(contentRepository).should().existsById(contentId);
            then(reviewRepository).should(never()).save(any(ReviewModel.class));
        }

        @Test
        @DisplayName("평점이 범위를 벗어나면 예외 발생하고 저장하지 않는다")
        void withInvalidRating_throwsException() {
            // given
            UUID contentId = UUID.randomUUID();
            UserModel author = UserModel.builder().id(UUID.randomUUID()).build();

            given(contentRepository.existsById(contentId)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> reviewService.create(
                    contentId,
                    author,
                    "리뷰",
                    BigDecimal.valueOf(6)
            ))
                    .isInstanceOf(InvalidReviewDataException.class)
                    .satisfies(e -> {
                        InvalidReviewDataException ex = (InvalidReviewDataException) e;
                        // 기존 메시지와 동일
                        assertThat(ex.getDetails().get("detailMessage"))
                                .isEqualTo("평점은 0.0 이상 5.0 이하만 가능합니다.");
                    });

            then(contentRepository).should().existsById(contentId);
            then(reviewRepository).should(never()).save(any(ReviewModel.class));
        }
    }
}