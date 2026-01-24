package com.mopl.api.interfaces.api.content;

import com.mopl.api.interfaces.api.content.dto.ContentResponse;
import com.mopl.api.interfaces.api.content.mapper.ContentResponseMapper;
import com.mopl.domain.fixture.ContentModelFixture;
import com.mopl.domain.model.content.ContentModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ContentResponseMapper 단위 테스트")
class ContentResponseMapperTest {

    private final ContentResponseMapper mapper = new ContentResponseMapper();

    @Nested
    @DisplayName("toResponse()")
    class ToResponseTest {

        @Test
        @DisplayName("ContentModel을 ContentResponse로 변환")
        void withContentModel_returnsContentResponse() {
            // given
            ContentModel contentModel = ContentModelFixture.create();
            String thumbnailUrl = "https://cdn.example.com/thumbnail.jpg";
            List<String> tags = List.of("SF", "Action");
            long watcherCount = 100L;

            // when
            ContentResponse result = mapper.toResponse(contentModel, thumbnailUrl, tags, watcherCount);

            // then
            assertThat(result.id()).isEqualTo(contentModel.getId());
            assertThat(result.type()).isEqualTo(contentModel.getType());
            assertThat(result.title()).isEqualTo(contentModel.getTitle());
            assertThat(result.description()).isEqualTo(contentModel.getDescription());
            assertThat(result.thumbnailUrl()).isEqualTo(thumbnailUrl);
            assertThat(result.tags()).isEqualTo(tags);
            assertThat(result.averageRating()).isEqualTo(contentModel.getAverageRating());
            assertThat(result.reviewCount()).isEqualTo(contentModel.getReviewCount());
            assertThat(result.watcherCount()).isEqualTo(watcherCount);
        }

        @Test
        @DisplayName("빈 태그 목록으로 변환")
        void withEmptyTags_returnsContentResponseWithEmptyTags() {
            // given
            ContentModel contentModel = ContentModelFixture.create();
            String thumbnailUrl = "https://cdn.example.com/thumbnail.jpg";
            List<String> tags = List.of();
            long watcherCount = 0L;

            // when
            ContentResponse result = mapper.toResponse(contentModel, thumbnailUrl, tags, watcherCount);

            // then
            assertThat(result.tags()).isEmpty();
            assertThat(result.watcherCount()).isZero();
        }

        @Test
        @DisplayName("null thumbnailUrl로 변환")
        void withNullThumbnailUrl_returnsContentResponseWithNullThumbnailUrl() {
            // given
            ContentModel contentModel = ContentModelFixture.create();
            List<String> tags = List.of("Drama");
            long watcherCount = 50L;

            // when
            ContentResponse result = mapper.toResponse(contentModel, null, tags, watcherCount);

            // then
            assertThat(result.thumbnailUrl()).isNull();
        }

        @Test
        @DisplayName("null tags로 변환")
        void withNullTags_returnsContentResponseWithNullTags() {
            // given
            ContentModel contentModel = ContentModelFixture.create();
            String thumbnailUrl = "https://cdn.example.com/thumbnail.jpg";
            long watcherCount = 50L;

            // when
            ContentResponse result = mapper.toResponse(contentModel, thumbnailUrl, null, watcherCount);

            // then
            assertThat(result.tags()).isNull();
        }
    }
}
