package com.mopl.domain.service.content;

import com.mopl.domain.exception.content.ContentNotFoundException;
import com.mopl.domain.fixture.ContentModelFixture;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.repository.content.ContentQueryRepository;
import com.mopl.domain.repository.content.ContentQueryRequest;
import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContentService 단위 테스트")
class ContentServiceTest {

    @Mock
    private ContentQueryRepository contentQueryRepository;

    @Mock
    private ContentRepository contentRepository;

    @InjectMocks
    private ContentService contentService;

    @Nested
    @DisplayName("getAll()")
    class GetAllTest {

        @Test
        @DisplayName("Repository에 위임하여 결과 반환")
        void delegatesToRepository() {
            // given
            ContentQueryRequest request = new ContentQueryRequest(
                null, null, null, null, null, null, null, null
            );
            CursorResponse<ContentModel> expectedResponse = CursorResponse.empty(
                "watcherCount", SortDirection.DESCENDING
            );

            given(contentQueryRepository.findAll(request)).willReturn(expectedResponse);

            // when
            CursorResponse<ContentModel> result = contentService.getAll(request);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            then(contentQueryRepository).should().findAll(request);
        }

        @Test
        @DisplayName("콘텐츠 목록이 있으면 결과 반환")
        void withContents_returnsContentList() {
            // given
            ContentModel content1 = ContentModelFixture.create();
            ContentModel content2 = ContentModelFixture.create();
            ContentQueryRequest request = new ContentQueryRequest(
                null, null, null, null, null, 20, null, null
            );
            CursorResponse<ContentModel> expectedResponse = CursorResponse.of(
                List.of(content1, content2),
                null,
                null,
                false,
                2L,
                "watcherCount",
                SortDirection.DESCENDING
            );

            given(contentQueryRepository.findAll(request)).willReturn(expectedResponse);

            // when
            CursorResponse<ContentModel> result = contentService.getAll(request);

            // then
            assertThat(result.data()).hasSize(2);
            assertThat(result.totalCount()).isEqualTo(2L);
            then(contentQueryRepository).should().findAll(request);
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetByIdTest {

        @Test
        @DisplayName("존재하는 콘텐츠 ID로 조회하면 ContentModel 반환")
        void withExistingContentId_returnsContentModel() {
            // given
            UUID contentId = UUID.randomUUID();
            ContentModel contentModel = ContentModelFixture.builder()
                .set("id", contentId)
                .sample();

            given(contentRepository.findById(contentId)).willReturn(Optional.of(contentModel));

            // when
            ContentModel result = contentService.getById(contentId);

            // then
            assertThat(result).isEqualTo(contentModel);
            assertThat(result.getId()).isEqualTo(contentId);
            then(contentRepository).should().findById(contentId);
        }

        @Test
        @DisplayName("존재하지 않는 콘텐츠 ID로 조회하면 ContentNotFoundException 발생")
        void withNonExistingContentId_throwsContentNotFoundException() {
            // given
            UUID contentId = UUID.randomUUID();

            given(contentRepository.findById(contentId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> contentService.getById(contentId))
                .isInstanceOf(ContentNotFoundException.class)
                .satisfies(e -> {
                    ContentNotFoundException ex = (ContentNotFoundException) e;
                    assertThat(ex.getDetails().get("id")).isEqualTo(contentId);
                });

            then(contentRepository).should().findById(contentId);
        }
    }

    @Nested
    @DisplayName("create()")
    class CreateTest {

        @Test
        @DisplayName("유효한 콘텐츠 생성")
        void withValidContent_createsContent() {
            // given
            ContentModel contentModel = ContentModelFixture.create();

            given(contentRepository.save(contentModel)).willReturn(contentModel);

            // when
            ContentModel result = contentService.create(contentModel);

            // then
            assertThat(result).isEqualTo(contentModel);
            then(contentRepository).should().save(contentModel);
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTest {

        @Test
        @DisplayName("콘텐츠 정보 업데이트 성공")
        void withValidContent_updatesContent() {
            // given
            ContentModel contentModel = ContentModelFixture.create();

            given(contentRepository.save(contentModel)).willReturn(contentModel);

            // when
            ContentModel result = contentService.update(contentModel);

            // then
            assertThat(result).isEqualTo(contentModel);
            then(contentRepository).should().save(contentModel);
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTest {

        @Test
        @DisplayName("콘텐츠 삭제 성공")
        void withValidContent_deletesContent() {
            // given
            ContentModel contentModel = ContentModelFixture.create();

            given(contentRepository.save(contentModel)).willReturn(contentModel);

            // when
            contentService.delete(contentModel);

            // then
            then(contentRepository).should().save(contentModel);
        }
    }
}
