package com.mopl.jpa.entity.content;

import com.mopl.domain.model.content.ContentModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ContentEntityMapper 단위 테스트")
class ContentEntityMapperTest {

    private final ContentEntityMapper mapper = new ContentEntityMapper();

    @Nested
    @DisplayName("toModel()")
    class ToModelTest {

        @Test
        @DisplayName("ContentEntity를 ContentModel로 변환")
        void withContentEntity_returnsContentModel() {
            // given
            UUID id = UUID.randomUUID();
            Instant now = Instant.now();

            ContentEntity entity = ContentEntity.builder()
                .id(id)
                .type(ContentModel.ContentType.movie)
                .title("인셉션")
                .description("꿈속의 꿈")
                .thumbnailUrl("https://mopl.com/inception.png")
                .createdAt(now)
                .updatedAt(now)
                .deletedAt(null)
                .build();

            // when
            ContentModel result = mapper.toModel(entity);

            // then
            assertThat(result.getId()).isEqualTo(id);
            assertThat(result.getType()).isEqualTo(ContentModel.ContentType.movie);
            assertThat(result.getTitle()).isEqualTo("인셉션");
            assertThat(result.getDescription()).isEqualTo("꿈속의 꿈");
            assertThat(result.getThumbnailUrl()).isEqualTo("https://mopl.com/inception.png");
            assertThat(result.getCreatedAt()).isEqualTo(now);
            assertThat(result.getUpdatedAt()).isEqualTo(now);
            assertThat(result.getDeletedAt()).isNull();
        }

        @Test
        @DisplayName("null 입력 시 null 반환")
        void withNull_returnsNull() {
            // when
            ContentModel result = mapper.toModel(null);

            // then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("toEntity()")
    class ToEntityTest {

        @Test
        @DisplayName("ContentModel을 ContentEntity로 변환")
        void withContentModel_returnsContentEntity() {
            // given
            UUID id = UUID.randomUUID();
            Instant now = Instant.now();

            ContentModel model = ContentModel.builder()
                .id(id)
                .type(ContentModel.ContentType.movie)
                .title("인셉션")
                .description("꿈속의 꿈")
                .thumbnailUrl("https://mopl.com/inception.png")
                .createdAt(now)
                .updatedAt(now)
                .build();

            // when
            ContentEntity result = mapper.toEntity(model);

            // then
            assertThat(result.getId()).isEqualTo(id);
            assertThat(result.getType()).isEqualTo(ContentModel.ContentType.movie);
            assertThat(result.getTitle()).isEqualTo("인셉션");
            assertThat(result.getDescription()).isEqualTo("꿈속의 꿈");
            assertThat(result.getThumbnailUrl()).isEqualTo("https://mopl.com/inception.png");
            assertThat(result.getCreatedAt()).isEqualTo(now);
            assertThat(result.getUpdatedAt()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("데이터 정합성 테스트")
    class DataIntegrityTest {

        @Test
        @DisplayName("양방향 변환 시 핵심 데이터 유지 확인")
        void roundTrip_preservesData() {
            // given
            UUID id = UUID.randomUUID();
            ContentModel originalModel = ContentModel.builder()
                .id(id)
                .type(ContentModel.ContentType.movie)
                .title("인셉션")
                .thumbnailUrl("https://mopl.com/inception.png")
                .build();

            // when
            ContentEntity entity = mapper.toEntity(originalModel);
            ContentModel resultModel = mapper.toModel(entity);

            // then
            assertThat(resultModel.getId()).isEqualTo(originalModel.getId());
            assertThat(resultModel.getTitle()).isEqualTo(originalModel.getTitle());
            assertThat(resultModel.getType()).isEqualTo(originalModel.getType());
        }
    }
}
