package com.mopl.search.infrastructure.search;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.search.content.service.ContentIndexService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@DisplayName("ElasticsearchContentSearchSyncAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class ElasticsearchContentSearchSyncAdapterTest {

    @Mock
    private ContentIndexService indexService;

    private ElasticsearchContentSearchSyncAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ElasticsearchContentSearchSyncAdapter(indexService);
    }

    @Nested
    @DisplayName("upsert()")
    class UpsertTest {

        @Test
        @DisplayName("ContentIndexService.upsert() 위임")
        void withModel_delegatesToIndexService() {
            // given
            ContentModel model = ContentModel.builder()
                .id(UUID.randomUUID())
                .type(ContentModel.ContentType.movie)
                .title("인셉션")
                .build();

            // when
            adapter.upsert(model);

            // then
            verify(indexService).upsert(model);
        }
    }

    @Nested
    @DisplayName("upsertAll()")
    class UpsertAllTest {

        @Test
        @DisplayName("ContentIndexService.upsertAll() 위임")
        void withModels_delegatesToIndexService() {
            // given
            List<ContentModel> models = List.of(
                ContentModel.builder()
                    .id(UUID.randomUUID())
                    .type(ContentModel.ContentType.movie)
                    .title("인셉션")
                    .build(),
                ContentModel.builder()
                    .id(UUID.randomUUID())
                    .type(ContentModel.ContentType.tvSeries)
                    .title("브레이킹 배드")
                    .build()
            );

            // when
            adapter.upsertAll(models);

            // then
            verify(indexService).upsertAll(models);
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTest {

        @Test
        @DisplayName("ContentIndexService.delete() 위임")
        void withContentId_delegatesToIndexService() {
            // given
            UUID contentId = UUID.randomUUID();

            // when
            adapter.delete(contentId);

            // then
            verify(indexService).delete(contentId);
        }
    }

    @Nested
    @DisplayName("deleteAll()")
    class DeleteAllTest {

        @Test
        @DisplayName("ContentIndexService.deleteAll() 위임")
        void withContentIds_delegatesToIndexService() {
            // given
            List<UUID> contentIds = List.of(UUID.randomUUID(), UUID.randomUUID());

            // when
            adapter.deleteAll(contentIds);

            // then
            verify(indexService).deleteAll(contentIds);
        }
    }
}
