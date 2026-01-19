package com.mopl.jpa.entity.review;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.jpa.entity.content.ContentEntity;
import com.mopl.jpa.entity.content.ContentEntityMapper;
import com.mopl.jpa.entity.user.UserEntity;
import com.mopl.jpa.entity.user.UserEntityMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewEntityMapper 단위 테스트")
class ReviewEntityMapperTest {

    @InjectMocks
    private ReviewEntityMapper reviewEntityMapper;

    @Mock
    private UserEntityMapper userEntityMapper;

    @Mock
    private ContentEntityMapper contentEntityMapper;

    @Nested
    @DisplayName("toModel()")
    class ToModelTest {

        @Test
        @DisplayName("ReviewEntity가 null이면 null을 반환한다")
        void withNullEntity_returnsNull() {
            ReviewModel result = reviewEntityMapper.toModel(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("유효한 Entity를 ID만 포함한 Model로 변환한다")
        void withValidEntity_mapsToModelWithIdsOnly() {
            // given
            UUID reviewId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();
            UUID authorId = UUID.randomUUID();
            String text = "리뷰 내용";
            double rating = 4.5;
            Instant now = Instant.now();

            ContentEntity contentEntity = ContentEntity.builder().id(contentId).build();
            UserEntity authorEntity = UserEntity.builder().id(authorId).build();

            ReviewEntity reviewEntity = ReviewEntity.builder()
                .id(reviewId)
                .content(contentEntity)
                .author(authorEntity)
                .text(text)
                .rating(rating)
                .createdAt(now)
                .updatedAt(now)
                .build();

            // when
            ReviewModel result = reviewEntityMapper.toModel(reviewEntity);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(reviewId);
            assertThat(result.getText()).isEqualTo(text);
            assertThat(result.getRating()).isEqualTo(rating);
            assertThat(result.getContent().getId()).isEqualTo(contentId);
            assertThat(result.getAuthor().getId()).isEqualTo(authorId);

            verifyNoInteractions(contentEntityMapper);
            verifyNoInteractions(userEntityMapper);
        }

        @Test
        @DisplayName("Entity의 연관관계가 null이면 Model의 필드도 null이다")
        void withNullRelations_mapsNulls() {
            // given
            ReviewEntity reviewEntity = ReviewEntity.builder()
                .id(UUID.randomUUID())
                .content(null)
                .author(null)
                .build();

            // when
            ReviewModel result = reviewEntityMapper.toModel(reviewEntity);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isNull();
            assertThat(result.getAuthor()).isNull();
        }
    }

    @Nested
    @DisplayName("toModelWithContent()")
    class ToModelWithContentTest {

        @Test
        @DisplayName("ReviewEntity가 null이면 null을 반환한다")
        void withNullEntity_returnsNull() {
            ReviewModel result = reviewEntityMapper.toModelWithContent(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Content는 전체 모델, Author는 ID만 포함한다")
        void withValidEntity_mapsContentFullAndAuthorIdOnly() {
            // given
            UUID reviewId = UUID.randomUUID();
            UUID authorId = UUID.randomUUID();

            ContentEntity contentEntity = ContentEntity.builder().id(UUID.randomUUID()).build();
            UserEntity authorEntity = UserEntity.builder().id(authorId).build();
            ContentModel expectedContent = mock(ContentModel.class);

            ReviewEntity reviewEntity = ReviewEntity.builder()
                .id(reviewId)
                .content(contentEntity)
                .author(authorEntity)
                .text("리뷰")
                .rating(4.0)
                .build();

            given(contentEntityMapper.toModel(contentEntity)).willReturn(expectedContent);

            // when
            ReviewModel result = reviewEntityMapper.toModelWithContent(reviewEntity);

            // then
            assertThat(result.getContent()).isEqualTo(expectedContent);
            assertThat(result.getAuthor().getId()).isEqualTo(authorId);

            verify(contentEntityMapper).toModel(contentEntity);
            verifyNoInteractions(userEntityMapper);
        }
    }

    @Nested
    @DisplayName("toModelWithAuthor()")
    class ToModelWithAuthorTest {

        @Test
        @DisplayName("ReviewEntity가 null이면 null을 반환한다")
        void withNullEntity_returnsNull() {
            ReviewModel result = reviewEntityMapper.toModelWithAuthor(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Content는 ID만, Author는 전체 모델을 포함한다")
        void withValidEntity_mapsContentIdOnlyAndAuthorFull() {
            // given
            UUID reviewId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();

            ContentEntity contentEntity = ContentEntity.builder().id(contentId).build();
            UserEntity authorEntity = UserEntity.builder().id(UUID.randomUUID()).build();
            UserModel expectedAuthor = mock(UserModel.class);

            ReviewEntity reviewEntity = ReviewEntity.builder()
                .id(reviewId)
                .content(contentEntity)
                .author(authorEntity)
                .text("리뷰")
                .rating(4.0)
                .build();

            given(userEntityMapper.toModel(authorEntity)).willReturn(expectedAuthor);

            // when
            ReviewModel result = reviewEntityMapper.toModelWithAuthor(reviewEntity);

            // then
            assertThat(result.getContent().getId()).isEqualTo(contentId);
            assertThat(result.getAuthor()).isEqualTo(expectedAuthor);

            verifyNoInteractions(contentEntityMapper);
            verify(userEntityMapper).toModel(authorEntity);
        }
    }

    @Nested
    @DisplayName("toModelWithContentAndAuthor()")
    class ToModelWithContentAndAuthorTest {

        @Test
        @DisplayName("ReviewEntity가 null이면 null을 반환한다")
        void withNullEntity_returnsNull() {
            ReviewModel result = reviewEntityMapper.toModelWithContentAndAuthor(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Content와 Author 모두 전체 모델을 포함한다")
        void withValidEntity_mapsContentAndAuthorFull() {
            // given
            UUID reviewId = UUID.randomUUID();
            String text = "리뷰 내용";
            double rating = 4.5;
            Instant now = Instant.now();

            ContentEntity contentEntity = ContentEntity.builder().id(UUID.randomUUID()).build();
            UserEntity authorEntity = UserEntity.builder().id(UUID.randomUUID()).build();
            ContentModel expectedContent = mock(ContentModel.class);
            UserModel expectedAuthor = mock(UserModel.class);

            ReviewEntity reviewEntity = ReviewEntity.builder()
                .id(reviewId)
                .content(contentEntity)
                .author(authorEntity)
                .text(text)
                .rating(rating)
                .createdAt(now)
                .updatedAt(now)
                .build();

            given(contentEntityMapper.toModel(contentEntity)).willReturn(expectedContent);
            given(userEntityMapper.toModel(authorEntity)).willReturn(expectedAuthor);

            // when
            ReviewModel result = reviewEntityMapper.toModelWithContentAndAuthor(reviewEntity);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(reviewId);
            assertThat(result.getText()).isEqualTo(text);
            assertThat(result.getContent()).isEqualTo(expectedContent);
            assertThat(result.getAuthor()).isEqualTo(expectedAuthor);

            verify(contentEntityMapper).toModel(contentEntity);
            verify(userEntityMapper).toModel(authorEntity);
        }

        @Test
        @DisplayName("Entity의 연관관계가 null이면 Model의 필드도 null이다")
        void withNullRelations_mapsNulls() {
            // given
            ReviewEntity reviewEntity = ReviewEntity.builder()
                .id(UUID.randomUUID())
                .content(null)
                .author(null)
                .build();

            // when
            ReviewModel result = reviewEntityMapper.toModelWithContentAndAuthor(reviewEntity);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isNull();
            assertThat(result.getAuthor()).isNull();
        }
    }

    @Nested
    @DisplayName("toEntity()")
    class ToEntityTest {

        @Test
        @DisplayName("ReviewModel이 null이면 null을 반환한다")
        void withNullModel_returnsNull() {
            ReviewEntity result = reviewEntityMapper.toEntity(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("유효한 Model을 Entity로 변환한다")
        void withValidModel_mapsToEntity() {
            // given
            UUID reviewId = UUID.randomUUID();
            ContentModel contentModel = mock(ContentModel.class);
            UserModel authorModel = mock(UserModel.class);

            ReviewModel reviewModel = ReviewModel.builder()
                .id(reviewId)
                .content(contentModel)
                .author(authorModel)
                .text("테스트 리뷰")
                .rating(4.5)
                .createdAt(Instant.now())
                .build();

            ContentEntity expectedContentEntity = ContentEntity.builder()
                .id(UUID.randomUUID())
                .build();
            UserEntity expectedUserEntity = UserEntity.builder()
                .id(UUID.randomUUID())
                .build();

            given(contentEntityMapper.toEntity(contentModel)).willReturn(expectedContentEntity);
            given(userEntityMapper.toEntity(authorModel)).willReturn(expectedUserEntity);

            // when
            ReviewEntity result = reviewEntityMapper.toEntity(reviewModel);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(reviewId);
            assertThat(result.getText()).isEqualTo("테스트 리뷰");
            assertThat(result.getContent()).isEqualTo(expectedContentEntity);
            assertThat(result.getAuthor()).isEqualTo(expectedUserEntity);

            verify(contentEntityMapper).toEntity(contentModel);
            verify(userEntityMapper).toEntity(authorModel);
        }

        @Test
        @DisplayName("Model의 연관관계가 null이면 Entity의 필드도 null이다")
        void withNullRelations_mapsNulls() {
            // given
            ReviewModel reviewModel = ReviewModel.builder()
                .id(UUID.randomUUID())
                .content(null)
                .author(null)
                .build();

            // when
            ReviewEntity result = reviewEntityMapper.toEntity(reviewModel);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isNull();
            assertThat(result.getAuthor()).isNull();
        }
    }
}
