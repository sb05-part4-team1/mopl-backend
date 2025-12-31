package com.mopl.jpa.repository.review;

import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.repository.review.ReviewRepository;
import com.mopl.jpa.config.JpaConfig;
import com.mopl.jpa.entity.review.ReviewEntityMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
    JpaConfig.class,
    ReviewRepositoryImpl.class,
    ReviewEntityMapper.class
})
@DisplayName("ReviewRepositoryImpl 슬라이스 테스트")
class ReviewRepositoryImplTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Nested
    @DisplayName("save()")
    class SaveTest {

        @Test
        @DisplayName("새 리뷰를 저장하면 ID가 생성되고 저장된 정보를 반환한다")
        void withNewReview_savesAndReturnsReview() {
            // given
            UUID contentId = UUID.randomUUID();
            UUID authorId = UUID.randomUUID();
            String text = "테스트 리뷰입니다.";

            // [주의] ReviewModel의 엄격한 검증 통과를 위해 안전한 값 사용 (String 생성자 추천)
            BigDecimal rating = new BigDecimal("4.5");

            ReviewModel reviewModel = ReviewModel.builder()
                .contentId(contentId)
                .authorId(authorId)
                .text(text)
                .rating(rating)
                .build();

            // when
            ReviewModel savedReview = reviewRepository.save(reviewModel);

            // then
            assertThat(savedReview.getId()).isNotNull();
            assertThat(savedReview.getContentId()).isEqualTo(contentId);
            assertThat(savedReview.getAuthorId()).isEqualTo(authorId);
            assertThat(savedReview.getText()).isEqualTo(text);
            assertThat(savedReview.getRating()).isEqualByComparingTo(rating);
            assertThat(savedReview.getCreatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindByIdTest {

        @Test
        @DisplayName("존재하는 ID로 조회하면 해당 리뷰를 반환한다")
        void withExistingId_returnsReview() {
            // given
            // 1. 먼저 데이터를 저장해서 ID를 확보
            ReviewModel review = ReviewModel.builder()
                .contentId(UUID.randomUUID())
                .authorId(UUID.randomUUID())
                .text("조회용 리뷰")
                .rating(new BigDecimal("5.0"))
                .build();

            ReviewModel savedReview = reviewRepository.save(review);
            UUID savedId = savedReview.getId();

            // when
            Optional<ReviewModel> result = reviewRepository.findById(savedId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(savedId);
            assertThat(result.get().getText()).isEqualTo("조회용 리뷰");
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 빈 Optional을 반환한다")
        void withNonExistingId_returnsEmpty() {
            // given
            UUID nonExistingId = UUID.randomUUID();

            // when
            Optional<ReviewModel> result = reviewRepository.findById(nonExistingId);

            // then
            assertThat(result).isEmpty();
        }
    }
}
