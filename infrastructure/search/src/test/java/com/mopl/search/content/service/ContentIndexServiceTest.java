package com.mopl.search.content.service;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.search.content.mapper.ContentDocumentMapper;
import com.mopl.search.content.repository.ContentDocumentRepository;
import com.mopl.search.document.ContentDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@DisplayName("ContentIndexService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class ContentIndexServiceTest {

    @Mock
    private ContentDocumentRepository repository;

    @Mock
    private ContentDocumentMapper mapper;

    private ContentIndexService service;

    @BeforeEach
    void setUp() {
        service = new ContentIndexService(repository, mapper);
    }

    @Nested
    @DisplayName("upsert()")
    class UpsertTest {

        @Test
        @DisplayName("모델 저장 성공")
        void withValidModel_savesDocument() {
            // given
            ContentModel model = ContentModel.builder()
                .id(UUID.randomUUID())
                .type(ContentModel.ContentType.movie)
                .title("인셉션")
                .build();

            ContentDocument document = ContentDocument.builder()
                .id(model.getId().toString())
                .type("movie")
                .title("인셉션")
                .build();

            when(mapper.toDocument(model)).thenReturn(document);

            // when
            service.upsert(model);

            // then
            verify(mapper).toDocument(model);
            verify(repository).save(document);
        }

        @Test
        @DisplayName("null 모델은 무시")
        void withNull_doesNothing() {
            // when
            service.upsert(null);

            // then
            verifyNoInteractions(mapper, repository);
        }

        @Test
        @DisplayName("저장 실패 시 예외 전파")
        void withRepositoryError_propagatesException() {
            // given
            ContentModel model = ContentModel.builder()
                .id(UUID.randomUUID())
                .type(ContentModel.ContentType.movie)
                .title("인셉션")
                .build();

            ContentDocument document = ContentDocument.builder().build();
            when(mapper.toDocument(model)).thenReturn(document);
            doThrow(new RuntimeException("ES connection failed"))
                .when(repository).save(any());

            // when & then
            assertThatThrownBy(() -> service.upsert(model))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("ES connection failed");
        }
    }

    @Nested
    @DisplayName("upsertAll()")
    class UpsertAllTest {

        @Test
        @DisplayName("여러 모델 일괄 저장 성공")
        void withValidModels_savesAllDocuments() {
            // given
            ContentModel model1 = ContentModel.builder()
                .id(UUID.randomUUID())
                .type(ContentModel.ContentType.movie)
                .title("인셉션")
                .build();

            ContentModel model2 = ContentModel.builder()
                .id(UUID.randomUUID())
                .type(ContentModel.ContentType.tvSeries)
                .title("브레이킹 배드")
                .build();

            List<ContentModel> models = List.of(model1, model2);

            ContentDocument doc1 = ContentDocument.builder().id(model1.getId().toString()).build();
            ContentDocument doc2 = ContentDocument.builder().id(model2.getId().toString()).build();

            when(mapper.toDocument(model1)).thenReturn(doc1);
            when(mapper.toDocument(model2)).thenReturn(doc2);

            // when
            service.upsertAll(models);

            // then
            verify(mapper, times(2)).toDocument(any(ContentModel.class));
            verify(repository).saveAll(anyList());
        }

        @Test
        @DisplayName("null 리스트는 무시")
        void withNull_doesNothing() {
            // when
            service.upsertAll(null);

            // then
            verifyNoInteractions(mapper, repository);
        }

        @Test
        @DisplayName("빈 리스트는 무시")
        void withEmptyList_doesNothing() {
            // when
            service.upsertAll(Collections.emptyList());

            // then
            verifyNoInteractions(mapper, repository);
        }

        @Test
        @DisplayName("일괄 저장 실패 시 예외 전파")
        void withRepositoryError_propagatesException() {
            // given
            ContentModel model = ContentModel.builder()
                .id(UUID.randomUUID())
                .type(ContentModel.ContentType.movie)
                .title("인셉션")
                .build();

            ContentDocument document = ContentDocument.builder().build();
            when(mapper.toDocument(any())).thenReturn(document);
            doThrow(new RuntimeException("Bulk operation failed"))
                .when(repository).saveAll(anyList());

            // when & then
            assertThatThrownBy(() -> service.upsertAll(List.of(model)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Bulk operation failed");
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTest {

        @Test
        @DisplayName("문서 삭제 성공")
        void withValidId_deletesDocument() {
            // given
            UUID contentId = UUID.randomUUID();

            // when
            service.delete(contentId);

            // then
            verify(repository).deleteById(contentId.toString());
        }

        @Test
        @DisplayName("null ID는 무시")
        void withNull_doesNothing() {
            // when
            service.delete(null);

            // then
            verifyNoInteractions(repository);
        }

        @Test
        @DisplayName("삭제 실패 시 예외 전파")
        void withRepositoryError_propagatesException() {
            // given
            UUID contentId = UUID.randomUUID();
            doThrow(new RuntimeException("Delete failed"))
                .when(repository).deleteById(any());

            // when & then
            assertThatThrownBy(() -> service.delete(contentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Delete failed");
        }
    }

    @Nested
    @DisplayName("deleteAll()")
    class DeleteAllTest {

        @Test
        @DisplayName("여러 문서 일괄 삭제 성공")
        void withValidIds_deletesAllDocuments() {
            // given
            List<UUID> contentIds = List.of(UUID.randomUUID(), UUID.randomUUID());

            // when
            service.deleteAll(contentIds);

            // then
            verify(repository).deleteAllById(anyList());
        }

        @Test
        @DisplayName("null 리스트는 무시")
        void withNull_doesNothing() {
            // when
            service.deleteAll(null);

            // then
            verifyNoInteractions(repository);
        }

        @Test
        @DisplayName("빈 리스트는 무시")
        void withEmptyList_doesNothing() {
            // when
            service.deleteAll(Collections.emptyList());

            // then
            verifyNoInteractions(repository);
        }

        @Test
        @DisplayName("일괄 삭제 실패 시 예외 전파")
        void withRepositoryError_propagatesException() {
            // given
            List<UUID> contentIds = List.of(UUID.randomUUID());
            doThrow(new RuntimeException("Bulk delete failed"))
                .when(repository).deleteAllById(anyList());

            // when & then
            assertThatThrownBy(() -> service.deleteAll(contentIds))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Bulk delete failed");
        }
    }
}
