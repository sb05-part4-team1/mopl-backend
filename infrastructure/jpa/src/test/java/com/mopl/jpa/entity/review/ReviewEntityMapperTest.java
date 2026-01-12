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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
        @DisplayName("유효한 Entity를 Model로 변환한다")
        void withValidEntity_mapsToModel() {
            // given
            UUID reviewId = UUID.randomUUID();
            String text = "리뷰 내용";
            BigDecimal rating = BigDecimal.valueOf(5);
            Instant now = Instant.now();

            // 협력 Entity 준비
            ContentEntity contentEntity = ContentEntity.builder().id(UUID.randomUUID()).build();
            UserEntity authorEntity = UserEntity.builder().id(UUID.randomUUID()).build();

            // 변환될 예상 Model 준비 (Mock 사용)
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

            // Mocking: 내부 매퍼 동작 정의
            given(contentEntityMapper.toModel(contentEntity)).willReturn(expectedContent);
            given(userEntityMapper.toModel(authorEntity)).willReturn(expectedAuthor);

            // when
            ReviewModel result = reviewEntityMapper.toModel(reviewEntity);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(reviewId);
            assertThat(result.getText()).isEqualTo(text);

            // 내부 매퍼가 반환한 객체가 잘 들어갔는지 확인
            assertThat(result.getContent()).isEqualTo(expectedContent);
            assertThat(result.getAuthor()).isEqualTo(expectedAuthor);
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
            // 1. Fixture 대신 직접 객체 생성
            UUID reviewId = UUID.randomUUID();
            ContentModel contentModel = mock(ContentModel.class); // Mock 객체 활용
            UserModel authorModel = mock(UserModel.class);       // Mock 객체 활용

            ReviewModel reviewModel = ReviewModel.builder()
                .id(reviewId)
                .content(contentModel)
                .author(authorModel)
                .text("테스트 리뷰")
                .rating(BigDecimal.valueOf(4.5))
                .createdAt(Instant.now())
                .build();

            // 2. 변환될 예상 Entity 준비
            ContentEntity expectedContentEntity = ContentEntity.builder().id(UUID.randomUUID())
                .build();
            UserEntity expectedUserEntity = UserEntity.builder().id(UUID.randomUUID()).build();

            // 3. Mocking: 위에서 만든 Mock Model이 들어오면 Entity 반환
            given(contentEntityMapper.toEntity(contentModel)).willReturn(expectedContentEntity);
            given(userEntityMapper.toEntity(authorModel)).willReturn(expectedUserEntity);

            // when
            ReviewEntity result = reviewEntityMapper.toEntity(reviewModel);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(reviewId);
            assertThat(result.getText()).isEqualTo("테스트 리뷰");

            // 내부 매퍼가 변환한 Entity가 잘 들어갔는지 확인
            assertThat(result.getContent()).isEqualTo(expectedContentEntity);
            assertThat(result.getAuthor()).isEqualTo(expectedUserEntity);

            // Verify
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
