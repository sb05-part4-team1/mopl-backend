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
            BigDecimal rating = BigDecimal.valueOf(4.5);

            // ReviewModel 생성 (빌더 패턴 사용 가정)
            ReviewModel reviewModel = ReviewModel.builder()
                    .contentId(contentId)
                    .authorId(authorId)
                    .text(text)
                    .rating(rating)
                    .build();

            // when
            ReviewModel savedReview = reviewRepository.save(reviewModel);

            // then
            assertThat(savedReview.getId()).isNotNull(); // ID 자동 생성 확인
            assertThat(savedReview.getContentId()).isEqualTo(contentId);
            assertThat(savedReview.getAuthorId()).isEqualTo(authorId);
            assertThat(savedReview.getText()).isEqualTo(text);

            // BigDecimal 비교는 소수점 처리를 위해 isEqualByComparingTo 사용
            assertThat(savedReview.getRating()).isEqualByComparingTo(rating);
            assertThat(savedReview.getCreatedAt()).isNotNull();
        }
    }
}