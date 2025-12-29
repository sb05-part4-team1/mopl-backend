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
    private final ReviewResponseMapper reviewResponseMapper = new ReviewResponseMapper(userSummaryMapper);

    @Test
    @DisplayName("ReviewModel과 UserModel을 받아 ReviewResponse로 변환한다")
    void toResponse_withAuthor_mapsToResponse() {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID contentId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        // 작성자 정보 (Service나 Facade에서 조회했다고 가정)
        UserModel author = UserModel.builder()
                .id(authorId)
                .name("홍길동")
                .profileImageUrl("https://example.com/profile.png")
                .build();

        // 리뷰 모델 (ID만 가지고 있음)
        ReviewModel reviewModel = ReviewModel.builder()
                .id(reviewId)
                .contentId(contentId)
                .authorId(authorId) // [변경] author 객체 대신 ID
                .text("리뷰 내용")
                .rating(BigDecimal.valueOf(4))
                .build();

        // when
        // [변경] 두 개의 인자(리뷰 + 작성자)를 넘김
        ReviewResponse response = reviewResponseMapper.toResponse(reviewModel, author);

        // then
        assertThat(response.id()).isEqualTo(reviewId);
        assertThat(response.contentId()).isEqualTo(contentId);
        assertThat(response.text()).isEqualTo("리뷰 내용");
        assertThat(response.rating()).isEqualByComparingTo(BigDecimal.valueOf(4));

        assertThat(response.author()).isNotNull();
        assertThat(response.author()).isInstanceOf(UserSummary.class);
        assertThat(response.author().userId()).isEqualTo(authorId);
        assertThat(response.author().name()).isEqualTo("홍길동");
        assertThat(response.author().profileImageUrl()).isEqualTo("https://example.com/profile.png");
    }

    @Test
    @DisplayName("넘겨받은 author가 null이면 Response의 author도 null이다")
    void toResponse_withNullAuthor_mapsAuthorToNull() {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID contentId = UUID.randomUUID();

        ReviewModel reviewModel = ReviewModel.builder()
                .id(reviewId)
                .contentId(contentId)
                .authorId(UUID.randomUUID()) // ID는 있지만, 조회된 유저 객체가 없는 상황 가정
                .text("리뷰 내용")
                .rating(BigDecimal.valueOf(3))
                .build();

        // when
        // [변경] author 자리에 null을 넘김
        ReviewResponse response = reviewResponseMapper.toResponse(reviewModel, null);

        // then
        assertThat(response.id()).isEqualTo(reviewId);
        assertThat(response.contentId()).isEqualTo(contentId);
        assertThat(response.text()).isEqualTo("리뷰 내용");
        assertThat(response.rating()).isEqualByComparingTo(BigDecimal.valueOf(3));

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

        // 이름과 이미지가 없는 유저
        UserModel author = UserModel.builder()
                .id(authorId)
                .name(null)
                .profileImageUrl(null)
                .build();

        ReviewModel reviewModel = ReviewModel.builder()
                .id(reviewId)
                .contentId(contentId)
                .authorId(authorId)
                .text("리뷰 내용")
                .rating(BigDecimal.valueOf(5))
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