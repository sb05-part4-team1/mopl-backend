package com.mopl.search.content.index;

import com.mopl.search.config.properties.SearchIndexProperties;
import com.mopl.search.document.ContentDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContentIndexInitializer 단위 테스트")
class ContentIndexInitializerTest {

    @Mock
    private ElasticsearchOperations operations;

    @Mock
    private IndexOperations indexOps;

    @Mock
    private ApplicationArguments args;

    private SearchIndexProperties indexProps;
    private ContentIndexInitializer initializer;

    @BeforeEach
    void setUp() {
        indexProps = new SearchIndexProperties();
        initializer = new ContentIndexInitializer(operations, indexProps);

        given(operations.indexOps(ContentDocument.class)).willReturn(indexOps);
        given(indexOps.getIndexCoordinates()).willReturn(IndexCoordinates.of("contents"));
    }

    @Nested
    @DisplayName("run()")
    class RunTest {

        @Test
        @DisplayName("인덱스가 존재하고 recreate 비활성화 시 초기화 건너뜀")
        void withExistingIndexAndRecreateDisabled_skipsInit() {
            // given
            indexProps.setRecreateOnStartup(false);
            given(indexOps.exists()).willReturn(true);

            // when
            initializer.run(args);

            // then
            then(indexOps).should(never()).delete();
            then(indexOps).should(never()).create();
            then(indexOps).should(never()).putMapping(nullable(Document.class));
        }

        @Test
        @DisplayName("인덱스가 존재하고 recreate 활성화 시 삭제 후 재생성")
        void withExistingIndexAndRecreateEnabled_deletesAndRecreates() {
            // given
            indexProps.setRecreateOnStartup(true);
            given(indexOps.exists()).willReturn(true);
            given(indexOps.delete()).willReturn(true);
            given(indexOps.create()).willReturn(true);
            given(indexOps.createMapping(ContentDocument.class)).willReturn(null);
            given(indexOps.putMapping(nullable(Document.class))).willReturn(true);

            // when
            initializer.run(args);

            // then
            then(indexOps).should().delete();
            then(indexOps).should().create();
            then(indexOps).should().putMapping(nullable(Document.class));
        }

        @Test
        @DisplayName("인덱스가 없으면 새로 생성")
        void withNoExistingIndex_createsNew() {
            // given
            indexProps.setRecreateOnStartup(false);
            given(indexOps.exists()).willReturn(false);
            given(indexOps.create()).willReturn(true);
            given(indexOps.createMapping(ContentDocument.class)).willReturn(null);
            given(indexOps.putMapping(nullable(Document.class))).willReturn(true);

            // when
            initializer.run(args);

            // then
            then(indexOps).should(never()).delete();
            then(indexOps).should().create();
            then(indexOps).should().putMapping(nullable(Document.class));
        }

        @Test
        @DisplayName("인덱스 생성 실패 시 예외 전파")
        void withCreateFailure_propagatesException() {
            // given
            indexProps.setRecreateOnStartup(false);
            given(indexOps.exists()).willReturn(false);
            given(indexOps.create()).willThrow(new RuntimeException("ES connection failed"));

            // when & then
            assertThatThrownBy(() -> initializer.run(args))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ES connection failed");
        }

        @Test
        @DisplayName("인덱스 삭제 실패 시에도 생성 시도")
        void withDeleteFailure_stillTriesToCreate() {
            // given
            indexProps.setRecreateOnStartup(true);
            given(indexOps.exists()).willReturn(true);
            given(indexOps.delete()).willReturn(false);
            given(indexOps.create()).willReturn(true);
            given(indexOps.createMapping(ContentDocument.class)).willReturn(null);
            given(indexOps.putMapping(nullable(Document.class))).willReturn(true);

            // when
            initializer.run(args);

            // then
            then(indexOps).should().delete();
            then(indexOps).should().create();
        }

        @Test
        @DisplayName("매핑 적용 실패 시에도 정상 종료")
        void withPutMappingFailure_completesNormally() {
            // given
            indexProps.setRecreateOnStartup(false);
            given(indexOps.exists()).willReturn(false);
            given(indexOps.create()).willReturn(true);
            given(indexOps.createMapping(ContentDocument.class)).willReturn(null);
            given(indexOps.putMapping(nullable(Document.class))).willReturn(false);

            // when
            initializer.run(args);

            // then
            then(indexOps).should().create();
            then(indexOps).should().putMapping(nullable(Document.class));
        }
    }
}
