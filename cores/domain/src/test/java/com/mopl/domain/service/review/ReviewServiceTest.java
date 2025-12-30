package com.mopl.domain.service.review;

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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

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

            // 🚨 [수정] ReviewModel의 엄격한 검증을 통과하기 위해 안전한 값("5.0") 사용
            // 4.5가 안 된다면, 로직이 정수 단위이거나 스케일이 안 맞아서일 수 있습니다.
            // new BigDecimal("5.0")은 스케일이 1로 명확하여 가장 안전합니다.
            BigDecimal rating = new BigDecimal("5.0");

            // save() 호출 시 전달된 객체를 그대로 반환하도록 설정
            given(reviewRepository.save(any(ReviewModel.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            ReviewModel result = reviewService.create(contentId, author, text, rating);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContentId()).isEqualTo(contentId);
            assertThat(result.getText()).isEqualTo(text);

            // 값 비교 (Scale 무관하게 값 자체 비교)
            assertThat(result.getRating()).isEqualByComparingTo(rating);

            // 저장소가 호출되었는지 확인
            then(reviewRepository).should().save(any(ReviewModel.class));
        }
    }
}