package com.mopl.api.interfaces.api.review;

import com.mopl.api.interfaces.api.user.UserSummary;
import com.mopl.api.interfaces.api.user.UserSummaryMapper;
import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.model.user.UserModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ReviewResponseMapper 단위 테스트")
class ReviewResponseMapperTest {

    private final UserSummaryMapper userSummaryMapper = new UserSummaryMapper();
    private final ReviewResponseMapper reviewResponseMapper = new ReviewResponseMapper(
        userSummaryMapper);

    @Test
    @DisplayName("ReviewModel과 UserModel을 받아 ReviewResponse로 변환한다")
    void toResponse_withAuthor_mapsToResponse() {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID contentId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        // 작성자 정보
        UserModel author = UserModel.builder()
            .id(authorId)
            .name("홍길동")
            .profileImageUrl("https://example.com/profile.png")
            .build();

        // 🚨 [수정] BigDecimal("4.0")을 사용하여 Scale 문제 예방
        ReviewModel reviewModel = ReviewModel.builder()
            .id(reviewId)
            .contentId(contentId)
            .authorId(authorId)
            .text("리뷰 내용")
            .rating(new BigDecimal("4.0"))
            .build();

        // when
        ReviewResponse response = reviewResponseMapper.toResponse(reviewModel, author);

        // then
        assertThat(response.id()).isEqualTo(reviewId);
        assertThat(response.contentId()).isEqualTo(contentId);
        assertThat(response.text()).isEqualTo("리뷰 내용");

        // 값 비교 (4.0 == 4.00)
        assertThat(response.rating()).isEqualByComparingTo(new BigDecimal("4.0"));

        assertThat(response.author()).isNotNull();
        assertThat(response.author()).isInstanceOf(UserSummary.class);
        assertThat(response.author().userId()).isEqualTo(authorId);
        assertThat(response.author().name()).isEqualTo("홍길동");
        assertThat(response.author().profileImageUrl()).isEqualTo(
            "https://example.com/profile.png");
    }

    @Test
    @DisplayName("넘겨받은 author가 null이면 Response의 author도 null이다")
    void toResponse_withNullAuthor_mapsAuthorToNull() {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID contentId = UUID.randomUUID();

        // 🚨 [수정] 안전한 값 사용
        ReviewModel reviewModel = ReviewModel.builder()
            .id(reviewId)
            .contentId(contentId)
            .authorId(UUID.randomUUID())
            .text("리뷰 내용")
            .rating(new BigDecimal("3.0"))
            .build();

        // when
        ReviewResponse response = reviewResponseMapper.toResponse(reviewModel, null);

        // then
        assertThat(response.id()).isEqualTo(reviewId);
        assertThat(response.contentId()).isEqualTo(contentId);
        assertThat(response.text()).isEqualTo("리뷰 내용");
        assertThat(response.rating()).isEqualByComparingTo(new BigDecimal("3.0"));

        // author 정보가 null로 매핑되었는지 확인
        assertThat(response.author()).isNull();
    }

    @Test
    @DisplayName("author의 필드(name/image)가 null이어도 UserSummary에 그대로 매핑된다")
    void toResponse_withAuthorNullFields_mapsNulls() {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID contentId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        UserModel author = UserModel.builder()
            .id(authorId)
            .name(null)
            .profileImageUrl(null)
            .build();

        // 🚨 [수정] 안전한 값 사용
        ReviewModel reviewModel = ReviewModel.builder()
            .id(reviewId)
            .contentId(contentId)
            .authorId(authorId)
            .text("리뷰 내용")
            .rating(new BigDecimal("5.0"))
            .build();

        // when
        ReviewResponse response = reviewResponseMapper.toResponse(reviewModel, author);

        // then
        assertThat(response.author()).isNotNull();
        assertThat(response.author().userId()).isEqualTo(authorId);
        assertThat(response.author().name()).isNull();
        assertThat(response.author().profileImageUrl()).isNull();
    }
}
