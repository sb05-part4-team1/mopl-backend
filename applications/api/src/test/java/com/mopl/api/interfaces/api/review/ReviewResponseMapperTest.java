package com.mopl.api.interfaces.api.review;

import com.mopl.api.interfaces.api.user.UserSummary;
import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.model.user.UserModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ReviewResponseMapper 단위 테스트")
class ReviewResponseMapperTest {

    private final ReviewResponseMapper reviewResponseMapper = new ReviewResponseMapper();

    @Test
    @DisplayName("ReviewModel을 ReviewResponse로 변환한다 (UserSummary author 존재)")
    void toResponse_withAuthor_mapsToResponse() {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID contentId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        UserModel author = UserModel.builder()
            .id(authorId)
            .name("홍길동")
            .profileImageUrl("https://example.com/profile.png")
            .build();

        ReviewModel reviewModel = ReviewModel.builder()
            .id(reviewId)
            .contentId(contentId)
            .author(author)
            .text("리뷰 내용")
            .rating(BigDecimal.valueOf(4))
            .build();

        // when
        ReviewResponse response = reviewResponseMapper.toResponse(reviewModel);

        // then
        assertThat(response.id()).isEqualTo(reviewId);
        assertThat(response.contentId()).isEqualTo(contentId);
        assertThat(response.text()).isEqualTo("리뷰 내용");
        assertThat(response.rating()).isEqualByComparingTo(BigDecimal.valueOf(4));

        assertThat(response.author()).isNotNull();
        assertThat(response.author()).isInstanceOf(UserSummary.class);
        assertThat(response.author().userId()).isEqualTo(authorId);
        assertThat(response.author().name()).isEqualTo("홍길동");
        assertThat(response.author().profileImageUrl()).isEqualTo(
            "https://example.com/profile.png");
    }

    @Test
    @DisplayName("ReviewModel의 author가 null이면 ReviewResponse의 author도 null이다")
    void toResponse_withNullAuthor_mapsAuthorToNull() {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID contentId = UUID.randomUUID();

        ReviewModel reviewModel = ReviewModel.builder()
            .id(reviewId)
            .contentId(contentId)
            .author(null)
            .text("리뷰 내용")
            .rating(BigDecimal.valueOf(3))
            .build();

        // when
        ReviewResponse response = reviewResponseMapper.toResponse(reviewModel);

        // then
        assertThat(response.id()).isEqualTo(reviewId);
        assertThat(response.contentId()).isEqualTo(contentId);
        assertThat(response.text()).isEqualTo("리뷰 내용");
        assertThat(response.rating()).isEqualByComparingTo(BigDecimal.valueOf(3));
        assertThat(response.author()).isNull();
    }

    @Test
    @DisplayName("author의 name/profileImageUrl이 null이어도 UserSummary에 그대로 매핑된다")
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

        ReviewModel reviewModel = ReviewModel.builder()
            .id(reviewId)
            .contentId(contentId)
            .author(author)
            .text("리뷰 내용")
            .rating(BigDecimal.valueOf(5))
            .build();

        // when
        ReviewResponse response = reviewResponseMapper.toResponse(reviewModel);

        // then
        assertThat(response.author()).isNotNull();
        assertThat(response.author().userId()).isEqualTo(authorId);
        assertThat(response.author().name()).isNull();
        assertThat(response.author().profileImageUrl()).isNull();
    }
}
