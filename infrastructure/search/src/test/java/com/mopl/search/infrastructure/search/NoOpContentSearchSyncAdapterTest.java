package com.mopl.search.infrastructure.search;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.support.search.ContentSearchSyncPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NoOpContentSearchSyncAdapter 단위 테스트")
class NoOpContentSearchSyncAdapterTest {

    private NoOpContentSearchSyncAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new NoOpContentSearchSyncAdapter();
    }

    @Test
    @DisplayName("ContentSearchSyncPort 인터페이스 구현 확인")
    void implementsContentSearchSyncPort() {
        assertThat(adapter).isInstanceOf(ContentSearchSyncPort.class);
    }

    @Nested
    @DisplayName("upsert()")
    class UpsertTest {

        @Test
        @DisplayName("아무 동작 없이 정상 완료")
        void withModel_doesNothing() {
            // given
            ContentModel model = ContentModel.builder()
                .id(UUID.randomUUID())
                .type(ContentModel.ContentType.movie)
                .title("인셉션")
                .build();

            // when & then
            assertThatCode(() -> adapter.upsert(model))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("null 입력도 정상 처리")
        void withNull_doesNothing() {
            // when & then
            assertThatCode(() -> adapter.upsert(null))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("upsertAll()")
    class UpsertAllTest {

        @Test
        @DisplayName("아무 동작 없이 정상 완료")
        void withModels_doesNothing() {
            // given
            List<ContentModel> models = List.of(
                ContentModel.builder()
                    .id(UUID.randomUUID())
                    .type(ContentModel.ContentType.movie)
                    .title("인셉션")
                    .build()
            );

            // when & then
            assertThatCode(() -> adapter.upsertAll(models))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("null 입력도 정상 처리")
        void withNull_doesNothing() {
            // when & then
            assertThatCode(() -> adapter.upsertAll(null))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTest {

        @Test
        @DisplayName("아무 동작 없이 정상 완료")
        void withContentId_doesNothing() {
            // given
            UUID contentId = UUID.randomUUID();

            // when & then
            assertThatCode(() -> adapter.delete(contentId))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("null 입력도 정상 처리")
        void withNull_doesNothing() {
            // when & then
            assertThatCode(() -> adapter.delete(null))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("deleteAll()")
    class DeleteAllTest {

        @Test
        @DisplayName("아무 동작 없이 정상 완료")
        void withContentIds_doesNothing() {
            // given
            List<UUID> contentIds = List.of(UUID.randomUUID(), UUID.randomUUID());

            // when & then
            assertThatCode(() -> adapter.deleteAll(contentIds))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("null 입력도 정상 처리")
        void withNull_doesNothing() {
            // when & then
            assertThatCode(() -> adapter.deleteAll(null))
                .doesNotThrowAnyException();
        }
    }
}
