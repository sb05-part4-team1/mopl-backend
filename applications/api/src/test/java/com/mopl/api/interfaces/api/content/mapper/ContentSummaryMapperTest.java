package com.mopl.api.interfaces.api.content.mapper;

import com.mopl.api.interfaces.api.content.dto.ContentSummary;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContentSummaryMapper 단위 테스트")
class ContentSummaryMapperTest {

    @Mock
    private StorageProvider storageProvider;

    @InjectMocks
    private ContentSummaryMapper mapper;

    @Nested
    @DisplayName("toSummary()")
    class ToSummaryTest {

        @Test
        @DisplayName("ContentModel을 ContentSummary로 변환")
        void withContentModel_returnsContentSummary() {
            // given
            ContentModel contentModel = ContentModelFixture.create();
            List<String> tags = List.of("SF", "Action");
            String expectedUrl = "https://cdn.example.com/" + contentModel.getThumbnailPath();

            given(storageProvider.getUrl(contentModel.getThumbnailPath())).willReturn(expectedUrl);

            // when
            ContentSummary result = mapper.toSummary(contentModel, tags);

            // then
            assertThat(result.id()).isEqualTo(contentModel.getId());
            assertThat(result.type()).isEqualTo(contentModel.getType());
            assertThat(result.title()).isEqualTo(contentModel.getTitle());
            assertThat(result.description()).isEqualTo(contentModel.getDescription());
            assertThat(result.thumbnailUrl()).isEqualTo(expectedUrl);
            assertThat(result.tags()).isEqualTo(tags);
            assertThat(result.averageRating()).isEqualTo(contentModel.getAverageRating());
            assertThat(result.reviewCount()).isEqualTo(contentModel.getReviewCount());
        }

        @Test
        @DisplayName("빈 태그 목록으로 변환")
        void withEmptyTags_returnsContentSummaryWithEmptyTags() {
            // given
            ContentModel contentModel = ContentModelFixture.create();
            List<String> tags = List.of();
            String expectedUrl = "https://cdn.example.com/thumbnail.jpg";

            given(storageProvider.getUrl(contentModel.getThumbnailPath())).willReturn(expectedUrl);

            // when
            ContentSummary result = mapper.toSummary(contentModel, tags);

            // then
            assertThat(result.tags()).isEmpty();
        }

        @Test
        @DisplayName("null tags로 변환")
        void withNullTags_returnsContentSummaryWithNullTags() {
            // given
            ContentModel contentModel = ContentModelFixture.create();
            String expectedUrl = "https://cdn.example.com/thumbnail.jpg";

            given(storageProvider.getUrl(contentModel.getThumbnailPath())).willReturn(expectedUrl);

            // when
            ContentSummary result = mapper.toSummary(contentModel, null);

            // then
            assertThat(result.tags()).isNull();
        }
    }

    @Nested
    @DisplayName("toSummaries()")
    class ToSummariesTest {

        @Test
        @DisplayName("ContentModel 컬렉션을 ContentSummary 리스트로 변환")
        void withContentModels_returnsContentSummaries() {
            // given
            ContentModel content1 = ContentModelFixture.create();
            ContentModel content2 = ContentModelFixture.create();
            List<ContentModel> models = List.of(content1, content2);

            Map<UUID, List<String>> tagsByContentId = Map.of(
                content1.getId(), List.of("SF", "Action"),
                content2.getId(), List.of("Drama")
            );

            given(storageProvider.getUrl(content1.getThumbnailPath()))
                .willReturn("https://cdn.example.com/" + content1.getThumbnailPath());
            given(storageProvider.getUrl(content2.getThumbnailPath()))
                .willReturn("https://cdn.example.com/" + content2.getThumbnailPath());

            // when
            List<ContentSummary> result = mapper.toSummaries(models, tagsByContentId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).id()).isEqualTo(content1.getId());
            assertThat(result.get(0).tags()).containsExactly("SF", "Action");
            assertThat(result.get(1).id()).isEqualTo(content2.getId());
            assertThat(result.get(1).tags()).containsExactly("Drama");
        }

        @Test
        @DisplayName("null 컬렉션이면 빈 리스트 반환")
        void withNullModels_returnsEmptyList() {
            // given
            Map<UUID, List<String>> tagsByContentId = Map.of();

            // when
            List<ContentSummary> result = mapper.toSummaries(null, tagsByContentId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("빈 컬렉션이면 빈 리스트 반환")
        void withEmptyModels_returnsEmptyList() {
            // given
            List<ContentModel> models = Collections.emptyList();
            Map<UUID, List<String>> tagsByContentId = Map.of();

            // when
            List<ContentSummary> result = mapper.toSummaries(models, tagsByContentId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("태그 맵에 없는 콘텐츠는 빈 태그 목록으로 변환")
        void withMissingTagsInMap_returnsContentSummaryWithEmptyTags() {
            // given
            ContentModel contentModel = ContentModelFixture.create();
            List<ContentModel> models = List.of(contentModel);
            Map<UUID, List<String>> tagsByContentId = Map.of(); // 태그 없음

            given(storageProvider.getUrl(contentModel.getThumbnailPath()))
                .willReturn("https://cdn.example.com/thumbnail.jpg");

            // when
            List<ContentSummary> result = mapper.toSummaries(models, tagsByContentId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().tags()).isEmpty();
        }
    }
}
