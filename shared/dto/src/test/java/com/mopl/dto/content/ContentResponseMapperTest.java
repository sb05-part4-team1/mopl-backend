package com.mopl.dto.content;

import com.mopl.domain.fixture.ContentModelFixture;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.storage.provider.StorageProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContentResponseMapper 단위 테스트")
class ContentResponseMapperTest {

    @Mock
    private StorageProvider storageProvider;

    @InjectMocks
    private ContentResponseMapper mapper;

    @Nested
    @DisplayName("toResponse()")
    class ToResponseTest {

        @Test
        @DisplayName("ContentModel을 ContentResponse로 변환")
        void withContentModel_returnsContentResponse() {
            // given
            ContentModel contentModel = ContentModelFixture.create();
            List<String> tags = List.of("SF", "Action");
            long watcherCount = 100L;
            String expectedUrl = "https://cdn.example.com/" + contentModel.getThumbnailPath();

            given(storageProvider.getUrl(contentModel.getThumbnailPath())).willReturn(expectedUrl);

            // when
            ContentResponse result = mapper.toResponse(contentModel, tags, watcherCount);

            // then
            assertThat(result.id()).isEqualTo(contentModel.getId());
            assertThat(result.type()).isEqualTo(contentModel.getType());
            assertThat(result.title()).isEqualTo(contentModel.getTitle());
            assertThat(result.description()).isEqualTo(contentModel.getDescription());
            assertThat(result.thumbnailUrl()).isEqualTo(expectedUrl);
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
            List<String> tags = List.of();
            long watcherCount = 0L;
            String expectedUrl = "https://cdn.example.com/thumbnail.jpg";

            given(storageProvider.getUrl(contentModel.getThumbnailPath())).willReturn(expectedUrl);

            // when
            ContentResponse result = mapper.toResponse(contentModel, tags, watcherCount);

            // then
            assertThat(result.tags()).isEmpty();
            assertThat(result.watcherCount()).isZero();
        }

        @Test
        @DisplayName("null thumbnailPath면 null thumbnailUrl 반환")
        void withNullThumbnailPath_returnsContentResponseWithNullThumbnailUrl() {
            // given
            ContentModel contentModel = ContentModelFixture.create();
            List<String> tags = List.of("Drama");
            long watcherCount = 50L;

            given(storageProvider.getUrl(contentModel.getThumbnailPath())).willReturn(null);

            // when
            ContentResponse result = mapper.toResponse(contentModel, tags, watcherCount);

            // then
            assertThat(result.thumbnailUrl()).isNull();
        }

        @Test
        @DisplayName("null tags로 변환")
        void withNullTags_returnsContentResponseWithNullTags() {
            // given
            ContentModel contentModel = ContentModelFixture.create();
            long watcherCount = 50L;
            String expectedUrl = "https://cdn.example.com/thumbnail.jpg";

            given(storageProvider.getUrl(contentModel.getThumbnailPath())).willReturn(expectedUrl);

            // when
            ContentResponse result = mapper.toResponse(contentModel, null, watcherCount);

            // then
            assertThat(result.tags()).isNull();
        }
    }
}
