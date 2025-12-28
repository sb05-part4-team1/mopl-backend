package com.mopl.jpa.entity.review;

import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.jpa.entity.content.ContentEntity;
import com.mopl.jpa.entity.user.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ReviewEntityMapper 단위 테스트")
class ReviewEntityMapperTest {

    private final ReviewEntityMapper reviewEntityMapper = new ReviewEntityMapper();

    @Nested
    @DisplayName("toModel()")
    class ToModelTest {

        @Test
        @DisplayName("reviewEntity가 null이면 null을 반환한다")
        void withNullEntity_returnsNull() {
            // given
            ReviewEntity reviewEntity = null;

            // when
            ReviewModel result = reviewEntityMapper.toModel(reviewEntity);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("유효한 ReviewEntity를 ReviewModel로 변환한다 (contentId/authorId 포함)")
        void withValidEntity_mapsToModel() {
            // given
            UUID reviewId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();
            UUID authorId = UUID.randomUUID();

            Instant createdAt = Instant.now();
            Instant updatedAt = Instant.now();
            Instant deletedAt = Instant.now();

            String text = "리뷰 텍스트";
            BigDecimal rating = BigDecimal.valueOf(4);

            ContentEntity contentEntity = ContentEntity.builder()
                .id(contentId)
                .build();

            UserEntity authorEntity = UserEntity.builder()
                .id(authorId)
                .build();

            ReviewEntity reviewEntity = ReviewEntity.builder()
                .id(reviewId)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .content(contentEntity)
                .author(authorEntity)
                .text(text)
                .rating(rating)
                .build();

            // when
            ReviewModel result = reviewEntityMapper.toModel(reviewEntity);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(reviewId);
            assertThat(result.getCreatedAt()).isEqualTo(createdAt);
            assertThat(result.getUpdatedAt()).isEqualTo(updatedAt);
            assertThat(result.getDeletedAt()).isEqualTo(deletedAt);

            assertThat(result.getContentId()).isEqualTo(contentId);

            assertThat(result.getAuthor()).isNotNull();
            assertThat(result.getAuthor().getId()).isEqualTo(authorId);

            assertThat(result.getText()).isEqualTo(text);
            assertThat(result.getRating()).isEqualTo(rating);
        }

        @Test
        @DisplayName("author가 null이면 author는 null로 변환된다")
        void withNullAuthor_mapsNullAuthor() {
            // given
            UUID reviewId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();

            ContentEntity contentEntity = ContentEntity.builder()
                .id(contentId)
                .build();

            ReviewEntity reviewEntity = ReviewEntity.builder()
                .id(reviewId)
                .content(contentEntity)
                .author(null)
                .text("리뷰")
                .rating(BigDecimal.valueOf(3))
                .build();

            // when
            ReviewModel result = reviewEntityMapper.toModel(reviewEntity);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getAuthor()).isNull();
            assertThat(result.getContentId()).isEqualTo(contentId);
        }
    }

    @Nested
    @DisplayName("toEntity()")
    class ToEntityTest {

        @Test
        @DisplayName("reviewModel이 null이면 null을 반환한다")
        void withNullModel_returnsNull() {
            // given
            ReviewModel reviewModel = null;

            // when
            ReviewEntity result = reviewEntityMapper.toEntity(reviewModel);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("유효한 ReviewModel을 ReviewEntity로 변환한다 (content/author는 id만 세팅)")
        void withValidModel_mapsToEntity() {
            // given
            UUID reviewId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();
            UUID authorId = UUID.randomUUID();

            Instant createdAt = Instant.now();
            Instant updatedAt = Instant.now();
            Instant deletedAt = Instant.now();

            String text = "리뷰 내용";
            BigDecimal rating = BigDecimal.valueOf(5);

            UserModel authorModel = UserModel.builder()
                .id(authorId)
                .build();

            ReviewModel reviewModel = ReviewModel.builder()
                .id(reviewId)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .contentId(contentId)
                .author(authorModel)
                .text(text)
                .rating(rating)
                .build();

            // when
            ReviewEntity result = reviewEntityMapper.toEntity(reviewModel);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(reviewId);
            assertThat(result.getCreatedAt()).isEqualTo(createdAt);
            assertThat(result.getUpdatedAt()).isEqualTo(updatedAt);
            assertThat(result.getDeletedAt()).isEqualTo(deletedAt);

            assertThat(result.getContent()).isNotNull();
            assertThat(result.getContent().getId()).isEqualTo(contentId);

            assertThat(result.getAuthor()).isNotNull();
            assertThat(result.getAuthor().getId()).isEqualTo(authorId);

            assertThat(result.getText()).isEqualTo(text);
            assertThat(result.getRating()).isEqualTo(rating);
        }

        @Test
        @DisplayName("author가 null이면 author는 null로 변환된다")
        void withNullAuthor_mapsNullAuthor() {
            // given
            UUID reviewId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();

            ReviewModel reviewModel = ReviewModel.builder()
                .id(reviewId)
                .contentId(contentId)
                .author(null)
                .text("리뷰")
                .rating(BigDecimal.valueOf(2))
                .build();

            // when
            ReviewEntity result = reviewEntityMapper.toEntity(reviewModel);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getAuthor()).isNull();
            assertThat(result.getContent()).isNotNull();
            assertThat(result.getContent().getId()).isEqualTo(contentId);
        }

        @Test
        @DisplayName("contentId가 null이면 content는 null로 변환된다")
        void withNullContentId_mapsNullContent() {
            // given
            UUID reviewId = UUID.randomUUID();
            UUID authorId = UUID.randomUUID();

            UserModel authorModel = UserModel.builder()
                .id(authorId)
                .build();

            ReviewModel reviewModel = ReviewModel.builder()
                .id(reviewId)
                .contentId(null)
                .author(authorModel)
                .text("리뷰")
                .rating(BigDecimal.valueOf(1))
                .build();

            // when
            ReviewEntity result = reviewEntityMapper.toEntity(reviewModel);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isNull();
            assertThat(result.getAuthor()).isNotNull();
            assertThat(result.getAuthor().getId()).isEqualTo(authorId);
        }
    }
}
