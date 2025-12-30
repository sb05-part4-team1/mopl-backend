package com.mopl.api.interfaces.api.content;

import com.mopl.domain.model.content.ContentModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ContentResponseMapper 단위 테스트")
class ContentResponseMapperTest {

    private final ContentResponseMapper mapper = new ContentResponseMapper();

    @Nested
    @DisplayName("toResponse()")
    class ToResponseTest {

        @Test
        @DisplayName("ContentModel과 통계 데이터를 ContentResponse로 변환한다")
        void withContentModelAndStats_returnsContentResponse() {
            // given
            UUID id = UUID.randomUUID();
            ContentModel model = ContentModel.builder()
                .id(id)
                .type("영화")
                .title("인셉션")
                .description("꿈속의 꿈")
                .thumbnailUrl("https://mopl.com/inception.png")
                .tags(List.of("SF", "액션"))
                .build();

            Double averageRating = 4.5;
            Integer reviewCount = 100;
            Long watcherCount = 1500L;

            // when
            ContentResponse result = mapper.toResponse(model, averageRating, reviewCount,
                watcherCount);

            // then
            assertThat(result.id()).isEqualTo(id);
            assertThat(result.type()).isEqualTo("영화");
            assertThat(result.title()).isEqualTo("인셉션");
            assertThat(result.description()).isEqualTo("꿈속의 꿈");
            assertThat(result.thumbnailUrl()).isEqualTo("https://mopl.com/inception.png");
            assertThat(result.tags()).containsExactly("SF", "액션");
            assertThat(result.averageRating()).isEqualTo(4.5);
            assertThat(result.reviewCount()).isEqualTo(100);
            assertThat(result.watcherCount()).isEqualTo(1500L);
        }

        @Test
        @DisplayName("태그 리스트가 비어있어도 정상적으로 변환된다")
        void withEmptyTags_returnsContentResponse() {
            // given
            ContentModel model = ContentModel.builder()
                .id(UUID.randomUUID())
                .tags(List.of())
                .build();

            // when
            ContentResponse result = mapper.toResponse(model);

            // then
            assertThat(result.tags()).isEmpty();
        }

        @Test
        @DisplayName("모델에 정의되지 않은 내부 데이터(예: BaseUpdatableModel 필드)는 응답에 포함되지 않는다")
        void internalFieldsAreNotExposed() {
            // given
            ContentModel model = ContentModel.builder()
                .id(UUID.randomUUID())
                .build();

            // when
            ContentResponse result = mapper.toResponse(model);

            // then
            assertThat(result.toString()).doesNotContain("createdAt");
            assertThat(result.toString()).doesNotContain("updatedAt");
            assertThat(result.toString()).doesNotContain("deletedAt");
        }

        @Test
        @DisplayName("기본 toResponse는 통계 값을 0으로 설정한다")
        void defaultToResponse_setsDefaultStats() {
            // given
            ContentModel model = ContentModel.builder()
                .id(UUID.randomUUID())
                .tags(List.of("SF"))
                .build();

            // when
            ContentResponse result = mapper.toResponse(model);

            // then
            assertThat(result.averageRating()).isEqualTo(0.0);
            assertThat(result.reviewCount()).isEqualTo(0);
            assertThat(result.watcherCount()).isEqualTo(0L);
        }
    }
}
