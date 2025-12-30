package com.mopl.domain.service.review;

import com.mopl.domain.exception.review.InvalidReviewDataException;
import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.model.user.UserModel;
// [변경] Repository 대신 Service import
import com.mopl.domain.service.content.ContentService;
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

    // [변경] ContentRepository -> ContentService로 교체
    @Mock
    private ContentService contentService;

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

            // [변경] repository.existsById -> service.exists
            given(contentService.exists(contentId)).willReturn(true);

            // save() 호출 시 들어온 객체를 그대로 반환하도록 설정
            given(reviewRepository.save(any(ReviewModel.class)))
                    .willAnswer(invocation -> invocation.getArgument(0, ReviewModel.class));

            // when
            ReviewModel result = reviewService.create(contentId, author, text, rating);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContentId()).isEqualTo(contentId);
            assertThat(result.getAuthorId()).isEqualTo(authorId);
            assertThat(result.getText()).isEqualTo(text);
            assertThat(result.getRating()).isEqualTo(rating);

            // [변경] 검증 대상도 service로 변경
            then(contentService).should().exists(contentId);
            then(reviewRepository).should().save(any(ReviewModel.class));
        }

        @Test
        @DisplayName("콘텐츠가 존재하지 않으면 예외 발생 및 저장하지 않는다")
        void withNotExistingContent_throwsException() {
            // given
            UUID contentId = UUID.randomUUID();
            UserModel author = UserModel.builder().id(UUID.randomUUID()).build();

            // [변경] service.exists가 false를 리턴하도록 설정
            given(contentService.exists(contentId)).willReturn(false);

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
                        assertThat(ex.getDetails().get("detailMessage"))
                                .isEqualTo("존재하지 않는 콘텐츠입니다. contentId=" + contentId);
                    });

            // [변경] 검증 대상 변경
            then(contentService).should().exists(contentId);
            then(reviewRepository).should(never()).save(any(ReviewModel.class));
        }

        @Test
        @DisplayName("authorId가 null이면 ReviewModel 유효성 예외가 발생하고 저장하지 않는다")
        void withNullAuthorId_throwsException() {
            // given
            UUID contentId = UUID.randomUUID();
            // [변경] 유효성 검사 전에 콘텐츠 존재 확인을 먼저 하므로 true 리턴 필요
            given(contentService.exists(contentId)).willReturn(true);

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
                        assertThat(ex.getDetails().get("detailMessage"))
                                .isEqualTo("작성자 ID는 null일 수 없습니다.");
                    });

            then(contentService).should().exists(contentId);
            then(reviewRepository).should(never()).save(any(ReviewModel.class));
        }

        @Test
        @DisplayName("rating이 null이면 ReviewModel 유효성 예외가 발생하고 저장하지 않는다")
        void withNullRating_throwsException() {
            // given
            UUID contentId = UUID.randomUUID();
            UserModel author = UserModel.builder().id(UUID.randomUUID()).build();

            given(contentService.exists(contentId)).willReturn(true);

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
                        assertThat(ex.getDetails().get("detailMessage"))
                                .isEqualTo("평점은 null일 수 없습니다.");
                    });

            then(contentService).should().exists(contentId);
            then(reviewRepository).should(never()).save(any(ReviewModel.class));
        }

        @Test
        @DisplayName("평점이 범위를 벗어나면 예외 발생하고 저장하지 않는다")
        void withInvalidRating_throwsException() {
            // given
            UUID contentId = UUID.randomUUID();
            UserModel author = UserModel.builder().id(UUID.randomUUID()).build();

            given(contentService.exists(contentId)).willReturn(true);

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
                        assertThat(ex.getDetails().get("detailMessage"))
                                .isEqualTo("평점은 0.0 이상 5.0 이하만 가능합니다.");
                    });

            then(contentService).should().exists(contentId);
            then(reviewRepository).should(never()).save(any(ReviewModel.class));
        }
    }
}